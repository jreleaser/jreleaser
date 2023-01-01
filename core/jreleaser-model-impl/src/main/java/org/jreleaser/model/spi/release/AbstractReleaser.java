/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.model.spi.release;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.BaseReleaser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.7.0
 */
public abstract class AbstractReleaser<A extends org.jreleaser.model.api.release.Releaser> implements Releaser<A> {
    protected final JReleaserContext context;
    protected final List<Asset> assets = new ArrayList<>();

    protected AbstractReleaser(JReleaserContext context, List<Asset> assets) {
        this.context = context;
        this.assets.addAll(assets);
    }

    @Override
    public final void release() throws ReleaseException {
        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();

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
