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
 * @since 0.8.0
 */
public class Archive {
    public enum Format {
        ZIP("zip"),
        TAR("tar"),
        TAR_BZ2("tar.bz2"),
        TAR_GZ("tar.gz"),
        TAR_XZ("tar.xz"),
        TAR_ZST("tar.zst"),
        TBZ2("tbz2"),
        TGZ("tgz"),
        TXZ("txz");

        private final String extension;

        Format(String extension) {
            this.extension = extension;
        }

        public String extension() {
            return this.extension;
        }

        public String formatted() {
            return extension();
        }

        public static Format of(String str) {
            if (isBlank(str)) return null;
            return valueOf(str.toUpperCase(Locale.ENGLISH).trim()
                .replace(".", "_"));
        }
    }
}
