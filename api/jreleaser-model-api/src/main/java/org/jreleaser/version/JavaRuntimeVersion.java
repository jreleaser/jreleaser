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

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class JavaRuntimeVersion implements Version<JavaRuntimeVersion> {
    private static final Pattern PBO = Pattern.compile("(?:\\-([a-zA-Z0-9]+))?\\+(0|[1-9]\\d*)(?:\\-([\\-a-zA-Z0-9\\.]+))?");
    private static final Pattern PO = Pattern.compile("\\-([a-zA-Z0-9]+)(?:\\-([-a-zA-Z0-9.]+))?");
    private static final Pattern O = Pattern.compile("\\+\\-([-a-zA-Z0-9.]+)");

    private final String version;
    private final String prerelease;
    private final String build;
    private final String optional;
    private final Pattern pattern;

    private JavaRuntimeVersion(String version, String prerelease, String build, String optional, Pattern pattern) {
        this.version = version;
        this.prerelease = isNotBlank(prerelease) ? prerelease.trim() : null;
        this.build = isNotBlank(build) ? build.trim() : null;
        this.optional = isNotBlank(optional) ? optional.trim() : null;
        this.pattern = pattern;
    }

    public int feature() {
        String[] parts = version.split("\\.");
        return Integer.parseInt(parts[0]);
    }

    public int interim() {
        String[] parts = version.split("\\.");
        return parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
    }

    public int update() {
        String[] parts = version.split("\\.");
        return parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
    }

    public int patch() {
        String[] parts = version.split("\\.");
        return parts.length > 3 ? Integer.parseInt(parts[3]) : 0;
    }

    public boolean hasPrerelease() {
        return isNotBlank(prerelease);
    }

    public boolean hasBuild() {
        return isNotBlank(build);
    }

    public boolean hasOptional() {
        return isNotBlank(optional);
    }

    public String getVersion() {
        return version;
    }

    public String getPrerelease() {
        return prerelease;
    }

    public String getBuild() {
        return build;
    }

    public String getOptional() {
        return optional;
    }

    @Override
    public String toRpmVersion() {
        StringBuilder b = new StringBuilder();
        b.append(version);
        if (!hasPrerelease() && !hasBuild() && hasOptional()) {
            b.append("~").append(optional.replace("-", "_"));
        } else {
            if (hasPrerelease()) b.append("~").append(prerelease.replace("-", "_"));
            if (hasBuild()) b.append("_").append(build.replace("-", "_"));
            if (hasOptional()) b.append("_").append(optional.replace("-", "_"));
        }
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(version);
        if (!hasPrerelease() && !hasBuild() && hasOptional()) {
            b.append("+-").append(optional);
        } else {
            if (hasPrerelease()) b.append("-").append(prerelease);
            if (hasBuild()) b.append("+").append(build);
            if (hasOptional()) b.append("-").append(optional);
        }
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        JavaRuntimeVersion v = (JavaRuntimeVersion) o;
        return pattern.pattern().equals(v.pattern.pattern()) &&
            Objects.equals(version, v.version) &&
            Objects.equals(prerelease, v.prerelease) &&
            Objects.equals(build, v.build) &&
            Objects.equals(optional, v.optional);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern.pattern(), version, prerelease, build, optional);
    }

    @Override
    public int compareTo(JavaRuntimeVersion that) {
        int c = compareVersion(this.version, that.version);
        if (c != 0) return c;
        c = compareStrings(this.prerelease, that.prerelease);
        if (c != 0) return c;
        c = compareStringsInvert(this.build, that.build);
        if (c != 0) return c;
        return compareStringsInvert(this.optional, that.optional);
    }

    @Override
    public boolean equalsSpec(JavaRuntimeVersion version) {
        return pattern.pattern().equals(version.pattern.pattern());
    }

    private int compareVersion(String v1, String v2) {
        return compareTokens(v1.split("\\."), v2.split("\\."));
    }

    private int compareStrings(String s1, String s2) {
        if (isBlank(s1) && isBlank(s2)) return 0;

        if (isBlank(s1)) {
            if (isNotBlank(s2)) return 1;
        } else {
            if (isBlank(s2)) return -1;
        }
        return s1.compareTo(s2);
    }

    private int compareStringsInvert(String s1, String s2) {
        if (isBlank(s1) && isBlank(s2)) return 0;

        if (isBlank(s1)) {
            if (isNotBlank(s2)) return -1;
        } else {
            if (isBlank(s2)) return 1;
        }
        return s1.compareTo(s2);
    }

    private int compareTokens(String[] t1, String[] t2) {
        int n = Math.min(t1.length, t2.length);
        for (int i = 0; i < n; i++) {
            String s1 = t1[i];
            String s2 = t2[i];

            try {
                int c = compareAsNumbers(s1, s2);
                if (0 != c) return c;
            } catch (NumberFormatException e) {
                int c = s1.compareTo(s2);
                if (0 != c) return c;
            }
        }

        String[] rest = t1.length > t2.length ? t1 : t2;
        int e = rest.length;
        for (int i = n; i < e; i++) {
            String o = rest[i];
            if ("0".equals(o)) continue;
            return t1.length - t2.length;
        }

        return 0;
    }

    private int compareAsNumbers(String s1, String s2) {
        int i1 = Integer.parseInt(s1);
        int i2 = Integer.parseInt(s2);
        return Integer.compare(i1, i2);
    }

    public static JavaRuntimeVersion defaultOf() {
        return of("0.0.0");
    }

    public static JavaRuntimeVersion of(String version) {
        requireNonBlank(version, "Argument 'version' must not be blank");

        char c = version.charAt(0);
        if (!(c >= '0' && c <= '9')) {
            throw new IllegalArgumentException("Version does not start with a digit: '" + version + "'");
        }

        String v = take(version, 0, listOf('-', '+'));
        String p = null;
        String b = null;
        String o = null;

        if (v.length() + 1 < version.length()) {
            String s = version.substring(v.length());

            Matcher m = O.matcher(s);
            if (m.matches()) {
                return new JavaRuntimeVersion(v, p, b, m.group(1), O);
            }

            m = PO.matcher(s);
            if (m.matches()) {
                return new JavaRuntimeVersion(v, m.group(1), b, m.group(2), PO);
            }

            m = PBO.matcher(s);
            if (m.matches()) {
                p = m.group(1);
                b = m.group(2);
                o = m.group(3);
            }
        }

        return new JavaRuntimeVersion(v, p, b, o, PBO);
    }

    private static String take(String str, int index, List<Character> delims) {
        StringBuilder b = new StringBuilder();

        for (int i = index; i < str.length(); i++) {
            char c = str.charAt(i);
            if (delims.contains(c)) {
                break;
            }
            b.append(c);
        }

        return b.toString();
    }

    public static JavaRuntimeVersion of(String version, String tag, String build, String optional) {
        return of(version, tag, build, optional, PBO);
    }

    private static JavaRuntimeVersion of(String version, String tag, String build, String optional, Pattern pattern) {
        requireNonBlank(version, "Argument 'version' must not be blank");
        return new JavaRuntimeVersion(version, tag, build, optional, pattern);
    }
}
