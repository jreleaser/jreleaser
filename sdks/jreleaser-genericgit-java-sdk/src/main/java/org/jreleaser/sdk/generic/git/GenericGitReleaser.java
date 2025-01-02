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
package org.jreleaser.sdk.generic.git;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.common.ExtraProperties;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.release.Asset;
import org.jreleaser.model.spi.release.Release;
import org.jreleaser.model.spi.release.ReleaseException;
import org.jreleaser.model.spi.release.Repository;
import org.jreleaser.model.spi.release.User;
import org.jreleaser.sdk.git.release.AbstractReleaser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class GenericGitReleaser extends AbstractReleaser<org.jreleaser.model.api.release.GenericGitReleaser> {
    private static final long serialVersionUID = -6810299968968134132L;

    public GenericGitReleaser(JReleaserContext context, Set<Asset> assets) {
        super(context, assets);
    }

    @Override
    public org.jreleaser.model.api.release.GenericGitReleaser getReleaser() {
        return context.getModel().getRelease().getGeneric().asImmutable();
    }

    @Override
    protected void createTag() throws ReleaseException {
        if (context.getModel().getRelease().getReleaser().isMatch()) {
            super.createTag();
        }
    }

    @Override
    protected void createRelease() throws ReleaseException {
        context.getLogger().info(RB.$("generic.git.warning"));
        createTag();
    }

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password, ExtraProperties extraProperties) throws IOException {
        return null;
    }

    @Override
    public Optional<User> findUser(String email, String name) {
        return Optional.empty();
    }

    @Override
    public List<Release> listReleases(String owner, String repo) throws IOException {
        return Collections.emptyList();
    }
}
