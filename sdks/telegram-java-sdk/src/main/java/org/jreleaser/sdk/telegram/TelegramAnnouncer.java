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
package org.jreleaser.sdk.telegram;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Telegram;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class TelegramAnnouncer implements Announcer {
    private final JReleaserContext context;

    TelegramAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.Telegram.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getTelegram().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Telegram telegram = context.getModel().getAnnounce().getTelegram();

        String message = "";
        if (isNotBlank(telegram.getMessage())) {
            message = telegram.getResolvedMessage(context);
        } else {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put(Constants.KEY_CHANGELOG, MustacheUtils.passThrough(context.getChangelog()));
            context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
            message = telegram.getResolvedMessageTemplate(context, props);
        }

        String chatId = telegram.getResolvedChatId();
        context.getLogger().info("message: {}", message);

        try {
            TelegramSdk sdk = TelegramSdk.builder(context.getLogger())
                .token(telegram.getResolvedToken())
                .connectTimeout(telegram.getConnectTimeout())
                .readTimeout(telegram.getReadTimeout())
                .dryrun(context.isDryrun())
                .build();
            sdk.sendMessage(chatId, message);
        } catch (TelegramException e) {
            throw new AnnounceException(e);
        }
    }
}
