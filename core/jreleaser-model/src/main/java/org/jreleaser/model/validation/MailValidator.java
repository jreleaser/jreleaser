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
package org.jreleaser.model.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Mail;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.Mail.MAIL_PASSWORD;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class MailValidator extends Validator {
    public static void validateMail(JReleaserContext context, Mail mail, Errors errors) {
        if (!mail.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.mail");

        if (null == mail.getTransport()) {
            mail.setTransport(Mail.Transport.SMTP);
        }

        if (isBlank(mail.getHost())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "mail.host"));
        }

        if (null == mail.getPort()) {
            mail.setPort(25);
        }

        if (!mail.isAuthSet()) {
            mail.setAuth(true);
        }

        if (isBlank(mail.getUsername())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "mail.username"));
        }

        mail.setPassword(
            checkProperty(context,
                MAIL_PASSWORD,
                "mail.password",
                mail.getPassword(),
                errors,
                context.isDryrun()));

        if (isBlank(mail.getFrom())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "mail.from"));
        }

        boolean to = isBlank(mail.getTo());
        boolean cc = isBlank(mail.getCc());
        boolean bcc = isBlank(mail.getBcc());

        if (!to && !cc && !bcc) {
            errors.configuration(RB.$("validation_mail_not_blank", "mail.to, mail.cc,", "mail.bcc"));
        }

        if (isBlank(mail.getSubject())) {
            mail.setSubject(RB.$("default.discussion.title"));
        }

        if (null == mail.getMimeType()) {
            mail.setMimeType(Mail.MimeType.TEXT);
        }

        if (isBlank(mail.getMessage()) && isBlank(mail.getMessageTemplate())) {
            mail.setMessageTemplate("src/jreleaser/templates/mail.tpl");
        }

        if (isNotBlank(mail.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(mail.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "mail.messageTemplate", mail.getMessageTemplate()));
        }
    }
}