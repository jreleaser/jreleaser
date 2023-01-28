/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.internal.announce.MattermostAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.MattermostAnnouncer.MATTERMOST_WEBHOOK;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public final class MattermostAnnouncerValidator {
    private static final String DEFAULT_MATTERMOST_TPL = "src/jreleaser/templates/mattermost.tpl";

    private MattermostAnnouncerValidator() {
        // noop
    }

    public static void validateMattermost(JReleaserContext context, MattermostAnnouncer mattermost, Errors errors) {
        context.getLogger().debug("announce.mattermost");
        resolveActivatable(context, mattermost, "announce.mattermost", "NEVER");
        if (!mattermost.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        mattermost.setWebhook(
            checkProperty(context,
                listOf(
                    "announce.mattermost.webhook",
                    MATTERMOST_WEBHOOK),
                "announce.mattermost.webhook",
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