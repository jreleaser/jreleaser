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
            errors.configuration(RB.$("validation_discussions_enabled"));
            discussions.disable();
            return;
        }

        if (isBlank(discussions.getOrganization())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "discussions.organization"));
        }

        if (isBlank(discussions.getTeam())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "discussions.team"));
        }

        if (isBlank(discussions.getTitle())) {
            discussions.setTitle(RB.$("default.discussion.title"));
        }

        if (isBlank(discussions.getMessage()) && isBlank(discussions.getMessageTemplate())) {
            discussions.setMessageTemplate("src/jreleaser/templates/discussions.tpl");
        }

        if (isBlank(discussions.getMessage()) && isBlank(discussions.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_DISCUSSIONS_TPL))) {
                discussions.setMessageTemplate(DEFAULT_DISCUSSIONS_TPL);
            } else {
                discussions.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(discussions.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(discussions.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "discussions.messageTemplate", discussions.getMessageTemplate()));
        }

        validateTimeout(discussions);
    }
}