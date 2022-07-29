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
package org.jreleaser.model;

import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public class VersionPattern extends AbstractModelObject<VersionPattern> {
    private Type type;
    private String format;

    public VersionPattern() {
        this.type = Type.SEMVER;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        freezeCheck();
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        freezeCheck();
        this.format = format;
    }

    @Override
    public String toString() {
        String s = type.toString();
        switch (type) {
            case CALVER:
            case CUSTOM:
                if (isNotBlank(format)) {
                    s += ":" + format;
                }
        }
        return s;
    }

    @Override
    public void merge(VersionPattern source) {
        if (source != null) {
            this.type = merge(this.type, source.type);
            this.format = merge(this.format, source.format);
        }
    }

    public static VersionPattern of(String str) {
        if (isBlank(str)) return null;

        String[] parts = str.trim().split(":");

        VersionPattern vp = new VersionPattern();
        switch (parts.length) {
            case 1:
                vp.setType(Type.of(parts[0]));
                break;
            case 2:
                vp.setType(Type.of(parts[0]));
                vp.setFormat(parts[1].trim());
                break;
            default:
                throw new IllegalArgumentException();
        }

        return vp;
    }

    public enum Type {
        SEMVER,
        CALVER,
        CHRONVER,
        JAVA_RUNTIME,
        JAVA_MODULE,
        CUSTOM;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Type of(String str) {
            if (isBlank(str)) return null;
            return Type.valueOf(str.replaceAll(" ", "_")
                .replaceAll("-", "_")
                .toUpperCase(Locale.ENGLISH).trim());
        }
    }
}
