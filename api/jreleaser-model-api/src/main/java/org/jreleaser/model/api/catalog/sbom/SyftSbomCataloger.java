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
package org.jreleaser.model.api.catalog.sbom;

import java.util.Locale;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public interface SyftSbomCataloger extends SbomCataloger {
    String TYPE = "syft";

    String getVersion();

    Set<Format> getFormats();

    enum Format {
        SYFT_JSON("syft.json"),
        CYCLONEDX_XML("cyclonedx.xml"),
        CYCLONEDX_JSON("cyclonedx.json"),
        GITHUB_JSON("github.json"),
        SPDX_JSON("spdx.json"),
        TABLE("table"),
        TEXT("txt");

        private final String alias;

        Format(String alias) {
            this.alias = alias;
        }

        public String extension() {
            return "." + alias.replace(".", "-") + ".sbom";
        }

        public String formatted() {
            return name().replace("_", "-").toLowerCase(Locale.ROOT);
        }

        public static Format of(String str) {
            if (isBlank(str)) return null;

            String value = str.replace(" ", "")
                .replace("-", ".")
                .replace("_", ".")
                .toUpperCase(Locale.ENGLISH).trim();

            // try alias
            for (Format type : Format.values()) {
                if (type.alias.toLowerCase(Locale.ENGLISH).equals(value)) {
                    return type;
                }
            }

            return Format.valueOf(value
                .replace(".", "_")
                .toUpperCase(Locale.ENGLISH).trim());
        }
    }
}
