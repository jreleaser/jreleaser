/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.jreleaser.model.Sdkman;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.Sdkman.SDKMAN_CONSUMER_KEY;
import static org.jreleaser.model.Sdkman.SDKMAN_CONSUMER_TOKEN;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class SdkmanValidator extends Validator {
    public static void validateSdkman(JReleaserContext context, Sdkman sdkman, Errors errors) {
        if (!sdkman.resolveEnabled(context.getModel().getProject())) return;
        if (!context.getModel().getRelease().getGitService().isReleaseSupported()) {
            sdkman.disable();
            return;
        }

        context.getLogger().debug("announce.sdkman");

        sdkman.setConsumerKey(
            checkProperty(context.getModel().getEnvironment(),
                SDKMAN_CONSUMER_KEY,
                "sdkman.consumerKey",
                sdkman.getConsumerKey(),
                errors,
                context.isDryrun()));

        sdkman.setConsumerToken(
            checkProperty(context.getModel().getEnvironment(),
                SDKMAN_CONSUMER_TOKEN,
                "sdkman.consumerToken",
                sdkman.getConsumerToken(),
                errors,
                context.isDryrun()));

        if (context.getModel().getActiveDistributions().isEmpty()) {
            context.getLogger().warn("There are no active distributions. Disabling Sdkman announcer");
            sdkman.disable();
        }

        if (sdkman.getConnectTimeout() <= 0 || sdkman.getConnectTimeout() > 300) {
            sdkman.setConnectTimeout(20);
        }
        if (sdkman.getReadTimeout() <= 0 || sdkman.getReadTimeout() > 300) {
            sdkman.setReadTimeout(60);
        }
    }
}