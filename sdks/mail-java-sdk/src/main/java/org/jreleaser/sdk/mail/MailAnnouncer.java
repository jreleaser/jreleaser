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
package org.jreleaser.sdk.mail;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Mail;
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
public class MailAnnouncer implements Announcer {
    private final JReleaserContext context;

    MailAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.Mail.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getMail().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Mail mail = context.getModel().getAnnounce().getMail();

        String message = "";
        if (isNotBlank(mail.getMessage())) {
            message = mail.getResolvedMessage(context);
        } else {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put(Constants.KEY_CHANGELOG, MustacheUtils.passThrough(context.getChangelog()));
            context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
            message = mail.getResolvedMessageTemplate(context, props);
        }

        String subject = mail.getResolvedSubject(context);
        context.getLogger().info("subject: {}", subject);
        context.getLogger().debug("message: {}", message);

        try {
            MessageMailCommand.builder(context.getLogger())
                .dryrun(context.isDryrun())
                .transport(mail.getTransport())
                .host(mail.getHost())
                .port(mail.getPort())
                .auth(mail.isAuth())
                .username(mail.getUsername())
                .password(context.isDryrun() ? "**UNDEFINED**" : mail.getResolvedPassword())
                .from(mail.getFrom())
                .to(mail.getTo())
                .cc(mail.getCc())
                .bcc(mail.getBcc())
                .subject(subject)
                .message(message)
                .mimeType(mail.getMimeType())
                .build()
                .execute();
        } catch (MailException e) {
            context.getLogger().trace(e);
            throw new AnnounceException(e);
        }
    }
}
