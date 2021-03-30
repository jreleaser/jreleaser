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

import java.util.List;

import static org.jreleaser.model.Sdkman.SDKMAN_CONSUMER_KEY;
import static org.jreleaser.model.Sdkman.SDKMAN_CONSUMER_TOKEN;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class SdkmanValidator extends Validator {
    public static void validateSdkman(JReleaserContext context, Sdkman sdkman, List<String> errors) {
        if (!sdkman.isEnabled()) return;

        sdkman.setConsumerKey(
            checkProperty(context.getModel().getEnvironment(),
                SDKMAN_CONSUMER_KEY,
                "sdkman.consumerKey",
                sdkman.getConsumerKey(),
                errors));

        sdkman.setConsumerToken(
            checkProperty(context.getModel().getEnvironment(),
                SDKMAN_CONSUMER_TOKEN,
                "sdkman.consumerToken",
                sdkman.getConsumerToken(),
                errors));

        if (context.getModel().getDistributions().isEmpty()) {
            context.getLogger().warn("There are no configured distributions. Disabling Sdkman announcement");
            sdkman.setEnabled(false);
        }
    }
}