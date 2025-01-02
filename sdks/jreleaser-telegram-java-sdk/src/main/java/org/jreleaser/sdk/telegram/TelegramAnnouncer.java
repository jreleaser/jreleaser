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
package org.jreleaser.sdk.telegram;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class TelegramAnnouncer implements Announcer<org.jreleaser.model.api.announce.TelegramAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.TelegramAnnouncer telegram;

    public TelegramAnnouncer(JReleaserContext context) {
        this.context = context;
        this.telegram = context.getModel().getAnnounce().getTelegram();
    }

    @Override
    public org.jreleaser.model.api.announce.TelegramAnnouncer getAnnouncer() {
        return telegram.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.TelegramAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return telegram.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        String message = "";
        if (isNotBlank(telegram.getMessage())) {
            message = telegram.getResolvedMessage(context);
        } else {
            TemplateContext props = new TemplateContext();
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            message = telegram.getResolvedMessageTemplate(context, props);
        }

        String chatId = telegram.getChatId();
        context.getLogger().info("message: {}", message);

        try {
            TelegramSdk sdk = TelegramSdk.builder(context.asImmutable())
                .token(telegram.getToken())
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
