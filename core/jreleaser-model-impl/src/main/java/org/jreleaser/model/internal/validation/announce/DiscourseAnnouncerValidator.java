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
package org.jreleaser.model.internal.validation.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.DiscourseAnnouncer;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.DiscourseAnnouncer.*;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author shblue21
 * @since 1.3.0
 */
public abstract class DiscourseAnnouncerValidator extends Validator {
    private static final String DEFAULT_DISCOURSE_TPL = "src/jreleaser/templates/discourse.tpl";

    public static void validateDiscourse(JReleaserContext context, DiscourseAnnouncer discourse, Errors errors) {
        context.getLogger().debug("announce.discourse");
        if (!discourse.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (isBlank(discourse.getHost())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "discourse.host"));
        }

        discourse.setUserName(
                checkProperty(context,
                        DISCOURSE_USERNAME,
                        "discourse.userName",
                        discourse.getUserName(),
                        errors,
                        context.isDryrun()));

        discourse.setApiKey(
                checkProperty(context,
                        DISCOURSE_API_KEY,
                        "discourse.apiKey",
                        discourse.getApiKey(),
                        errors,
                        context.isDryrun()));

        discourse.setCategoryName(
                checkProperty(context,
                        DISCOURSE_CATEGORY_NAME,
                        "discourse.category",
                        discourse.getCategoryName(),
                        errors,
                        context.isDryrun()));
        

        if (isBlank(discourse.getTitle())) {
            discourse.setTitle(RB.$("default.discussion.title"));
        }

        if (isBlank(discourse.getMessage()) && isBlank(discourse.getMessageTemplate())) {
            discourse.setMessageTemplate("src/jreleaser/templates/discussions.tpl");
        }


        if (isBlank(discourse.getMessage()) && isBlank(discourse.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_DISCOURSE_TPL))) {
                discourse.setMessageTemplate(DEFAULT_DISCOURSE_TPL);
            } else {
                discourse.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(discourse.getMessageTemplate()) &&
                !Files.exists(context.getBasedir().resolve(discourse.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "discourse.messageTemplate", discourse.getMessageTemplate()));
        }

        validateTimeout(discourse);
    }
}