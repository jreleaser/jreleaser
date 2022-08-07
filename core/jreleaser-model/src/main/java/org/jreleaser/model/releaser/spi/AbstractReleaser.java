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
package org.jreleaser.model.releaser.spi;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.7.0
 */
public abstract class AbstractReleaser implements Releaser {
    protected final JReleaserContext context;
    protected final List<Asset> assets = new ArrayList<>();

    protected AbstractReleaser(JReleaserContext context, List<Asset> assets) {
        this.context = context;
        this.assets.addAll(assets);
    }

    @Override
    public final void release() throws ReleaseException {
        GitService service = context.getModel().getRelease().getGitService();

        if (service.isSkipRelease()) {
            if (service.isSkipTag()) {
                context.getLogger().info(RB.$("releaser.tag.and.release.skipped"));
            } else {
                createTag();
            }
        } else {
            createRelease();
        }
    }

    protected abstract void createTag() throws ReleaseException;

    protected abstract void createRelease() throws ReleaseException;
}
