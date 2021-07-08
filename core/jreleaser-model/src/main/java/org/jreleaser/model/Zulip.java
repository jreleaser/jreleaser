/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import org.jreleaser.util.Env;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.KEY_TAG_NAME;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Zulip extends AbstractAnnouncer {
    public static final String NAME = "zulip";
    public static final String ZULIP_API_KEY = "ZULIP_API_KEY";

    private String account;
    private String apiKey;
    private String apiHost;
    private String channel;
    private String subject;
    private String message;
    private String messageTemplate;

    public Zulip() {
        super(NAME);
    }

    void setAll(Zulip zulip) {
        super.setAll(zulip);
        this.account = zulip.account;
        this.apiKey = zulip.apiKey;
        this.apiHost = zulip.apiHost;
        this.channel = zulip.channel;
        this.subject = zulip.subject;
        this.message = zulip.message;
        this.messageTemplate = zulip.messageTemplate;
    }

    public String getResolvedSubject(JReleaserContext context) {
        Map<String, Object> props = context.props();
        applyTemplates(props, getResolvedExtraProperties());
        return applyTemplate(subject, props);
    }

    public String getResolvedMessage(JReleaserContext context) {
        Map<String, Object> props = context.props();
        applyTemplates(props, getResolvedExtraProperties());
        return applyTemplate(message, props);
    }

    public String getResolvedMessageTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.props();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getGitService()
            .getEffectiveTagName());
        props.putAll(extraProps);

        Path templatePath = context.getBasedir().resolve(messageTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error reading template " +
                context.relativizeToBasedir(templatePath));
        }
    }

    public String getResolvedApiKey() {
        return Env.resolve(ZULIP_API_KEY, apiKey);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("account", account);
        props.put("apiKey", isNotBlank(getResolvedApiKey()) ? HIDE : UNSET);
        props.put("apiHost", apiHost);
        props.put("channel", channel);
        props.put("subject", subject);
        props.put("message", message);
        props.put("messageTemplate", messageTemplate);
    }
}
