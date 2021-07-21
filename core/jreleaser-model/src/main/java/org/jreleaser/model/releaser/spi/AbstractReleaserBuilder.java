/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.model.releaser.spi;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.util.Artifacts;
import org.jreleaser.util.Algorithm;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.model.Checksum.INDIVIDUAL_CHECKSUM;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractReleaserBuilder<R extends Releaser> implements ReleaserBuilder<R> {
    protected final List<Path> assets = new ArrayList<>();
    protected JReleaserContext context;

    @Override
    public ReleaserBuilder<R> addReleaseAsset(Path asset) {
        if (null != asset && asset.toFile().exists()) {
            this.assets.add(asset);
        }
        return this;
    }

    @Override
    public ReleaserBuilder<R> addReleaseAssets(Path assets) {
        if (assets.toFile().exists()) {
            for (File asset : assets.toFile().listFiles()) {
                addReleaseAsset(asset.toPath().toAbsolutePath());
            }
        }

        return this;
    }

    @Override
    public ReleaserBuilder<R> setReleaseAssets(List<Path> assets) {
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

        boolean uploadIndividualChecksums = context.getModel().getChecksum().isIndividual();
        for (Artifact artifact : Artifacts.resolveFiles(context)) {
            if (!artifact.isActive()) continue;
            Path path = artifact.getEffectivePath(context);
            addReleaseAsset(path);
            if (isIndividual(context, artifact)) {
                for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                    addReleaseAsset(context.getChecksumsDirectory()
                        .resolve(path.getFileName() + "." + algorithm.formatted()));
                }
            }
        }

        for (Distribution distribution : context.getModel().getActiveDistributions()) {
            for (Artifact artifact : distribution.getArtifacts()) {
                if (!artifact.isActive()) continue;
                Path path = artifact.getEffectivePath(context, distribution);
                addReleaseAsset(path);
                if (isIndividual(context, distribution, artifact)) {
                    for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                        addReleaseAsset(context.getChecksumsDirectory()
                            .resolve(distribution.getName())
                            .resolve(path.getFileName() + "." + algorithm.formatted()));
                    }
                }
            }
        }

        for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
            Path checksums = context.getChecksumsDirectory()
                .resolve(context.getModel().getChecksum().getResolvedName(context, algorithm));
            if (Files.exists(checksums)) {
                addReleaseAsset(checksums);
            }
        }

        if (context.getModel().getSigning().isEnabled()) {
            List<Path> assetsCopy = new ArrayList<>(assets);
            for (Path asset : assetsCopy) {
                Path signature = context.getSignaturesDirectory()
                    .resolve(asset.getFileName().toString() + (context.getModel().getSigning().isArmored() ? ".asc" : ".sig"));
                if (Files.exists(signature)) {
                    addReleaseAsset(signature);
                }
            }
        }

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