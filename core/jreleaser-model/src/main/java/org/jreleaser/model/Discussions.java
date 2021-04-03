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

import org.jreleaser.util.Constants;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.util.MustacheUtils.applyTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Discussions extends AbstractAnnouncer {
    public static final String NAME = "discussions";

    private String organization;
    private String team;
    private String title;
    private String message;
    private String messageTemplate;

    public Discussions() {
        super(NAME);
    }

    void setAll(Discussions discussions) {
        super.setAll(discussions);
        this.organization = discussions.organization;
        this.team = discussions.team;
        this.title = discussions.title;
        this.message = discussions.message;
        this.messageTemplate = discussions.messageTemplate;
    }

    public String getResolvedTitle(JReleaserModel model) {
        Map<String, Object> props = model.props();
        return applyTemplate(new StringReader(title), props);
    }

    public String getResolvedMessage(JReleaserModel model) {
        Map<String, Object> props = model.props();
        return applyTemplate(new StringReader(message), props);
    }

    public String getResolvedMessageTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.getModel().props();
        props.put(Constants.KEY_TAG_NAME, context.getModel().getRelease().getGitService()
            .getEffectiveTagName(context.getModel().getProject()));
        props.putAll(extraProps);

        Path templatePath = context.getBasedir().resolve(messageTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error reading template " +
                context.getBasedir().relativize(templatePath));
        }
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        props.put("organization", organization);
        props.put("title", title);
        props.put("message", message);
        props.put("messageTemplate", messageTemplate);
    }
}
