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

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class MastodonAnnouncer implements Announcer<org.jreleaser.model.api.announce.MastodonAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.MastodonAnnouncer mastodon;

    public MastodonAnnouncer(JReleaserContext context) {
        this.context = context;
        this.mastodon = context.getModel().getAnnounce().getMastodon();
    }

    @Override
    public org.jreleaser.model.api.announce.MastodonAnnouncer getAnnouncer() {
        return mastodon.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.MastodonAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return mastodon.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
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
