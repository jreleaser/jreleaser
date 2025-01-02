/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
import org.jreleaser.model.internal.announce.ZulipAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.ZulipAnnouncer.ZULIP_API_KEY;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class ZulipAnnouncerValidator {
    private static final String DEFAULT_ZULIP_TPL = "src/jreleaser/templates/zulip.tpl";

    private ZulipAnnouncerValidator() {
        // noop
    }

    public static void validateZulip(JReleaserContext context, ZulipAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.zulip");
        resolveActivatable(context, announcer, "announce.zulip", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        announcer.setAccount(
            checkProperty(context,
                listOf(
                    "announce.zulip.account",
                    "zulip.account"),
                "announce.zulip.account",
                announcer.getAccount(),
                errors));

        announcer.setApiKey(
            checkProperty(context,
                listOf(
                    "announce.zulip.api.key",
                    ZULIP_API_KEY),
                "announce.zulip.apiKey",
                announcer.getApiKey(),
                errors,
                context.isDryrun()));

        announcer.setApiHost(
            checkProperty(context,
                listOf(
                    "announce.zulip.api.host",
                    "zulip.api.host"),
                "announce.zulip.apiHost",
                announcer.getApiHost(),
                errors));

        if (isBlank(announcer.getSubject())) {
            announcer.setSubject(RB.$("default.discussion.title"));
        }
        if (isBlank(announcer.getChannel())) {
            announcer.setChannel("announce");
        }

        if (isBlank(announcer.getMessage()) && isBlank(announcer.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_ZULIP_TPL))) {
                announcer.setMessageTemplate(DEFAULT_ZULIP_TPL);
            } else {
                announcer.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(announcer.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "zulip.messageTemplate", announcer.getMessageTemplate()));
        }

        validateTimeout(announcer);
    }
}