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
package org.jreleaser.model.validation;

import org.jreleaser.model.Discussions;
import org.jreleaser.model.Github;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class DiscussionsValidator extends Validator {
    private static final String DEFAULT_DISCUSSIONS_TPL = "src/jreleaser/templates/discussions.tpl";

    public static void validateDiscussions(JReleaserContext context, Discussions discussions, Errors errors) {
        if (!discussions.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.discussions");

        if (!Github.NAME.equals(context.getModel().getRelease().getGitService().getServiceName())) {
            errors.configuration("discussions may only be used when releasing to GitHub");
            discussions.disable();
            return;
        }

        if (isBlank(discussions.getOrganization())) {
            errors.configuration("discussions.organization must not be blank.");
        }

        if (isBlank(discussions.getTeam())) {
            errors.configuration("discussions.team must not be blank.");
        }

        if (isBlank(discussions.getTitle())) {
            discussions.setTitle("{{projectNameCapitalized}} {{projectVersion}} released!");
        }

        if (isBlank(discussions.getMessage()) && isBlank(discussions.getMessageTemplate())) {
            discussions.setMessageTemplate("src/jreleaser/templates/discussions.tpl");
        }

        if (isBlank(discussions.getMessage()) && isBlank(discussions.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_DISCUSSIONS_TPL))) {
                discussions.setMessageTemplate(DEFAULT_DISCUSSIONS_TPL);
            } else {
                discussions.setMessage("\uD83D\uDE80 {{projectNameCapitalized}} {{projectVersion}} has been released! {{releaseNotesUrl}}");
            }
        }

        if (isNotBlank(discussions.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(discussions.getMessageTemplate().trim()))) {
            errors.configuration("discussions.messageTemplate does not exist. " + discussions.getMessageTemplate());
        }

        if (discussions.getConnectTimeout() <= 0 || discussions.getConnectTimeout() > 300) {
            discussions.setConnectTimeout(context.getModel().getRelease().getGitService().getConnectTimeout());
        }
        if (discussions.getReadTimeout() <= 0 || discussions.getReadTimeout() > 300) {
            discussions.setReadTimeout(context.getModel().getRelease().getGitService().getReadTimeout());
        }
    }
}