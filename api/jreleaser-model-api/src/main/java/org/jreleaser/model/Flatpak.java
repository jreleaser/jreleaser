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
 * @since 1.2.0
 */
public class Flatpak {
    public enum Runtime {
        FREEDESKTOP("org.freedesktop.Platform", "org.freedesktop.Sdk"),
        GNOME("org.gnome.Platform", "org.gnome.Sdk"),
        KDE("org.kde.Platform", "org.kde.Sdk"),
        ELEMENTARY("io.elementary.Platform", "io.elementary.Sdk");

        private final String runtime;
        private final String sdk;

        Runtime(String runtime, String sdk) {
            this.runtime = runtime;
            this.sdk = sdk;
        }

        public String runtime() {
            return runtime;
        }

        public String sdk() {
            return sdk;
        }

        public String formatted() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Runtime of(String str) {
            if (isBlank(str)) return null;
            return Runtime.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
        }
    }
}
