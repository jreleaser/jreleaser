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
package org.jreleaser.sdk.commons;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.sbom.SbomCataloger;
import org.jreleaser.model.internal.checksum.Checksum;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.signing.Signing;
import org.jreleaser.model.internal.upload.ArtifactoryUploader;
import org.jreleaser.model.internal.upload.Uploader;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessorHelper;
import org.jreleaser.model.spi.upload.ArtifactUploader;
import org.jreleaser.util.Algorithm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.jreleaser.model.Constants.KEY_PLATFORM_REPLACED;
import static org.jreleaser.model.api.checksum.Checksum.INDIVIDUAL_CHECKSUM;
import static org.jreleaser.model.api.checksum.Checksum.KEY_SKIP_CHECKSUM;
import static org.jreleaser.model.api.signing.Signing.KEY_SKIP_SIGNING;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class AbstractArtifactUploader<A extends org.jreleaser.model.api.upload.Uploader, U extends Uploader<A>> implements ArtifactUploader<A, U> {
    protected final JReleaserContext context;

    protected AbstractArtifactUploader(JReleaserContext context) {
        this.context = context;
    }

    protected Set<Artifact> collectArtifacts() {
        Set<Artifact> artifacts = new LinkedHashSet<>();
        List<String> keys = getUploader().resolveSkipKeys();
        Checksum checksum = context.getModel().getChecksum();
        boolean uploadChecksums = getUploader().isChecksums() && !(getUploader() instanceof ArtifactoryUploader);
        boolean isChecksumsEnabled = checksum.isFiles() || checksum.isArtifacts();

        if (getUploader().isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActiveAndSelected()) continue;
                Path path = artifact.getEffectivePath(context);
                if (isSkip(artifact, keys)) continue;
                if (Files.exists(path) && 0 != path.toFile().length()) {
                    artifacts.add(artifact);
                    if (uploadChecksums &&
                        checksum.isFiles() &&
                        isIndividual(context, artifact) &&
                        !artifact.extraPropertyIsTrue(KEY_SKIP_CHECKSUM)) {
                        for (Algorithm algorithm : checksum.getAlgorithms()) {
                            artifacts.add(Artifact.of(context.getChecksumsDirectory()
                                .resolve(path.getFileName() + "." + algorithm.formatted())));
                        }
                    }
                }
            }
        }

        if (getUploader().isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                if (isSkip(distribution, keys)) continue;
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActiveAndSelected()) continue;
                    Path path = artifact.getEffectivePath(context, distribution);
                    if (isSkip(artifact, keys)) continue;
                    if (Files.exists(path) && 0 != path.toFile().length()) {
                        String platform = artifact.getPlatform();
                        String platformReplaced = distribution.getPlatform().applyReplacements(platform);
                        if (isNotBlank(platformReplaced)) {
                            artifact.getExtraProperties().put(KEY_PLATFORM_REPLACED, platformReplaced);
                        }
                        artifacts.add(artifact);
                        if (uploadChecksums &&
                            checksum.isArtifacts() &&
                            isIndividual(context, distribution, artifact)) {
                            for (Algorithm algorithm : checksum.getAlgorithms()) {
                                artifacts.add(Artifact.of(context.getChecksumsDirectory()
                                    .resolve(distribution.getName())
                                    .resolve(path.getFileName() + "." + algorithm.formatted())));
                            }
                        }
                    }
                }
            }
        }

        if (uploadChecksums && isChecksumsEnabled) {
            for (Algorithm algorithm : checksum.getAlgorithms()) {
                Path checksums = context.getChecksumsDirectory()
                    .resolve(checksum.getResolvedName(context, algorithm));
                if (Files.exists(checksums)) {
                    artifacts.add(Artifact.of(checksums));
                }
            }
        }

        if (getUploader().isCatalogs()) {
            List<? extends SbomCataloger<?>> catalogers = context.getModel().getCatalog().getSbom().findAllActiveSbomCatalogers();
            for (SbomCataloger<?> cataloger : catalogers) {
                if (!cataloger.getPack().isEnabled()) continue;
                artifacts.addAll(SbomCatalogerProcessorHelper.resolveArtifacts(context, cataloger));
            }
        }

        Signing signing = context.getModel().getSigning();
        if (getUploader().isSignatures() && signing.isEnabled()) {
            String extension = signing.getSignatureExtension();

            List<Artifact> signatures = new ArrayList<>();
            for (Artifact artifact : artifacts) {
                if (artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                Path signaturePath = context.getSignaturesDirectory()
                    .resolve(artifact.getEffectivePath(context).getFileName() + extension);
                if (Files.exists(signaturePath) && 0 != signaturePath.toFile().length()) {
                    signatures.add(Artifact.of(signaturePath, artifact.getExtraProperties()));
                }
            }
            if (!signatures.isEmpty() && signing.getMode() == org.jreleaser.model.Signing.Mode.COSIGN) {
                Path publicKeyFile = signing.getCosign().getResolvedPublicKeyFilePath(context);
                signatures.add(Artifact.of(publicKeyFile));
            }

            artifacts.addAll(signatures);
        }

        if (getUploader().isCatalogs()) {
            artifacts.addAll(SbomCatalogerProcessorHelper.resolveArtifacts(context));
        }

        return artifacts;
    }

    private boolean isSkip(ExtraProperties props, List<String> keys) {
        for (String key : keys) {
            if (props.extraPropertyIsTrue(key)) {
                return true;
            }
        }
        return false;
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
