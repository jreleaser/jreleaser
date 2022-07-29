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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class Env {
    private static final String JRELEASER_ENV_PREFIX = "JRELEASER_";
    private static final String JRELEASER_SYS_PREFIX = "jreleaser.";

    public static String toVar(String str) {
        return str.replaceAll(" ", "_")
            .replaceAll("-", "_")
            .toUpperCase(Locale.ENGLISH);
    }

    public static String envKey(String key) {
        if (!key.startsWith(JRELEASER_ENV_PREFIX)) {
            key = JRELEASER_ENV_PREFIX + key;
        }
        return key.replace(".", "_")
            .toUpperCase(Locale.ENGLISH);
    }

    public static String sysKey(String key) {
        if (!key.startsWith(JRELEASER_SYS_PREFIX)) {
            key = JRELEASER_SYS_PREFIX + key;
        }
        return key.replace("_", ".")
            .toLowerCase(Locale.ENGLISH);
    }

    public static String env(String key, String value) {
        if (isNotBlank(value)) {
            return value;
        }
        return System.getenv(envKey(key));
    }

    public static String env(Collection<String> keys, String value) {
        if (isNotBlank(value)) {
            return value;
        }

        return keys.stream()
            .map(Env::envKey)
            .filter(key -> System.getenv().containsKey(key))
            .map(System::getenv)
            .findFirst()
            .orElse(null);
    }

    public static String sys(String key, String value) {
        if (isNotBlank(value)) {
            return value;
        }
        return System.getProperty(sysKey(key));
    }

    public static String sys(Collection<String> keys, String value) {
        if (isNotBlank(value)) {
            return value;
        }

        return keys.stream()
            .map(Env::sysKey)
            .filter(key -> System.getProperties().containsKey(key))
            .map(System::getProperty)
            .findFirst()
            .orElse(null);
    }

    public static String resolve(String key, String value) {
        return env(key, sys(key, value));
    }

    public static String resolveOrDefault(String key, String value, String defaultValue) {
        String result = env(key, sys(key, value));
        return isNotBlank(result) ? result : defaultValue;
    }

    public static String check(String key, String value, String property, String dsl, String configFilePath, Errors errors) {
        if (isBlank(value)) {
            String prefixedKey = envKey(key);
            value = System.getenv(prefixedKey);
            if (isBlank(value)) {
                errors.configuration(RB.$("ERROR_environment_property_check",
                    property, dsl, prefixedKey, configFilePath, prefixedKey));
            }
        }

        return value;
    }

    public static String check(Collection<String> keys, Properties values, String property, String dsl, String configFilePath, Errors errors) {
        List<String> prefixedKeys = keys.stream()
            .map(Env::envKey)
            .collect(Collectors.toList());

        String value = prefixedKeys.stream()
            .filter(values::containsKey)
            .map(values::getProperty)
            .findFirst()
            .orElse(null);

        if (isBlank(value)) {
            errors.configuration(RB.$("ERROR_environment_property_check2",
                property, dsl, prefixedKeys, configFilePath, prefixedKeys));
        }

        return value;
    }
}
