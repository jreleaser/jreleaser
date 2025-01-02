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
package org.jreleaser.version;

import java.util.Objects;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class CustomVersion implements Version<CustomVersion> {
    private final String version;

    private CustomVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equalsSpec(CustomVersion version) {
        return true;
    }

    @Override
    public String toRpmVersion() {
        return toString().replace("-", "_");
    }

    @Override
    public String toString() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        CustomVersion v = (CustomVersion) o;
        return version.equals(v.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public int compareTo(CustomVersion other) {
        return version.compareTo(other.version);
    }

    public static CustomVersion defaultOf() {
        return of("0.0.0");
    }

    public static CustomVersion of(String version) {
        requireNonBlank(version, "Argument 'version' must not be blank");
        return new CustomVersion(version);
    }
}
