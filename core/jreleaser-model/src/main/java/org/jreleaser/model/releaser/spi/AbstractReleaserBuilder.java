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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractReleaserBuilder<R extends Releaser, B extends ReleaserBuilder<R, B>> implements ReleaserBuilder<R, B> {
    protected final List<Path> assets = new ArrayList<>();
    protected JReleaserContext context;

    protected final B self() {
        return (B) this;
    }

    @Override
    public B addReleaseAsset(Path asset) {
        if (null != asset && asset.toFile().exists()) {
            this.assets.add(asset);
        }
        return self();
    }

    @Override
    public B addReleaseAssets(Path assets) {
        if (assets.toFile().exists()) {
            for (File asset : assets.toFile().listFiles()) {
                addReleaseAsset(asset.toPath().toAbsolutePath());
            }
        }

        return self();
    }

    @Override
    public B setReleaseAssets(List<Path> assets) {
        if (null != assets) {
            this.assets.addAll(assets);
        }
        return self();
    }

    protected void validate() {
        requireNonNull(context, "'context' must not be null");
        if (assets.isEmpty()) {
            throw new IllegalArgumentException("'assets must not be empty");
        }
    }

    @Override
    public B configureWith(JReleaserContext context) {
        this.context = context;

        for (Distribution distribution : context.getModel().getDistributions().values()) {
            for (Artifact artifact : distribution.getArtifacts()) {
                addReleaseAsset(context.getBasedir().resolve(Paths.get(artifact.getPath())));
            }
        }

        addReleaseAsset(context.getChecksumsDirectory().resolve("checksums.txt"));
        addReleaseAssets(context.getSignaturesDirectory());

        return self();
    }
}