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

import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Webhook;
import org.jreleaser.model.Webhooks;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public abstract class WebhooksValidator extends Validator {
    private static final String DEFAULT_TPL = "src/jreleaser/templates/";

    public static void validateWebhooks(JReleaserContext context, Webhooks webhooks, Errors errors) {
        context.getLogger().debug("announce.webhooks");

        Map<String, Webhook> webhook = webhooks.getWebhooks();

        boolean enabled = false;
        for (Map.Entry<String, Webhook> e : webhook.entrySet()) {
            e.getValue().setName(e.getKey());
            if (validateWebhook(context, webhooks, e.getValue(), errors)) {
                enabled = true;
            }
        }

        if (enabled) {
            webhooks.setActive(Active.ALWAYS);
            webhooks.resolveEnabled(context.getModel().getProject());
        }
    }

    public static boolean validateWebhook(JReleaserContext context, Webhooks webhooks, Webhook webhook, Errors errors) {
        if (!webhook.resolveEnabled(context.getModel().getProject())) return false;
        if (isBlank(webhook.getName())) {
            webhook.disable();
            return false;
        }

        context.getLogger().debug("announce.webhook." + webhook.getName());

        webhook.setWebhook(
            checkProperty(context,
                Env.toVar(webhook.getName()) + "_WEBHOOK",
                "webhook." + webhook.getName() + ".webhook",
                webhook.getWebhook(),
                errors,
                context.isDryrun()));

        if (isBlank(webhook.getMessage()) && isBlank(webhook.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_TPL + webhook.getName() + ".tpl"))) {
                webhook.setMessageTemplate(DEFAULT_TPL + webhook.getName() + ".tpl");
            } else {
                webhook.setMessage("\uD83D\uDE80 {{projectNameCapitalized}} {{projectVersion}} has been released! {{releaseNotesUrl}}");
            }
        }

        if (isNotBlank(webhook.getMessage()) && isBlank(webhook.getMessageProperty())) {
            webhook.setMessageProperty("text");
        }

        if (isNotBlank(webhook.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(webhook.getMessageTemplate().trim()))) {
            errors.configuration("webhook." + webhook.getName() + ".messageTemplate does not exist. " + webhook.getMessageTemplate());
        }

        if (webhook.getConnectTimeout() <= 0 || webhook.getConnectTimeout() > 300) {
            webhook.setConnectTimeout(20);
        }

        if (webhook.getReadTimeout() <= 0 || webhook.getReadTimeout() > 300) {
            webhook.setReadTimeout(60);
        }

        return true;
    }
}