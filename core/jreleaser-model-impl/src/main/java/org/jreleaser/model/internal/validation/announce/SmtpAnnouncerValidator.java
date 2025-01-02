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
package org.jreleaser.model.internal.validation.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.SmtpAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.SmtpAnnouncer.MAIL_PASSWORD;
import static org.jreleaser.model.api.announce.SmtpAnnouncer.SMTP_PASSWORD;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class SmtpAnnouncerValidator {
    private SmtpAnnouncerValidator() {
        // noop
    }

    public static void validateSmtp(JReleaserContext context, SmtpAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.smtp");
        resolveActivatable(context, announcer, "announce.smtp", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (null == announcer.getTransport()) {
            announcer.setTransport(org.jreleaser.model.Mail.Transport.SMTP);
        }

        if (isBlank(announcer.getHost())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "mail.host"));
        }

        if (null == announcer.getPort()) {
            announcer.setPort(25);
        }

        if (!announcer.isAuthSet()) {
            announcer.setAuth(true);
        }

        if (isBlank(announcer.getUsername())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "mail.username"));
        }

        announcer.setPassword(
            checkProperty(context,
                listOf(
                    "announce.smtp.password",
                    SMTP_PASSWORD,
                    MAIL_PASSWORD),
                "announce.smtp.password",
                announcer.getPassword(),
                errors,
                context.isDryrun()));

        if (isBlank(announcer.getFrom())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "mail.from"));
        }

        boolean to = isBlank(announcer.getTo());
        boolean cc = isBlank(announcer.getCc());
        boolean bcc = isBlank(announcer.getBcc());

        if (!to && !cc && !bcc) {
            errors.configuration(RB.$("validation_mail_not_blank", "mail.to, mail.cc,", "mail.bcc"));
        }

        if (isBlank(announcer.getSubject())) {
            announcer.setSubject(RB.$("default.discussion.title"));
        }

        if (null == announcer.getMimeType()) {
            announcer.setMimeType(org.jreleaser.model.Mail.MimeType.TEXT);
        }

        if (isBlank(announcer.getMessage()) && isBlank(announcer.getMessageTemplate())) {
            announcer.setMessageTemplate("src/jreleaser/templates/smtp.tpl");
        }

        if (isNotBlank(announcer.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "mail.messageTemplate", announcer.getMessageTemplate()));
        }
    }
}