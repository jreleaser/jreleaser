/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.zernio;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.zernio.api.Account;
import org.jreleaser.sdk.zernio.api.Platform;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.24.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class ZernioAnnouncer implements Announcer<org.jreleaser.model.api.announce.ZernioAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.ZernioAnnouncer zernio;

    public ZernioAnnouncer(JReleaserContext context) {
        this.context = context;
        this.zernio = context.getModel().getAnnounce().getZernio();
    }

    @Override
    public org.jreleaser.model.api.announce.ZernioAnnouncer getAnnouncer() {
        return zernio.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.ZernioAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return zernio.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        String message = "";
        if (isNotBlank(zernio.getMessage())) {
            message = zernio.getResolvedMessage(context);
        } else {
            TemplateContext props = new TemplateContext();
            context.getModel().getRelease().getReleaser().fillProps(props, context);
            message = zernio.getResolvedMessageTemplate(context, props);
        }

        try {
            ZernioSdk sdk = ZernioSdk.builder(context.asImmutable())
                .apiHost(zernio.getApiHost())
                .token(context.isDryrun() ? "**UNDEFINED**" : zernio.getToken())
                .connectTimeout(zernio.getConnectTimeout())
                .readTimeout(zernio.getReadTimeout())
                .dryrun(context.isDryrun())
                .build();

            if (!context.isDryrun()) {
                if (isNotBlank(getAnnouncer().getProfileId())) {
                    context.getLogger().info(RB.$("zernio.platforms.by.profile.id", getAnnouncer().getProfileId()));
                } else {
                    context.getLogger().info(RB.$("zernio.platforms", getAnnouncer().getProfileId()));
                }
                Set<Platform> platforms = sdk.listAccounts(getAnnouncer().getProfileId()).getAccounts().stream()
                    .filter(Account::isActive)
                    .map(Account::asPlatform)
                    .collect(toSet());

                if (platforms.isEmpty()) {
                    context.getLogger().warn(RB.$("zernio.platforms.empty"));
                    return;
                }

                sdk.post(message, platforms);
            }
        } catch (ZernioException e) {
            throw new AnnounceException(e);
        }
    }
}
