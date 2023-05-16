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
package org.jreleaser.sdk.bitbucketcloud;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.release.Asset;
import org.jreleaser.model.spi.release.Release;
import org.jreleaser.model.spi.release.ReleaseException;
import org.jreleaser.model.spi.release.Repository;
import org.jreleaser.model.spi.release.User;
import org.jreleaser.sdk.bitbucketcloud.api.BBCRepository;
import org.jreleaser.sdk.git.release.AbstractReleaser;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * @author Hasnae Rehioui
 * @since 1.7.0
 */
public class BitbucketcloudReleaser extends AbstractReleaser<org.jreleaser.model.api.release.BitbucketcloudReleaser> {
    private static final long serialVersionUID = 5874983846985118132L;

    private final org.jreleaser.model.internal.release.BitbucketcloudReleaser bitbucketcloud;

    public BitbucketcloudReleaser(JReleaserContext context, Set<Asset> assets) {
        super(context, assets);
        bitbucketcloud = context.getModel().getRelease().getBitbucketcloud();
    }

    @Override
    public org.jreleaser.model.api.release.BitbucketcloudReleaser getReleaser() {
        return bitbucketcloud.asImmutable();
    }

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password) throws IOException {
        context.getLogger().debug(RB.$("git.repository.lookup"), owner, repo);
        Bitbucketcloud api = new Bitbucketcloud(
            context.getLogger(),
            password,
            bitbucketcloud.getConnectTimeout(),
            bitbucketcloud.getReadTimeout()
        );

        BBCRepository repository = api.findRepository(owner, repo);
        if (null == repository) {
            repository = api.createRepository(owner, repo);
        }

        return new Repository(
            Repository.Kind.BITBUCKETCLOUD,
            owner,
            repo,
            repository.getLinks().getHtml().getHref(),
            String.format("https://bitbucket.org/%s/%s.git", owner, repo)
        );
    }

    @Override
    public Optional<User> findUser(String email, String name) {
       return Optional.empty();
    }

    @Override
    public List<Release> listReleases(String owner, String repo) throws IOException {
        return emptyList();
    }

    @Override
    protected void createRelease() throws ReleaseException {
        if (context.getModel().getRelease().getReleaser().isMatch()) {
            super.createTag();
        }
    }
}
