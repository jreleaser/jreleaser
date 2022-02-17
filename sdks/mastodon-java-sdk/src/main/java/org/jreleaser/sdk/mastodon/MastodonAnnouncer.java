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
package org.jreleaser.sdk.mastodon;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Mastodon;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class MastodonAnnouncer implements Announcer {
    private final JReleaserContext context;

    MastodonAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return Mastodon.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getMastodon().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Mastodon mastodon = context.getModel().getAnnounce().getMastodon();

        String status = mastodon.getResolvedStatus(context);

        context.getLogger().debug("status: {}", status);

        try {
            MastodonSdk sdk = MastodonSdk.builder(context.getLogger())
                .host(mastodon.getHost())
                .accessToken(mastodon.getResolvedAccessToken())
                .connectTimeout(mastodon.getConnectTimeout())
                .readTimeout(mastodon.getReadTimeout())
                .dryrun(context.isDryrun())
                .build();
            sdk.status(status);
        } catch (MastodonException e) {
            throw new AnnounceException(e);
        }
    }
}
