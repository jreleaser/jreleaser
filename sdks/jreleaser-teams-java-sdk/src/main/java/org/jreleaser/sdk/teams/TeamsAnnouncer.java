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
package org.jreleaser.sdk.teams;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.sdk.commons.ClientUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.mustache.MustacheUtils.passThrough;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class TeamsAnnouncer implements Announcer {
    private final JReleaserContext context;

    TeamsAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.TeamsAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getTeams().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        org.jreleaser.model.internal.announce.TeamsAnnouncer teams = context.getModel().getAnnounce().getTeams();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(Constants.KEY_CHANGELOG, passThrough(convertLineEndings(context.getChangelog())));
        context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
        String message = teams.getResolvedMessageTemplate(context, props);

        context.getLogger().info(RB.$("webhook.message.send"));

        if (!context.isDryrun()) {
            ClientUtils.webhook(context.getLogger(),
                teams.getResolvedWebhook(),
                teams.getConnectTimeout(),
                teams.getReadTimeout(),
                message);
        }
    }

    public static String convertLineEndings(String str) {
        return str.replaceAll("\\n", "\\\\n\\\\n");
    }
}
