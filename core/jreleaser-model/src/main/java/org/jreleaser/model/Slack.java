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
public class Slack extends AbstractAnnouncer {
    public static final String NAME = "slack";
    public static final String SLACK_TOKEN = "SLACK_TOKEN";
    public static final String SLACK_WEBHOOK = "SLACK_WEBHOOK";

    private String token;
    private String webhook;
    private String channel;
    private String message;
    private String messageTemplate;

    public Slack() {
        super(NAME);
    }

    void setAll(Slack slack) {
        super.setAll(slack);
        this.token = slack.token;
        this.webhook = slack.webhook;
        this.channel = slack.channel;
        this.message = slack.message;
        this.messageTemplate = slack.messageTemplate;
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
            .getEffectiveTagName(context.getModel()));
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

    public String getResolvedToken() {
        return Env.resolve(SLACK_TOKEN, token);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getResolvedWebhook() {
        return Env.resolve(SLACK_WEBHOOK, webhook);
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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
    protected void asMap(Map<String, Object> props) {
        props.put("webhook", isNotBlank(getResolvedWebhook()) ? HIDE : UNSET);
        props.put("token", isNotBlank(getResolvedToken()) ? HIDE : UNSET);
        props.put("channel", channel);
        props.put("message", message);
        props.put("messageTemplate", messageTemplate);
    }
}
