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
package org.jreleaser.model.validation;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Slack;

import java.nio.file.Files;
import java.util.List;

import static org.jreleaser.model.Slack.SLACK_TOKEN;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class SlackValidator extends Validator {
    private static final String DEFAULT_SLACK_TPL = "src/jreleaser/templates/slack.tpl";

    public static void validateSlack(JReleaserContext context, Slack slack, List<String> errors) {
        if (!slack.isEnabled()) return;

        slack.setToken(
            checkProperty(context.getModel().getEnvironment(),
                SLACK_TOKEN,
                "slack.token",
                slack.getToken(),
                errors));


        if (isBlank(slack.getChannel())) {
            slack.setChannel("#announce");
        }

        if (isBlank(slack.getMessage()) && isBlank(slack.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_SLACK_TPL))) {
                slack.setMessageTemplate(DEFAULT_SLACK_TPL);
            } else {
                slack.setMessage("\uD83D\uDE80 {{projectNameCapitalized}} {{projectVersion}} has been released! {{releaseNotesUrl}}");
            }
        }

        if (isNotBlank(slack.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(slack.getMessageTemplate().trim()))) {
            errors.add("slack.messageTemplate does not exist. " + slack.getMessageTemplate());
        }
    }
}