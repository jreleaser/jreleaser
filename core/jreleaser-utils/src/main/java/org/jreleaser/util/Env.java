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
package org.jreleaser.util;

import org.jreleaser.bundle.RB;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class Env {
    private static final String JRELEASER_PREFIX = "JRELEASER_";

    public static String toVar(String str) {
        return str.replaceAll(" ", "_")
            .replaceAll("-", "_")
            .toUpperCase();
    }

    public static String prefix(String key) {
        if (!key.startsWith(JRELEASER_PREFIX)) {
            return JRELEASER_PREFIX + key;
        }
        return key;
    }

    public static String resolve(String key, String value) {
        if (isNotBlank(value)) {
            return value;
        }
        return System.getenv(prefix(key));
    }

    public static String check(String key, String value, String property, String dsl, String configFilePath, Errors errors) {
        if (isBlank(value)) {
            String prefixedKey = prefix(key);
            value = System.getenv(prefixedKey);
            if (isBlank(value)) {
                errors.configuration(RB.$("ERROR_environment_property_check",
                    property, dsl, prefixedKey, configFilePath, prefixedKey));
            }
        }

        return value;
    }
}
