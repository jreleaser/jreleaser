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
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.WebhookAnnouncer;
import org.jreleaser.model.internal.announce.WebhooksAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public final class WebhooksAnnouncerValidator {
    private static final String DEFAULT_TPL = "src/jreleaser/templates/";

    private WebhooksAnnouncerValidator() {
        // noop
    }

    public static void validateWebhooks(JReleaserContext context, Mode mode, WebhooksAnnouncer webhooks, Errors errors) {
        context.getLogger().debug("announce.webhooks");

        Map<String, WebhookAnnouncer> webhook = webhooks.getWebhooks();

        boolean enabled = false;
        for (Map.Entry<String, WebhookAnnouncer> e : webhook.entrySet()) {
            e.getValue().setName(e.getKey());
            if ((mode.validateConfig() || mode.validateAnnounce()) && validateWebhook(context, e.getValue(), errors)) {
                enabled = true;
            }
        }

        if (enabled) {
            webhooks.setActive(Active.ALWAYS);
        } else {
            webhooks.setActive(Active.NEVER);
        }

        if (!webhooks.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
        }
    }

    public static boolean validateWebhook(JReleaserContext context, WebhookAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.webhooks." + announcer.getName());
        resolveActivatable(context, announcer,
            listOf("announce.webhooks." + announcer.getName(), "announce.webhooks"),
            "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return false;
        }
        if (isBlank(announcer.getName())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            announcer.disable();
            return false;
        }

        announcer.setWebhook(
            checkProperty(context,
                listOf(
                    "announce.webhooks." + announcer.getName() + ".webhook",
                    announcer.getName() + ".webhook"),
                "announce.webhooks." + announcer.getName() + ".webhook",
                announcer.getWebhook(),
                errors,
                context.isDryrun()));

        String defaultMessageTemplate = DEFAULT_TPL + announcer.getName() + ".tpl";
        if (isBlank(announcer.getMessage()) && isBlank(announcer.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(defaultMessageTemplate))) {
                announcer.setMessageTemplate(defaultMessageTemplate);
            } else {
                announcer.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(announcer.getMessage()) && isBlank(announcer.getMessageProperty())) {
            announcer.setMessageProperty("text");
        }

        if (isNotBlank(announcer.getMessageTemplate()) &&
            !defaultMessageTemplate.equals(announcer.getMessageTemplate().trim()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist",
                "webhook." + announcer.getName() + ".messageTemplate", announcer.getMessageTemplate()));
        }

        validateTimeout(announcer);

        return true;
    }
}