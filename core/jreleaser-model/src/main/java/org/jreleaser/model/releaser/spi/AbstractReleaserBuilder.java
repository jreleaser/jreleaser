/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

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

        for (Artifact artifact : Artifacts.resolveFiles(context)) {
            addReleaseAsset(artifact.getResolvedPath(context));
        }

        for (Distribution distribution : context.getModel().getDistributions().values()) {
            for (Artifact artifact : distribution.getArtifacts()) {
                addReleaseAsset(artifact.getResolvedPath(context, distribution));
            }
        }

        Path checksums = context.getChecksumsDirectory().resolve("checksums.txt");
        if (Files.exists(checksums)) {
            addReleaseAsset(checksums);
        }
        addReleaseAssets(context.getSignaturesDirectory());

        return this;
    }
}