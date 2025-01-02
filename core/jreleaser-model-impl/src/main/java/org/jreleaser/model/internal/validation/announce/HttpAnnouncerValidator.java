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
import org.jreleaser.model.Http;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.HttpAnnouncer;
import org.jreleaser.model.internal.announce.HttpAnnouncers;
import org.jreleaser.model.internal.validation.common.HttpValidator;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class HttpAnnouncerValidator {
    private static final String DEFAULT_TPL = "src/jreleaser/templates";

    private HttpAnnouncerValidator() {
        // noop
    }

    public static void validateHttpAnnouncers(JReleaserContext context, Mode mode, HttpAnnouncers http, Errors errors) {
        context.getLogger().debug("announce.http");

        Map<String, HttpAnnouncer> ha = http.getHttp();

        boolean enabled = false;
        for (Map.Entry<String, HttpAnnouncer> e : ha.entrySet()) {
            e.getValue().setName(e.getKey());
            if ((mode.validateConfig() || mode.validateAnnounce()) && validateHttpAnnouncer(context, e.getValue(), errors)) {
                enabled = true;
            }
        }

        if (enabled) {
            http.setActive(Active.ALWAYS);
        } else {
            http.setActive(Active.NEVER);
        }

        if (!http.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
        }
    }

    public static boolean validateHttpAnnouncer(JReleaserContext context, HttpAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.http." + announcer.getName());
        resolveActivatable(context, announcer,
            listOf("announce.http." + announcer.getName(), "announce.http"),
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

        if (isBlank(announcer.getUrl())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "http." + announcer.getName() + ".url"));
        }

        if (null == announcer.getMethod()) {
            announcer.setMethod(Http.Method.PUT);
        }

        HttpValidator.validateHttp(context, announcer, "announce", announcer.getName(), errors);

        String defaultPayloadTemplate = DEFAULT_TPL + "/http/" + announcer.getName() + ".tpl";
        if (isBlank(announcer.getPayload()) && isBlank(announcer.getPayloadTemplate())) {
            if (Files.exists(context.getBasedir().resolve(defaultPayloadTemplate))) {
                announcer.setPayloadTemplate(defaultPayloadTemplate);
            } else {
                announcer.setPayload(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(announcer.getPayloadTemplate()) &&
            !defaultPayloadTemplate.equals(announcer.getPayloadTemplate().trim()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getPayloadTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist",
                "http." + announcer.getName() + ".payloadTemplate", announcer.getPayloadTemplate()));
        }

        validateTimeout(announcer);

        return true;
    }
}