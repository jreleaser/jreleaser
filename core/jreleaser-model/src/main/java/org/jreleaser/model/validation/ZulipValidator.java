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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Zulip;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.Zulip.ZULIP_API_KEY;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class ZulipValidator extends Validator {
    private static final String DEFAULT_ZULIP_TPL = "src/jreleaser/templates/zulip.tpl";

    public static void validateZulip(JReleaserContext context, Zulip zulip, Errors errors) {
        if (!zulip.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.zulip");

        if (isBlank(zulip.getAccount())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "zulip.account"));
        }

        zulip.setApiKey(
            checkProperty(context,
                ZULIP_API_KEY,
                "zulip.apiKey",
                zulip.getApiKey(),
                errors,
                context.isDryrun()));

        if (isBlank(zulip.getApiHost())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "zulip.apiHost"));
        }
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