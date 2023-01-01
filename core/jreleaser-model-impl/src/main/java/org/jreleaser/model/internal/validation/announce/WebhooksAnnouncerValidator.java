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
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.WebhookAnnouncer;
import org.jreleaser.model.internal.announce.WebhooksAnnouncer;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
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

        if (!webhooks.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
        }
    }

    public static boolean validateWebhook(JReleaserContext context, WebhookAnnouncer webhook, Errors errors) {
        context.getLogger().debug("announce.webhook." + webhook.getName());
        if (!webhook.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return false;
        }
        if (isBlank(webhook.getName())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            webhook.disable();
            return false;
        }

        webhook.setWebhook(
            checkProperty(context,
                Env.toVar(webhook.getName()) + "_WEBHOOK",
                "announce.webhooks." + webhook.getName() + ".webhook",
                webhook.getWebhook(),
                errors,
                context.isDryrun()));

        String defaultMessageTemplate = DEFAULT_TPL + webhook.getName() + ".tpl";
        if (isBlank(webhook.getMessage()) && isBlank(webhook.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(defaultMessageTemplate))) {
                webhook.setMessageTemplate(defaultMessageTemplate);
            } else {
                webhook.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(webhook.getMessage()) && isBlank(webhook.getMessageProperty())) {
            webhook.setMessageProperty("text");
        }

        if (isNotBlank(webhook.getMessageTemplate()) &&
            !defaultMessageTemplate.equals(webhook.getMessageTemplate().trim()) &&
            !Files.exists(context.getBasedir().resolve(webhook.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist",
                "webhook." + webhook.getName() + ".messageTemplate", webhook.getMessageTemplate()));
        }

        validateTimeout(webhook);

        return true;
    }
}