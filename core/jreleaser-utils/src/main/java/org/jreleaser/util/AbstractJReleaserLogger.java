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

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractJReleaserLogger implements JReleaserLogger {
    private String indent = "";
    private String prefix = null;
    private String previousPrefix = null;

    @Override
    public void setPrefix(String prefix) {
        this.previousPrefix = this.prefix;
        this.prefix = prefix;
    }

    @Override
    public void restorePrefix() {
        this.prefix = this.previousPrefix;
    }

    @Override
    public void increaseIndent() {
        indent += "  ";
    }

    @Override
    public void decreaseIndent() {
        if (indent.length() > 0) {
            indent = indent.substring(0, indent.length() - 2);
        }
    }

    protected String formatMessage(String message) {
        return indent + (prefix != null ? "[" + prefix + "] " : "") + message;
    }
}
