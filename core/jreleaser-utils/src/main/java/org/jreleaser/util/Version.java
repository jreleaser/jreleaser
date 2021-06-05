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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jreleaser.util.ObjectUtils.requireState;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Version implements Comparable<Version> {
    private static final Pattern FULL_SEMVER_PATTERN = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:[\\.\\-]((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    private static final Pattern MAJOR_MINOR_PATTERN = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:[\\.\\-]((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    private static final Pattern MAJOR_PATTERN = Pattern.compile("^(0|[1-9]\\d*)(?:[\\.\\-]((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    private final int major;
    private final int minor;
    private final int patch;
    private final String tag;
    private final String build;

    private Version(int major, int minor, int patch, String tag, String build) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.tag = isNotBlank(tag) ? tag.trim() : null;
        this.build = isNotBlank(build) ? build.trim() : null;
    }

    public boolean hasMinor() {
        return minor != -1;
    }

    public boolean hasPatch() {
        return patch != -1;
    }

    public boolean hasTag() {
        return isNotBlank(tag);
    }

    public boolean hasBuild() {
        return isNotBlank(build);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public String getTag() {
        return tag;
    }

    public String getBuild() {
        return build;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(major);
        if (hasMinor()) b.append(".").append(minor);
        if (hasPatch()) b.append(".").append(patch);
        if (hasTag()) b.append("-").append(tag);
        if (hasBuild()) b.append("+").append(build);
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version = (Version) o;
        return major == version.major &&
            minor == version.minor &&
            patch == version.patch &&
            Objects.equals(tag, version.tag) &&
            Objects.equals(build, version.build);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, tag, build);
    }

    @Override
    public int compareTo(Version other) {
        int result = major - other.major;
        if (result == 0) {
            result = minor - other.minor;
            if (result == 0) {
                result = patch - other.patch;
            }
        }
        return result;
    }

    public static int javaMajorVersion() {
        String jv = System.getProperty("java.version");
        if (jv.startsWith("1.")) {
            jv = jv.substring(2);
        }
        return Integer.parseInt(jv.split("\\.")[0]);
    }

    public static Version of(String version) {
        requireNonBlank(version, "Argument 'version' must not be blank");

        Matcher m = FULL_SEMVER_PATTERN.matcher(version.trim());

        if (m.matches()) {
            String major = m.group(1);
            String minor = m.group(2);
            String patch = m.group(3);
            String tag = m.group(4);
            String build = m.group(5);

            return of(Integer.parseInt(major),
                Integer.parseInt(minor),
                Integer.parseInt(patch),
                isNotBlank(tag) ? tag : null,
                isNotBlank(build) ? build : null);
        }

        m = MAJOR_MINOR_PATTERN.matcher(version);
        if (m.matches()) {
            String major = m.group(1);
            String minor = m.group(2);
            String tag = m.group(3);
            String build = m.group(4);

            return of(Integer.parseInt(major),
                Integer.parseInt(minor),
                isNotBlank(tag) ? tag : null,
                isNotBlank(build) ? build : null);
        }

        m = MAJOR_PATTERN.matcher(version);
        if (m.matches()) {
            String major = m.group(1);
            String tag = m.group(2);
            String build = m.group(3);

            return of(Integer.parseInt(major),
                isNotBlank(tag) ? tag : null,
                isNotBlank(build) ? build : null);
        }

        throw new IllegalArgumentException("Cannot parse version '" + version + "'");
    }

    public static Version of(int major, int minor, int patch, String tag, String build) {
        requireState(major > -1, "Argument 'major' must not be negative");
        requireState(minor > -1, "Argument 'minor' must not be negative");
        requireState(patch > -1, "Argument 'patch' must not be negative");
        return new Version(major, minor, patch, tag, build);
    }

    public static Version of(int major, int minor, String tag, String build) {
        requireState(major > -1, "Argument 'major' must not be negative");
        requireState(minor > -1, "Argument 'minor' must not be negative");
        return new Version(major, minor, -1, tag, build);
    }

    public static Version of(int major, String tag, String build) {
        requireState(major > -1, "Argument 'major' must not be negative");
        return new Version(major, -1, -1, tag, build);
    }
}
