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
package org.jreleaser.mustache;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;

import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public final class Templates {
    private Templates() {
        // noop
    }

    public static String resolveTemplate(String input, TemplateContext props) {
        if (isBlank(input)) return input;

        int count = 0;

        while (input.contains("{{")) {
            input = applyTemplate(input, props);
            count++;

            if (input.contains("{{") && count >= 10) {
                throw new JReleaserException(RB.$("ERROR_input_can_not_resolve", input));
            }
        }

        return input;
    }
}
