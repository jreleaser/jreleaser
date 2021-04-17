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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Version {
    private static final Pattern FULL_SEMVER_PATTERN = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:[\\.\\-]((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    private static final Pattern MAJOR_MINOR_PATTERN = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:[\\.\\-]((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    private static final Pattern MAJOR_PATTERN = Pattern.compile("^(0|[1-9]\\d*)(?:[\\.\\-]((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    private final String major;
    private final String minor;
    private final String patch;
    private final String tag;
    private final String build;

    private Version(String major, String minor, String patch, String tag, String build) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.tag = isNotBlank(tag) ? tag.trim() : null;
        this.build = isNotBlank(build) ? build.trim() : null;
    }

    public boolean hasMinor() {
        return isNotBlank(minor);
    }

    public boolean hasPatch() {
        return isNotBlank(patch);
    }

    public boolean hasTag() {
        return isNotBlank(tag);
    }

    public boolean hasBuild() {
        return isNotBlank(build);
    }

    public String getMajor() {
        return major;
    }

    public String getMinor() {
        return minor;
    }

    public String getPatch() {
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
        StringBuilder b = new StringBuilder(major);
        if (hasMinor()) b.append(".").append(minor);
        if (hasPatch()) b.append(".").append(patch);
        if (hasTag()) b.append("-").append(tag);
        if (hasBuild()) b.append("+").append(build);
        return b.toString();
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

            return of(major, minor, patch,
                isNotBlank(tag) ? tag : null,
                isNotBlank(build) ? build : null);
        }

        m = MAJOR_MINOR_PATTERN.matcher(version);
        if (m.matches()) {
            String major = m.group(1);
            String minor = m.group(2);
            String tag = m.group(3);
            String build = m.group(4);

            return of(major, minor,
                isNotBlank(tag) ? tag : null,
                isNotBlank(build) ? build : null);
        }

        m = MAJOR_PATTERN.matcher(version);
        if (m.matches()) {
            String major = m.group(1);
            String tag = m.group(2);
            String build = m.group(3);

            return of(major,
                isNotBlank(tag) ? tag : null,
                isNotBlank(build) ? build : null);
        }

        throw new IllegalArgumentException("Cannot parse version '" + version + "'");
    }

    public static Version of(String major, String minor, String patch, String tag, String build) {
        requireNonBlank(major, "Argument 'major' must not be blank");
        requireNonBlank(minor, "Argument 'minor' must not be blank");
        requireNonBlank(patch, "Argument 'patch' must not be blank");
        return new Version(major.trim(), minor.trim(), patch.trim(), tag, build);
    }

    public static Version of(String major, String minor, String tag, String build) {
        requireNonBlank(major, "Argument 'major' must not be blank");
        requireNonBlank(minor, "Argument 'minor' must not be blank");
        return new Version(major.trim(), minor.trim(), null, tag, build);
    }

    public static Version of(String major, String tag, String build) {
        requireNonBlank(major, "Argument 'major' must not be blank");
        return new Version(major.trim(), null, null, tag, build);
    }
}
