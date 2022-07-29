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
package org.jreleaser.model;

import org.jreleaser.bundle.RB;
import org.jreleaser.util.Env;
import org.jreleaser.util.JReleaserException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.KEY_TAG_NAME;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Mail extends AbstractAnnouncer<Mail> {
    public static final String NAME = "mail";
    public static final String MAIL_PASSWORD = "MAIL_PASSWORD";

    private final Map<String, String> properties = new LinkedHashMap<>();

    private Transport transport;
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
    private String messageTemplate;
    private MimeType mimeType;

    public Mail() {
        super(NAME);
    }

    @Override
    public void merge(Mail mail) {
        freezeCheck();
        super.merge(mail);
        this.transport = merge(this.transport, mail.transport);
        this.host = merge(this.host, mail.host);
        this.port = merge(this.port, mail.port);
        this.auth = merge(this.auth, mail.auth);
        this.username = merge(this.username, mail.username);
        this.password = merge(this.password, mail.password);
        this.from = merge(this.from, mail.from);
        this.to = merge(this.to, mail.to);
        this.cc = merge(this.cc, mail.cc);
        this.bcc = merge(this.bcc, mail.bcc);
        this.subject = merge(this.subject, mail.subject);
        this.message = merge(this.message, mail.message);
        this.messageTemplate = merge(this.messageTemplate, mail.messageTemplate);
        this.mimeType = merge(this.mimeType, mail.mimeType);
        setProperties(merge(this.properties, mail.properties));
    }

    public String getResolvedSubject(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getGitService().getEffectiveTagName(context.getModel()));
        return resolveTemplate(subject, props);
    }

    public String getResolvedMessage(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getGitService().getEffectiveTagName(context.getModel()));
        return resolveTemplate(message, props);
    }

    public String getResolvedMessageTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getGitService()
            .getEffectiveTagName(context.getModel()));
        props.putAll(extraProps);

        Path templatePath = context.getBasedir().resolve(messageTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
    }

    public String getResolvedPassword() {
        return Env.env(MAIL_PASSWORD, password);
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        freezeCheck();
        this.transport = Transport.valueOf(transport.replaceAll(" ", "_")
            .replaceAll("-", "_")
            .toUpperCase(Locale.ENGLISH));
    }

    public void setTransport(Transport transport) {
        freezeCheck();
        this.transport = transport;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        freezeCheck();
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        freezeCheck();
        this.port = port;
    }

    public Boolean isAuth() {
        return auth != null && auth;
    }

    public void setAuth(Boolean auth) {
        freezeCheck();
        this.auth = auth;
    }

    public boolean isAuthSet() {
        return auth != null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        freezeCheck();
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        freezeCheck();
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        freezeCheck();
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        freezeCheck();
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        freezeCheck();
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        freezeCheck();
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        freezeCheck();
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        freezeCheck();
        this.message = message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        freezeCheck();
        this.messageTemplate = messageTemplate;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        freezeCheck();
        this.mimeType = MimeType.valueOf(mimeType.replaceAll(" ", "_")
            .replaceAll("-", "_")
            .toUpperCase(Locale.ENGLISH));
    }

    public void setMimeType(MimeType mimeType) {
        freezeCheck();
        this.mimeType = mimeType;
    }

    public Map<String, String> getProperties() {
        return freezeWrap(properties);
    }

    public void setProperties(Map<String, String> properties) {
        freezeCheck();
        this.properties.putAll(properties);
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("transport", transport);
        props.put("host", host);
        props.put("port", port);
        props.put("auth", isAuth());
        props.put("username", username);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
        props.put("from", from);
        props.put("to", to);
        props.put("cc", cc);
        props.put("bcc", bcc);
        props.put("subject", subject);
        props.put("message", message);
        props.put("messageTemplate", messageTemplate);
        props.put("mimeType", mimeType);
        props.put("properties", properties);
    }

    public enum MimeType {
        TEXT("text/plain"),
        HTML("text/html");

        private final String code;

        MimeType(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }
    }

    public enum Transport {
        SMTP,
        SMTPS
    }
}
