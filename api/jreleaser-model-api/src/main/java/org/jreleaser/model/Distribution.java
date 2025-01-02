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
package org.jreleaser.model;

import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Distribution {
    public enum DistributionType {
        BINARY("binary"),
        FLAT_BINARY("flat"),
        JAVA_BINARY("java"),
        JLINK("jlink"),
        SINGLE_JAR("uberjar"),
        @Deprecated
        NATIVE_IMAGE("graal"),
        NATIVE_PACKAGE("jpackage");

        private final String alias;

        DistributionType(String alias) {
            this.alias = alias.toUpperCase(Locale.ENGLISH);
        }

        public static DistributionType of(String str) {
            if (isBlank(str)) return null;

            String value = str.replace(" ", "_")
                .replace("-", "_")
                .toUpperCase(Locale.ENGLISH).trim();

            // try alias
            for (DistributionType type : DistributionType.values()) {
                if (type.alias.equals(value)) {
                    return type;
                }
            }

            return DistributionType.valueOf(value);
        }
    }
}
