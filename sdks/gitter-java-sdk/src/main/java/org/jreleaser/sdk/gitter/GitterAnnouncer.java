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
package org.jreleaser.sdk.gitter;

import org.jreleaser.model.Gitter;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class GitterAnnouncer implements Announcer {
    private final JReleaserContext context;

    GitterAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.Gitter.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getGitter().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Gitter gitter = context.getModel().getAnnounce().getGitter();

        String message = "";
        if (isNotBlank(gitter.getMessage())) {
            message = gitter.getResolvedMessage(context);
        } else {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put(Constants.KEY_CHANGELOG, MustacheUtils.passThrough(context.getChangelog()));
            context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
            message = gitter.getResolvedMessageTemplate(context, props);
        }

        context.getLogger().info("message: {}", message);

        if (!context.isDryrun()) {
            ClientUtils.webhook(context.getLogger(),
                gitter.getResolvedWebhook(),
                gitter.getConnectTimeout(),
                gitter.getReadTimeout(),
                Message.of(message));
        }
    }
}
