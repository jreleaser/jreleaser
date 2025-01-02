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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.spi.release.Asset;
import org.jreleaser.model.spi.release.ReleaseException;
import org.jreleaser.model.spi.release.Releaser;
import org.jreleaser.sdk.git.ChangelogProvider;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andres Almiray
 * @since 0.7.0
 */
public abstract class AbstractReleaser<A extends org.jreleaser.model.api.release.Releaser> implements Releaser<A> {
    private static final long serialVersionUID = 362449254352903201L;

    protected final JReleaserContext context;
    protected final Set<Asset> assets = new TreeSet<>();

    protected AbstractReleaser(JReleaserContext context, Set<Asset> assets) {
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

    @Override
    public String generateReleaseNotes() throws IOException {
        try {
            return ChangelogProvider.getChangelog(context).trim();
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_changelog"), e);
        }
    }

    protected void createTag() throws ReleaseException {
        ReleaseUtils.createTag(context);
    }

    protected abstract void createRelease() throws ReleaseException;
}
