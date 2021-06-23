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

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Mastodon;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.Mastodon.MASTODON_ACCESS_TOKEN;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class MastodonValidator extends Validator {
    public static void validateMastodon(JReleaserContext context, Mastodon mastodon, Errors errors) {
        if (!mastodon.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.mastodon");

        if (isBlank(mastodon.getHost())) {
            errors.configuration("mastodon.host must not be blank.");
        }

        mastodon.setAccessToken(
            checkProperty(context.getModel().getEnvironment(),
                MASTODON_ACCESS_TOKEN,
                "mastodon.accessToken",
                mastodon.getAccessToken(),
                errors,
                context.isDryrun()));

        if (isBlank(mastodon.getStatus())) {
            mastodon.setStatus("\uD83D\uDE80 {{projectNameCapitalized}} {{projectVersion}} has been released! {{releaseNotesUrl}}");
        }

        if (mastodon.getConnectTimeout() <= 0 || mastodon.getConnectTimeout() > 300) {
            mastodon.setConnectTimeout(20);
        }
        if (mastodon.getReadTimeout() <= 0 || mastodon.getReadTimeout() > 300) {
            mastodon.setReadTimeout(60);
        }
    }
}