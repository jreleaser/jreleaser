/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.maven.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Mail extends AbstractAnnouncer {
    public static final String NAME = "mail";
    private final Map<String, String> properties = new LinkedHashMap<>();

    private Transport transport = Transport.SMTP;
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
    private MimeType mimeType = MimeType.TEXT;

    public Mail() {
        super(NAME);
    }

    void setAll(Mail mail) {
        super.setAll(mail);
        this.transport = mail.transport;
        this.host = mail.host;
        this.port = mail.port;
        this.auth = mail.auth;
        this.username = mail.username;
        this.password = mail.password;
        this.from = mail.from;
        this.to = mail.to;
        this.cc = mail.cc;
        this.bcc = mail.bcc;
        this.subject = mail.subject;
        this.message = mail.message;
        this.messageTemplate = mail.messageTemplate;
        this.mimeType = mail.mimeType;
        setProperties(mail.properties);
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean isAuth() {
        return auth != null && auth;
    }

    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    public boolean isAuthSet() {
        return auth != null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            transport != null ||
            auth != null ||
            mimeType != null ||
            port != null ||
            isNotBlank(host) ||
            isNotBlank(username) ||
            isNotBlank(password) ||
            isNotBlank(from) ||
            isNotBlank(to) ||
            isNotBlank(cc) ||
            isNotBlank(bcc) ||
            isNotBlank(subject) ||
            isNotBlank(message) ||
            isNotBlank(subject) ||
            isNotBlank(message) ||
            isNotBlank(messageTemplate) ||
            !properties.isEmpty();
    }

    public enum MimeType {
        TEXT,
        HTML
    }

    public enum Transport {
        SMTP,
        SMTPS
    }
}
