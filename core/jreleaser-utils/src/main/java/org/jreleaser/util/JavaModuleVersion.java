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
package org.jreleaser.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public class JavaModuleVersion implements Version<JavaModuleVersion> {
    private final String version;
    private final String prerelease;
    private final String build;

    private JavaModuleVersion(String version, String prerelease, String build) {
        this.version = version;
        this.prerelease = isNotBlank(prerelease) ? prerelease.trim() : null;
        this.build = isNotBlank(build) ? build.trim() : null;
    }

    public boolean hasPrerelease() {
        return isNotBlank(prerelease);
    }

    public boolean hasBuild() {
        return isNotBlank(build);
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

    @Override
    public String toRpmVersion() {
        StringBuilder b = new StringBuilder();
        b.append(version);
        if (hasPrerelease()) b.append("~").append(prerelease.replace("-", "_"));
        if (hasBuild()) b.append("_").append(build.replace("-", "_"));
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(version);
        if (hasPrerelease()) b.append("-").append(prerelease);
        if (hasBuild()) b.append("+").append(build);
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaModuleVersion v = (JavaModuleVersion) o;
        return Objects.equals(version, v.version) &&
            Objects.equals(prerelease, v.prerelease) &&
            Objects.equals(build, v.build);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, prerelease, build);
    }

    @Override
    public int compareTo(JavaModuleVersion that) {
        int c = compareVersion(this.version, that.version);
        if (c != 0) return c;
        if (isBlank(this.prerelease)) {
            if (isNotBlank(that.prerelease)) return 1;
        } else {
            if (isBlank(that.prerelease)) return -1;
        }
        c = comparePrerelease(this.prerelease, that.prerelease);
        if (c != 0) return c;
        return compareBuild(this.build, that.build);
    }

    @Override
    public boolean equalsSpec(JavaModuleVersion version) {
        return check(prerelease, version.prerelease) &&
            check(build, version.build);
    }

    private boolean check(String s1, String s2) {
        if (isBlank(s1)) {
            return isBlank(s2);
        } else {
            return isNotBlank(s2);
        }
    }

    private int compareVersion(String v1, String v2) {
        return compareTokens(v1.split("\\."), v2.split("\\."));
    }

    private int comparePrerelease(String p1, String p2) {
        List<Character> delims = CollectionUtils.listOf('.', '-');
        return compareTokens(split(p1, delims), split(p2, delims));
    }

    private int compareBuild(String b1, String b2) {
        List<Character> delims = CollectionUtils.listOf('.', '-', '+');
        return compareTokens(split(b1, delims), split(b2, delims));
    }

    private String[] split(String s, List<Character> delims) {
        if (isBlank(s)) return new String[0];

        List<String> tokens = new ArrayList<>();

        StringBuilder accumulator = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (delims.contains(c)) {
                tokens.add(accumulator.toString());
                accumulator = new StringBuilder();
            } else {
                accumulator.append(c);
            }
        }

        if (accumulator.length() > 0) {
            tokens.add(accumulator.toString());
        }

        return tokens.toArray(new String[0]);
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

    public static JavaModuleVersion defaultOf() {
        return of("0.0.0");
    }

    public static JavaModuleVersion of(String version) {
        requireNonBlank(version, "Argument 'version' must not be blank");

        char c = version.charAt(0);
        if (!(c >= '0' && c <= '9')) {
            throw new IllegalArgumentException("Version does not start with a digit: '" + version + "'");
        }

        String v = take(version, 0, listOf('-', '+'));
        String p = null;
        String b = null;
        if (v.length() + 1 < version.length()) {
            if (version.charAt(v.length()) == '-') {
                p = take(version, v.length() + 1, listOf('+'));
                if (v.length() + 1 + p.length() + 1 < version.length()) {
                    b = take(version, v.length() + 1 + p.length() + 1, emptyList());
                }
            } else {
                b = take(version, v.length() + 1, emptyList());
            }
        }

        return new JavaModuleVersion(v, p, b);
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

    public static JavaModuleVersion of(String version, String tag, String build) {
        requireNonBlank(version, "Argument 'version' must not be blank");
        return new JavaModuleVersion(version, tag, build);
    }
}
