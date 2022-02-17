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
package org.jreleaser.sdk.zulip;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Zulip;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class ZulipAnnouncer implements Announcer {
    private final JReleaserContext context;

    ZulipAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.Zulip.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getZulip().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Zulip zulip = context.getModel().getAnnounce().getZulip();

        String message = "";
        if (isNotBlank(zulip.getMessage())) {
            message = zulip.getResolvedMessage(context);
        } else {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put(Constants.KEY_CHANGELOG, MustacheUtils.passThrough(context.getChangelog()));
            context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
            message = zulip.getResolvedMessageTemplate(context, props);
        }

        String subject = zulip.getResolvedSubject(context);
        context.getLogger().info("channel: {}", zulip.getChannel());
        context.getLogger().info("subject: {}", subject);
        context.getLogger().debug("message: {}", message);

        try {
            ZulipSdk sdk = ZulipSdk.builder(context.getLogger())
                .apiHost(zulip.getApiHost())
                .account(zulip.getAccount())
                .apiKey(context.isDryrun() ? "**UNDEFINED**" : zulip.getResolvedApiKey())
                .connectTimeout(zulip.getConnectTimeout())
                .readTimeout(zulip.getReadTimeout())
                .dryrun(context.isDryrun())
                .build();
            sdk.message(zulip.getChannel(), subject, message);
        } catch (ZulipException e) {
            throw new AnnounceException(e);
        }
    }
}
