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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Mattermost;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.Mattermost.MATTERMOST_WEBHOOK;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class MattermostValidator extends Validator {
    private static final String DEFAULT_MATTERMOST_TPL = "src/jreleaser/templates/mattermost.tpl";

    public static void validateMattermost(JReleaserContext context, Mattermost mattermost, Errors errors) {
        if (!mattermost.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.mattermost");

        mattermost.setWebhook(
            checkProperty(context,
                MATTERMOST_WEBHOOK,
                "mattermost.webhook",
                mattermost.getWebhook(),
                errors,
                context.isDryrun()));

        if (isBlank(mattermost.getMessage()) && isBlank(mattermost.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_MATTERMOST_TPL))) {
                mattermost.setMessageTemplate(DEFAULT_MATTERMOST_TPL);
            } else {
                mattermost.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(mattermost.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(mattermost.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "mattermost.messageTemplate", mattermost.getMessageTemplate()));
        }

        validateTimeout(mattermost);
    }
}