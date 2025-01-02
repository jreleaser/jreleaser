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
package org.jreleaser.sdk.smtp;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class SmtpAnnouncer implements Announcer<org.jreleaser.model.api.announce.SmtpAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.SmtpAnnouncer smtp;

    public SmtpAnnouncer(JReleaserContext context) {
        this.context = context;
        this.smtp = context.getModel().getAnnounce().getSmtp();
    }

    @Override
    public org.jreleaser.model.api.announce.SmtpAnnouncer getAnnouncer() {
        return smtp.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.SmtpAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return smtp.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        String message = "";
        if (isNotBlank(smtp.getMessage())) {
            message = smtp.getResolvedMessage(context);
        } else {
            TemplateContext props = new TemplateContext();
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            message = smtp.getResolvedMessageTemplate(context, props);
        }

        String subject = smtp.getResolvedSubject(context);
        context.getLogger().info("subject: {}", subject);
        context.getLogger().debug("message: {}", message);

        try {
            MessageSmtpCommand.builder(context.getLogger())
                .dryrun(context.isDryrun())
                .transport(smtp.getTransport())
                .host(smtp.getHost())
                .port(smtp.getPort())
                .auth(smtp.isAuth())
                .username(smtp.getUsername())
                .password(context.isDryrun() ? "**UNDEFINED**" : smtp.getPassword())
                .from(smtp.getFrom())
                .to(smtp.getTo())
                .cc(smtp.getCc())
                .bcc(smtp.getBcc())
                .subject(subject)
                .message(message)
                .mimeType(smtp.getMimeType())
                .build()
                .execute();
        } catch (SmtpException e) {
            context.getLogger().trace(e);
            throw new AnnounceException(e);
        }
    }
}
