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

import org.jreleaser.model.Discussions;
import org.jreleaser.model.Github;
import org.jreleaser.model.JReleaserContext;

import java.nio.file.Files;
import java.util.List;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class DiscussionsValidator extends Validator {
    private static final String DEFAULT_DISCUSSIONS_TPL = "src/jreleaser/templates/discussions.tpl";

    public static void validateDiscussions(JReleaserContext context, Discussions discussions, List<String> errors) {
        if (!discussions.isEnabled()) return;
        context.getLogger().debug("announce.discussions");

        if (!Github.NAME.equals(context.getModel().getRelease().getGitService().getServiceName())) {
            errors.add("discussions may only be used when releasing to GitHub");
            discussions.setEnabled(false);
            return;
        }

        if (isBlank(discussions.getOrganization())) {
            errors.add("discussions.organization must not be blank.");
        }

        if (isBlank(discussions.getTeam())) {
            errors.add("discussions.team must not be blank.");
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
            errors.add("discussions.messageTemplate does not exist. " + discussions.getMessageTemplate());
        }
    }
}