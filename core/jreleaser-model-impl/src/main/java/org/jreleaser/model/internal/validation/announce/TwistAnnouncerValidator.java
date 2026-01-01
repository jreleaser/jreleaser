/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
import org.jreleaser.model.internal.announce.TwistAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.TwistAnnouncer.TWIST_ACCESS_TOKEN;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Usman Shaikh
 * @since 1.23.0
 */
public final class TwistAnnouncerValidator {
    private TwistAnnouncerValidator() {
        // noop
    }

    public static void validateTwist(JReleaserContext context, TwistAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.twist");
        resolveActivatable(context, announcer, "announce.twist", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        announcer.setAccessToken(
            checkProperty(context,
                listOf(
                    "announce.twist.access.token",
                    TWIST_ACCESS_TOKEN),
                "announce.twist.accessToken",
                announcer.getAccessToken(),
                errors,
                context.isDryrun()));

        if (isBlank(announcer.getChannelId()) && isBlank(announcer.getThreadId())) {
            errors.configuration(RB.$("validation_must_not_be_null", "announce.twist.channelId or announce.twist.threadId"));
            announcer.disable();
            return;
        }

        if (isNotBlank(announcer.getChannelId()) && isNotBlank(announcer.getThreadId())) {
            errors.configuration(RB.$("validation_exclusive_set", "announce.twist.channelId", "announce.twist.threadId"));
            announcer.disable();
            return;
        }

        if (isNotBlank(announcer.getChannelId()) && !isPositiveInteger(announcer.getChannelId())) {
            errors.configuration(RB.$("validation_must_be_positive", "announce.twist.channelId"));
            announcer.disable();
            return;
        }

        if (isNotBlank(announcer.getThreadId()) && !isPositiveInteger(announcer.getThreadId())) {
            errors.configuration(RB.$("validation_must_be_positive", "announce.twist.threadId"));
            announcer.disable();
            return;
        }

        if (isNotBlank(announcer.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "twist.messageTemplate", announcer.getMessageTemplate()));
            announcer.disable();
            return;
        }

        if (isNotBlank(announcer.getChannelId()) && isBlank(announcer.getTitle())) {
            announcer.setTitle(RB.$("default.discussion.title"));
        }

        if (isBlank(announcer.getMessage()) && isBlank(announcer.getMessageTemplate())) {
            announcer.setMessage(RB.$("default.release.message"));
        }

        validateTimeout(announcer);
    }

    private static boolean isPositiveInteger(String id) {
        try {
            return Integer.parseInt(id) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
