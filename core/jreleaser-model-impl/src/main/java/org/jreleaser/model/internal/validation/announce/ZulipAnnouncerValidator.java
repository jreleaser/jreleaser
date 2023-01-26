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

    public static void validateZulip(JReleaserContext context, ZulipAnnouncer zulip, Errors errors) {
        context.getLogger().debug("announce.zulip");
        resolveActivatable(context, zulip, "announce.zulip", "NEVER");
        if (!zulip.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        zulip.setAccount(
            checkProperty(context,
                listOf(
                    "announce.zulip.account",
                    "zulip.account"),
                "announce.zulip.account",
                zulip.getAccount(),
                errors));

        zulip.setApiKey(
            checkProperty(context,
                listOf(
                    "announce.zulip.api.key",
                    ZULIP_API_KEY),
                "announce.zulip.apiKey",
                zulip.getApiKey(),
                errors,
                context.isDryrun()));

        zulip.setApiHost(
            checkProperty(context,
                listOf(
                    "announce.zulip.api.host",
                    "zulip.api.host"),
                "announce.zulip.apiHost",
                zulip.getApiHost(),
                errors));

        if (isBlank(zulip.getSubject())) {
            zulip.setSubject(RB.$("default.discussion.title"));
        }
        if (isBlank(zulip.getChannel())) {
            zulip.setChannel("announce");
        }

        if (isBlank(zulip.getMessage()) && isBlank(zulip.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_ZULIP_TPL))) {
                zulip.setMessageTemplate(DEFAULT_ZULIP_TPL);
            } else {
                zulip.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(zulip.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(zulip.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "zulip.messageTemplate", zulip.getMessageTemplate()));
        }

        validateTimeout(zulip);
    }
}