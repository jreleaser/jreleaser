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

import org.jreleaser.bundle.RB;

import java.time.YearMonth;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jreleaser.util.ObjectUtils.requireState;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class ChronVer implements Version<ChronVer> {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^([2-9]\\d{3})\\.(0[1-9]|1[0-2])\\.(0[1-9]|[1-2]\\d|3[0-1])(?:\\.((?:[1-9]\\d*)(?:(?:-[a-zA-Z0-9]+)+(?:\\.[1-9]\\d*)?)?))?(?:-[a-zA-Z0-9]+)?$");
    private static final Pattern CHANGESET_PATTERN = Pattern.compile("^(?:((?:[1-9]\\d*))(?:-([a-zA-Z0-9-]+[a-zA-Z0-9]?)(?:\\.([1-9]\\d*))?)?)?$");

    private final int year;
    private final int month;
    private final int day;
    private final Changeset changeset;

    private ChronVer(int year, int month, int day, Changeset changeset) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.changeset = changeset;
    }

    public boolean hasChangeset() {
        return !changeset.isEmpty();
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public Changeset getChangeset() {
        return changeset;
    }

    @Override
    public String toRpmVersion() {
        return toString().replace("-", "_");
    }

    @Override
    public boolean equalsSpec(ChronVer version) {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(year)
            .append(".")
            .append(month)
            .append(".")
            .append(day);
        if (hasChangeset()) {
            b.append(".")
                .append(changeset);
        }
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        ChronVer version = (ChronVer) o;
        return year == version.year &&
            month == version.month &&
            day == version.day &&
            Objects.equals(changeset, version.changeset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day, changeset);
    }

    @Override
    public int compareTo(ChronVer other) {
        int result = year - other.year;
        if (result == 0) {
            result = month - other.month;
            if (result == 0) {
                result = day - other.day;
            }
        }

        if (result == 0) {
            result = changeset.compareTo(other.changeset);
        }

        return result;
    }

    public static ChronVer defaultOf() {
        return of("2000.01.01");
    }

    public static ChronVer of(String version) {
        requireNonBlank(version, "Argument 'version' must not be blank");

        Matcher m = VERSION_PATTERN.matcher(version.trim());

        if (m.matches()) {
            int year = Integer.parseInt(m.group(1));
            String s = m.group(2);
            int month = Integer.parseInt(s.startsWith("0") ? s.substring(1) : s);
            s = m.group(3);
            int day = Integer.parseInt(s.startsWith("0") ? s.substring(1) : s);
            String changeset = m.group(4);

            // validate num of days per month
            if (day > YearMonth.of(year, month).lengthOfMonth()) {
                throw new IllegalArgumentException(RB.$("ERROR_version_parse", version));
            }

            return of(year, month, day, changeset);
        }

        throw new IllegalArgumentException(RB.$("ERROR_version_parse", version));
    }

    public static ChronVer of(int year, int month, int day, String changeset) {
        requireState(year > -1, "Argument 'year' must not be negative");
        requireState(month > -1, "Argument 'month' must not be negative");
        requireState(day > -1, "Argument 'day' must not be negative");
        return new ChronVer(year, month, day, Changeset.of(changeset));
    }

    public static final class Changeset implements Comparable<Changeset> {
        private final String identifier;
        private final int change;
        private final String tag;
        private final int change2;

        private Changeset(String identifier) {
            if (isNotBlank(identifier)) {
                this.identifier = identifier.trim();
                Matcher matcher = CHANGESET_PATTERN.matcher(identifier);
                if (matcher.matches()) {
                    this.change = Integer.parseInt(matcher.group(1));
                    this.tag = matcher.group(2);
                    String c = matcher.group(3);
                    if (isNotBlank(c)) {
                        this.change2 = Integer.parseInt(c);
                    } else {
                        this.change2 = 0;
                    }
                } else {
                    this.change = 0;
                    this.tag = null;
                    this.change2 = 0;
                }
            } else {
                this.identifier = "";
                this.change = 0;
                this.tag = null;
                this.change2 = 0;
            }
        }

        public String getIdentifier() {
            return identifier;
        }

        public boolean isEmpty() {
            return isBlank(identifier);
        }

        public boolean hasTag() {
            return isNotBlank(tag);
        }

        public boolean hasChange2() {
            return change2 != 0;
        }

        public int getChange() {
            return change;
        }

        public String getTag() {
            return tag;
        }

        public int getChange2() {
            return change2;
        }

        @Override
        public String toString() {
            if (isEmpty()) return "";
            StringBuilder b = new StringBuilder()
                .append(change);
            if (hasTag()) {
                b.append("-").append(tag);
            }
            if (hasChange2()) {
                b.append(".").append(change2);
            }
            return b.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (null == o || getClass() != o.getClass()) return false;
            Changeset changeset = (Changeset) o;
            return identifier.equals(changeset.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        @Override
        public int compareTo(Changeset other) {
            if (null == other) return -1;
            if (isEmpty() && other.isEmpty()) return 0;
            if (isEmpty() && !other.isEmpty()) return 1;
            if (!isEmpty() && other.isEmpty()) return -1;

            int c = change - other.change;

            if (c == 0 && hasTag()) {
                c = tag.compareTo(other.tag);
            }

            if (c == 0 && hasChange2()) {
                c = change2 - other.change2;
            }

            return c;
        }

        public static Changeset of(String str) {
            return new Changeset(str);
        }
    }
}
