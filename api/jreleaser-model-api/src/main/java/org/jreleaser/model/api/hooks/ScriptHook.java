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
package org.jreleaser.model.api.hooks;

import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public interface ScriptHook extends Hook {
    String getRun();

    Shell getShell();

    enum Shell {
        BASH(".sh", "bash --noprofile --norc -eo pipefail {{script}}"),
        SH(".sh", "sh -e {{script}}"),
        CMD(".cmd", "%ComSpec% /D /E:ON /V:OFF /S /C \"CALL \"{{script}}\"\""),
        PWSH(".ps1", "pwsh -command \". '{{script}}'\""),
        POWERSHELL(".ps1", "powershell -command \". '{{script}}'\"");

        private final String extension;
        private final String expression;

        Shell(String extension, String expression) {
            this.extension = extension;
            this.expression = expression;
        }

        public String extension() {
            return extension;
        }

        public String expression() {
            return expression;
        }

        public static Shell of(String str) {
            if (isBlank(str)) return null;
            return Shell.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
        }
    }
}
