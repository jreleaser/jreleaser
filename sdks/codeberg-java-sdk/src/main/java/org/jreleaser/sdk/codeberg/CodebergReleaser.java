/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.sdk.codeberg;

import org.jreleaser.model.Gitea;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.releaser.spi.Asset;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.sdk.gitea.GiteaReleaser;

import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class CodebergReleaser extends GiteaReleaser {
    public CodebergReleaser(JReleaserContext context, List<Asset> assets) {
        super(context, assets);
    }

    @Override
    protected Gitea resolveGiteaFromModel() {
        return context.getModel().getRelease().getCodeberg();
    }

    @Override
    protected Repository.Kind resolveRepositoryKind() {
        return Repository.Kind.CODEBERG;
    }
}
