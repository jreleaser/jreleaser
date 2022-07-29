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

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.Mail;
import org.jreleaser.util.JReleaserLogger;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class MessageMailCommand implements MailCommand {
    private final JReleaserLogger logger;
    private final boolean dryrun;
    private final Mail.Transport transport;
    private final String host;
    private final Integer port;
    private final Boolean auth;
    private final String username;
    private final String password;
    private final String from;
    private final String to;
    private final String cc;
    private final String bcc;
    private final String subject;
    private final String message;
    private final Mail.MimeType mimeType;
    private final Map<String, String> properties = new LinkedHashMap<>();

    private MessageMailCommand(JReleaserLogger logger,
                               boolean dryrun,
                               Mail.Transport transport,
                               String host,
                               Integer port,
                               Boolean auth,
                               String username,
                               String password,
                               String from,
                               String to,
                               String cc,
                               String bcc,
                               String subject,
                               String message,
                               Mail.MimeType mimeType,
                               Map<String, String> properties) {
        this.logger = logger;
        this.dryrun = dryrun;
        this.transport = transport;
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.username = username;
        this.password = password;
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.message = message;
        this.mimeType = mimeType;
        this.properties.putAll(properties);
    }

    @Override
    public void execute() throws MailException {
        logger.info(RB.$("mail.message.send"));
        if (dryrun) return;

        Properties props = new Properties();
        props.putAll(properties);

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        if (auth) {
            if (!props.containsKey("mail.smtp.auth")) {
                props.put("mail.smtp.auth", "true");
            }
            if (transport == Mail.Transport.SMTP) {
                if (!props.containsKey("mail.smtp.starttls.enable")) {
                    props.put("mail.smtp.starttls.enable", "true");
                }
            } else {
                if (!props.containsKey("mail.smtp.socketFactory.port")) {
                    props.put("mail.smtp.socketFactory.port", port);
                }
                if (!props.containsKey("mail.smtp.socketFactory.class")) {
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                }
            }
        }

        try {
            Session session = Session.getInstance(props);
            Message message = new MimeMessage(session);

            if (isNotBlank(from)) {
                message.setFrom(new InternetAddress(from));
            }

            if (isNotBlank(to)) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            }
            if (isNotBlank(cc)) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            }
            if (isNotBlank(bcc)) {
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
            }

            message.setSubject(subject);

            MimeMultipart content = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(this.message, mimeType.code());
            content.addBodyPart(textPart);
            message.setContent(content);

            message.setHeader("X-Mailer", "JReleaser " + JReleaserVersion.getPlainVersion());
            message.setSentDate(new Date());

            Transport t = session.getTransport(transport.name().toLowerCase(Locale.ENGLISH));
            if (auth) {
                t.connect(host, username, password);
            } else {
                t.connect();
            }
            t.sendMessage(message, message.getAllRecipients());
        } catch (Exception e) {
            throw new MailException(e);
        }
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder {
        private final JReleaserLogger logger;
        private boolean dryrun;
        private Mail.Transport transport = Mail.Transport.SMTP;
        private String host;
        private Integer port;
        private Boolean auth;
        private String username;
        private String password;
        private String from;
        private String to;
        private String cc;
        private String bcc;
        private String subject;
        private String message;
        private Mail.MimeType mimeType = Mail.MimeType.TEXT;
        private final Map<String, String> properties = new LinkedHashMap<>();

        protected Builder(JReleaserLogger logger) {
            this.logger = logger;
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder transport(Mail.Transport transport) {
            this.transport = transport;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(Integer port) {
            this.port = port;
            return this;
        }

        public Builder auth(Boolean auth) {
            this.auth = auth;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder cc(String cc) {
            this.cc = cc;
            return this;
        }

        public Builder bcc(String bcc) {
            this.bcc = bcc;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder mimeType(Mail.MimeType mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            this.properties.putAll(properties);
            return this;
        }

        public MessageMailCommand build() {
            return new MessageMailCommand(
                logger,
                dryrun,
                transport,
                host,
                port,
                auth,
                username,
                password,
                from,
                to,
                cc,
                bcc,
                subject,
                message,
                mimeType,
                properties);
        }
    }
}
