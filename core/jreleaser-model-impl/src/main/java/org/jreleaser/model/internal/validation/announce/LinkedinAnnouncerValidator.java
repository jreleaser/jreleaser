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
import org.jreleaser.model.internal.announce.LinkedinAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.LinkedinAnnouncer.LINKEDIN_ACCESS_TOKEN;
import static org.jreleaser.model.api.announce.LinkedinAnnouncer.LINKEDIN_OWNER;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class LinkedinAnnouncerValidator {
    private static final String DEFAULT_LINKEDIN_TPL = "src/jreleaser/templates/linkedin.tpl";

    private LinkedinAnnouncerValidator() {
        // noop
    }

    public static void validateLinkedin(JReleaserContext context, LinkedinAnnouncer linkedin, Errors errors) {
        context.getLogger().debug("announce.linkedin");
        resolveActivatable(context, linkedin, "announce.linkedin", "NEVER");
        if (!linkedin.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        linkedin.setOwner(
            checkProperty(context,
                listOf(
                    "announce.linkedin.owner",
                    LINKEDIN_OWNER),
                "announce.linkedin.owner",
                linkedin.getOwner(),
                errors,
                true)); // optional

        linkedin.setAccessToken(
            checkProperty(context,
                listOf(
                    "announce.linkedin.access.token",
                    LINKEDIN_ACCESS_TOKEN),
                "announce.linkedin.accessToken",
                linkedin.getAccessToken(),
                errors,
                context.isDryrun()));

        if (isBlank(linkedin.getSubject())) {
            linkedin.setSubject("{{projectNameCapitalized}} {{projectVersion}} released");
        }

        if (isBlank(linkedin.getMessage()) && isBlank(linkedin.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_LINKEDIN_TPL))) {
                linkedin.setMessageTemplate(DEFAULT_LINKEDIN_TPL);
            } else {
                linkedin.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(linkedin.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(linkedin.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "linkedin.messageTemplate", linkedin.getMessageTemplate()));
        }

        validateTimeout(linkedin);
    }
}