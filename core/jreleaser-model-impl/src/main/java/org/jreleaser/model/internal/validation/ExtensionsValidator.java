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
package org.jreleaser.model.internal.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.extensions.Extension;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public abstract class ExtensionsValidator extends Validator {
    public static void validateExtensions(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, Extension> extensions = context.getModel().getExtensions();
        if (!extensions.isEmpty()) context.getLogger().debug("extensions");

        for (Map.Entry<String, Extension> e : extensions.entrySet()) {
            Extension extension = e.getValue();
            if (isBlank(extension.getName())) {
                extension.setName(e.getKey());
            }
            validateExtension(context, mode, extension, errors);
        }
    }

    private static void validateExtension(JReleaserContext context, Mode mode, Extension extension, Errors errors) {
        context.getLogger().debug("extension.{}", extension.getName());

        if (!extension.isEnabledSet()) {
            extension.setEnabled(true);
        }
        if (!extension.isEnabled()) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (isNotBlank(extension.getDirectory()) &&
            !Files.exists(context.getBasedir().resolve(extension.getDirectory().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist",
                "extension." + extension.getName() + ".directory", extension.getDirectory()));
        }

        for (int i = 0; i < extension.getProviders().size(); i++) {
            validateExtensionProvider(context, mode, extension, extension.getProviders().get(i), i, errors);
        }
    }

    private static void validateExtensionProvider(JReleaserContext context, Mode mode, Extension extension, Extension.Provider provider, int index, Errors errors) {
        if (isBlank(provider.getType())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "extensions.{}.providers[{}]", extension.getName(), index));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            extension.setEnabled(false);
        }
    }
}
