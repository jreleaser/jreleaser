/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.sdk.git.release;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.sbom.SbomCataloger;
import org.jreleaser.model.internal.checksum.Checksum;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.signing.Signing;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessorHelper;
import org.jreleaser.model.spi.release.Asset;
import org.jreleaser.model.spi.release.Releaser;
import org.jreleaser.model.spi.release.ReleaserBuilder;
import org.jreleaser.util.Algorithm;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.model.api.checksum.Checksum.INDIVIDUAL_CHECKSUM;
import static org.jreleaser.model.api.checksum.Checksum.KEY_SKIP_CHECKSUM;
import static org.jreleaser.model.api.release.Releaser.KEY_SKIP_RELEASE;
import static org.jreleaser.model.api.release.Releaser.KEY_SKIP_RELEASE_SIGNATURES;
import static org.jreleaser.model.api.signing.Signing.KEY_SKIP_SIGNING;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractReleaserBuilder<R extends Releaser<?>> implements ReleaserBuilder<R> {
    protected final Set<Asset> assets = new TreeSet<>();
    protected JReleaserContext context;

    @Override
    public ReleaserBuilder<R> addReleaseAsset(Asset asset) {
        if (null != asset) {
            this.assets.add(asset);
        }
        return this;
    }

    @Override
    public ReleaserBuilder<R> addReleaseAssets(Path assets) {
        if (assets.toFile().exists()) {
            for (File asset : assets.toFile().listFiles()) {
                addReleaseAsset(Asset.file(Artifact.of(asset.toPath().toAbsolutePath())));
            }
        }

        return this;
    }

    @Override
    public ReleaserBuilder<R> setReleaseAssets(List<Asset> assets) {
        if (null != assets) {
            this.assets.addAll(assets);
        }
        return this;
    }

    protected void validate() {
        requireNonNull(context, "'context' must not be null");
    }

    @Override
    public ReleaserBuilder<R> configureWith(JReleaserContext context) {
        this.context = context;
        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();
        if (!service.resolveUploadAssetsEnabled(context.getModel().getProject())) {
            return this;
        }

        Set<Asset> assets = new LinkedHashSet<>();
        Checksum checksum = context.getModel().getChecksum();
        boolean isChecksumsEnabled = checksum.isFiles() || checksum.isArtifacts();

        if (service.isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActiveAndSelected() || artifact.extraPropertyIsTrue(KEY_SKIP_RELEASE) ||
                    artifact.isOptional(context) && !artifact.resolvedPathExists()) continue;
                Path path = artifact.getEffectivePath(context);
                assets.add(Asset.file(Artifact.of(path, artifact.getExtraProperties())));
                if (service.isChecksums() &&
                    checksum.isFiles() &&
                    isIndividual(context, artifact) &&
                    !artifact.extraPropertyIsTrue(KEY_SKIP_CHECKSUM)) {
                    for (Algorithm algorithm : checksum.getAlgorithms()) {
                        assets.add(Asset.checksum(Artifact.of(context.getChecksumsDirectory()
                            .resolve(path.getFileName() + "." + algorithm.formatted()))));
                    }
                }
            }
        }

        if (service.isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                if (distribution.extraPropertyIsTrue(KEY_SKIP_RELEASE)) {
                    continue;
                }
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActiveAndSelected() || artifact.extraPropertyIsTrue(KEY_SKIP_RELEASE) ||
                        artifact.isOptional(context) && !artifact.resolvedPathExists()) continue;
                    Path path = artifact.getEffectivePath(context, distribution);
                    assets.add(Asset.file(Artifact.of(path, artifact.getExtraProperties()), distribution));
                    if (service.isChecksums() &&
                        checksum.isArtifacts() &&
                        isIndividual(context, distribution, artifact)) {
                        for (Algorithm algorithm : checksum.getAlgorithms()) {
                            assets.add(Asset.checksum(Artifact.of(context.getChecksumsDirectory()
                                .resolve(distribution.getName())
                                .resolve(path.getFileName() + "." + algorithm.formatted()))));
                        }
                    }
                }
            }
        }

        if (service.isChecksums() && isChecksumsEnabled) {
            for (Algorithm algorithm : checksum.getAlgorithms()) {
                Path checksums = context.getChecksumsDirectory()
                    .resolve(checksum.getResolvedName(context, algorithm));
                if (Files.exists(checksums)) {
                    assets.add(Asset.checksum(Artifact.of(checksums)));
                }
            }
        }

        if (service.isCatalogs()) {
            List<? extends SbomCataloger<?>> catalogers = context.getModel().getCatalog().getSbom().findAllActiveSbomCatalogers();
            for (SbomCataloger<?> cataloger : catalogers) {
                if (!cataloger.getPack().isEnabled()) continue;
                SbomCatalogerProcessorHelper.resolveArtifacts(context, cataloger).stream()
                    .map(Asset::catalog)
                    .forEach(assets::add);
            }
        }

        Signing signing = context.getModel().getSigning();
        if (signing.isEnabled() && service.isSignatures()) {
            boolean signaturesAdded = false;
            List<Asset> assetsCopy = new ArrayList<>(assets);
            for (Asset asset : assetsCopy) {
                if (asset.getArtifact().extraPropertyIsTrue(KEY_SKIP_SIGNING) ||
                    asset.getArtifact().extraPropertyIsTrue(KEY_SKIP_RELEASE_SIGNATURES)) continue;
                Path signature = context.getSignaturesDirectory()
                    .resolve(asset.getFilename() + (signing.getSignatureExtension()));
                if (Files.exists(signature)) {
                    assets.add(Asset.signature(Artifact.of(signature)));
                    signaturesAdded = true;
                }
            }
            if (signaturesAdded && signing.getMode() == org.jreleaser.model.Signing.Mode.COSIGN) {
                Path publicKeyFile = signing.getCosign().getResolvedPublicKeyFilePath(context);
                assets.add(Asset.signature(Artifact.of(publicKeyFile)));
            }
        }

        if (service.isCatalogs()) {
            SbomCatalogerProcessorHelper.resolveArtifacts(context).stream()
                .map(Asset::catalog)
                .forEach(assets::add);
        }

        assets.forEach(this::addReleaseAsset);

        return this;
    }

    private boolean isIndividual(JReleaserContext context, Artifact artifact) {
        if (artifact.getExtraProperties().containsKey(INDIVIDUAL_CHECKSUM)) {
            return isTrue(artifact.getExtraProperties().get(INDIVIDUAL_CHECKSUM));
        }
        return context.getModel().getChecksum().isIndividual();
    }

    private boolean isIndividual(JReleaserContext context, Distribution distribution, Artifact artifact) {
        if (artifact.getExtraProperties().containsKey(INDIVIDUAL_CHECKSUM)) {
            return isTrue(artifact.getExtraProperties().get(INDIVIDUAL_CHECKSUM));
        }
        if (distribution.getExtraProperties().containsKey(INDIVIDUAL_CHECKSUM)) {
            return isTrue(distribution.getExtraProperties().get(INDIVIDUAL_CHECKSUM));
        }
        return context.getModel().getChecksum().isIndividual();
    }
}