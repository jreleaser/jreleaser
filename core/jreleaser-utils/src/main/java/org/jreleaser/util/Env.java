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
package org.jreleaser.util;

import java.util.List;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Env {
    private static final String JRELEASER_PREFIX = "JRELEASER_";

    public static String resolve(String key, String value) {
        if (isNotBlank(value)) {
            return value;
        }
        return System.getenv(JRELEASER_PREFIX + key);
    }

    public static void check(String key, String value, String property, List<String> errors) {
        if (isBlank(value)) {
            String prefixedKey = JRELEASER_PREFIX + key;
            if (isBlank(System.getenv(prefixedKey))) {
                errors.add(property + " must not be blank. Alternatively define a " + prefixedKey + " environment variable.");
            }
        }
    }
}
