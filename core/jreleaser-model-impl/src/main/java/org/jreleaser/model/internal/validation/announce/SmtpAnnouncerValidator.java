/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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

    public static void validateSmtp(JReleaserContext context, SmtpAnnouncer smtp, Errors errors) {
        context.getLogger().debug("announce.smtp");
        resolveActivatable(context, smtp, "announce.smtp", "NEVER");
        if (!smtp.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (null == smtp.getTransport()) {
            smtp.setTransport(org.jreleaser.model.Mail.Transport.SMTP);
        }

        if (isBlank(smtp.getHost())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "mail.host"));
        }

        if (null == smtp.getPort()) {
            smtp.setPort(25);
        }

        if (!smtp.isAuthSet()) {
            smtp.setAuth(true);
        }

        if (isBlank(smtp.getUsername())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "mail.username"));
        }

        smtp.setPassword(
            checkProperty(context,
                listOf(
                    "announce.smtp.password",
                    SMTP_PASSWORD,
                    MAIL_PASSWORD),
                "announce.smtp.password",
                smtp.getPassword(),
                errors,
                context.isDryrun()));

        if (isBlank(smtp.getFrom())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "mail.from"));
        }

        boolean to = isBlank(smtp.getTo());
        boolean cc = isBlank(smtp.getCc());
        boolean bcc = isBlank(smtp.getBcc());

        if (!to && !cc && !bcc) {
            errors.configuration(RB.$("validation_mail_not_blank", "mail.to, mail.cc,", "mail.bcc"));
        }

        if (isBlank(smtp.getSubject())) {
            smtp.setSubject(RB.$("default.discussion.title"));
        }

        if (null == smtp.getMimeType()) {
            smtp.setMimeType(org.jreleaser.model.Mail.MimeType.TEXT);
        }

        if (isBlank(smtp.getMessage()) && isBlank(smtp.getMessageTemplate())) {
            smtp.setMessageTemplate("src/jreleaser/templates/mail.tpl");
        }

        if (isNotBlank(smtp.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(smtp.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "mail.messageTemplate", smtp.getMessageTemplate()));
        }
    }
}