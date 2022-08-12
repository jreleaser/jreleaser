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

import org.jreleaser.bundle.RB;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.binarySearch;
import static java.util.Collections.emptyList;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
public class CalVer implements Version<CalVer> {
    private static final Map<String, String> PATTERNS = new LinkedHashMap<>();

    private static final String YEAR = "YEAR";
    private static final String YEAR_LONG = "YYYY";
    private static final String YEAR_SHORT = "YY";
    private static final String YEAR_ZERO = "0Y";
    private static final String MONTH = "MONTH";
    private static final String MONTH_SHORT = "MM";
    private static final String MONTH_ZERO = "0M";
    private static final String WEEK = "WEEK";
    private static final String WEEK_SHORT = "WW";
    private static final String WEEK_ZERO = "0W";
    private static final String DAY = "DAY";
    private static final String DAY_SHORT = "DD";
    private static final String DAY_ZERO = "0D";
    private static final String MINOR = "MINOR";
    private static final String MICRO = "MICRO";
    private static final String MODIFIER = "MODIFIER";
    private static final String MODIFIER_OP = "[MODIFIER]";
    private static final String MODIFIER_OP2 = "MODIFIER]";

    private static final String[] YEARS = {YEAR_ZERO, YEAR_SHORT, YEAR_LONG};
    private static final String[] MONTHS = {MONTH_ZERO, MONTH_SHORT};
    private static final String[] WEEKS = {WEEK_ZERO, WEEK_SHORT};
    private static final String[] DAYS = {DAY_ZERO, DAY_SHORT};
    private static final String[] NUMBERS = {MICRO, MINOR};

    static {
        PATTERNS.put(YEAR_LONG, "([2-9][0-9]{3})");
        PATTERNS.put(YEAR_SHORT, "([1-9]|[1-9][0-9]|[1-9][0-9]{2})");
        PATTERNS.put(YEAR_ZERO, "(0[1-9]|[1-9][0-9]|[1-9][0-9]{2})");
        PATTERNS.put(MONTH_SHORT, "([1-9]|1[0-2])");
        PATTERNS.put(MONTH_ZERO, "(0[1-9]|1[0-2])");
        PATTERNS.put(WEEK_SHORT, "([1-9]|[1-4][0-9]|5[0-2])");
        PATTERNS.put(WEEK_ZERO, "(0[1-9]|[1-4][0-9]|5[0-2])");
        PATTERNS.put(DAY_SHORT, "([1-9]|[1-2][0-9]|3[0-1])");
        PATTERNS.put(DAY_ZERO, "(0[1-9]|[1-2][0-9]|3[0-1])");
        PATTERNS.put(MINOR, "(0|[1-9]\\d*)");
        PATTERNS.put(MICRO, "(0|[1-9]\\d*)");
        PATTERNS.put(MODIFIER, "([a-zA-Z\\-][0-9a-zA-Z\\-]*)");
        PATTERNS.put(MODIFIER_OP, "([a-zA-Z\\-][0-9a-zA-Z\\-]*))?");
    }

    private final String year;
    private final String month;
    private final String week;
    private final String day;
    private final String minor;
    private final String micro;
    private final String modifier;
    private final String pattern;

    private final int yearAsInt;
    private final int monthAsInt;
    private final int weekAsInt;
    private final int dayAsInt;
    private final int minorAsInt;
    private final int microAsInt;

    public CalVer(String pattern, Map<String, String> elements) {
        String y = elements.get(YEAR);
        String m = elements.get(MONTH);
        String w = elements.get(WEEK);
        String d = elements.get(DAY);
        String n = elements.get(MINOR);
        String r = elements.get(MICRO);
        String o = elements.get(MODIFIER);

        this.pattern = pattern;
        this.year = isNotBlank(y) ? y.trim() : null;
        this.month = isNotBlank(m) ? m.trim() : null;
        this.week = isNotBlank(w) ? w.trim() : null;
        this.day = isNotBlank(d) ? d.trim() : null;
        this.minor = isNotBlank(n) ? n.trim() : null;
        this.micro = isNotBlank(r) ? r.trim() : null;
        this.modifier = isNotBlank(o) ? o.trim() : null;

        this.yearAsInt = isBlank(this.year) ? -1 : parseInt(this.year);
        this.monthAsInt = isBlank(this.month) ? -1 : parseInt(this.month);
        this.weekAsInt = isBlank(this.week) ? -1 : parseInt(this.week);
        this.dayAsInt = isBlank(this.day) ? -1 : parseInt(this.day);
        this.minorAsInt = isBlank(this.minor) ? -1 : parseInt(this.minor);
        this.microAsInt = isBlank(this.micro) ? -1 : parseInt(this.micro);

        if (yearAsInt != -1 && monthAsInt != -1 && dayAsInt != -1) {
            // validate num of days per month
            if (dayAsInt > YearMonth.of(yearAsInt, monthAsInt).lengthOfMonth()) {
                throw new IllegalArgumentException(RB.$("ERROR_version_parse", this));
            }
        }
    }

    public boolean hasYear() {
        return isNotBlank(year);
    }

    public boolean hasMonth() {
        return isNotBlank(month);
    }

    public boolean hasWeek() {
        return isNotBlank(week);
    }

    public boolean hasDay() {
        return isNotBlank(day);
    }

    public boolean hasMinor() {
        return isNotBlank(minor);
    }

    public boolean hasMicro() {
        return isNotBlank(micro);
    }

    public boolean hasModifier() {
        return isNotBlank(modifier);
    }

    public String getPattern() {
        return pattern;
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getWeek() {
        return week;
    }

    public String getDay() {
        return day;
    }

    public String getMinor() {
        return minor;
    }

    public String getMicro() {
        return micro;
    }

    public String getModifier() {
        return modifier;
    }

    public int getYearAsInt() {
        return yearAsInt;
    }

    public int getMonthAsInt() {
        return monthAsInt;
    }

    public int getWeekAsInt() {
        return weekAsInt;
    }

    public int getDayAsInt() {
        return dayAsInt;
    }

    public int getMinorAsInt() {
        return minorAsInt;
    }

    public int getMicroAsInt() {
        return microAsInt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalVer version = (CalVer) o;
        return Objects.equals(pattern, version.pattern) &&
            Objects.equals(year, version.year) &&
            Objects.equals(month, version.month) &&
            Objects.equals(week, version.week) &&
            Objects.equals(day, version.day) &&
            Objects.equals(minor, version.minor) &&
            Objects.equals(micro, version.micro) &&
            Objects.equals(modifier, version.modifier);
    }

    @Override
    public String toRpmVersion() {
        return toString().replace("-", "_");
    }

    @Override
    public String toString() {
        String str = safeReplace(pattern, YEAR_LONG, year);
        str = safeReplace(str, YEAR_SHORT, year);
        str = safeReplace(str, YEAR_ZERO, year);
        str = safeReplace(str, MONTH_SHORT, month);
        str = safeReplace(str, MONTH_ZERO, month);
        str = safeReplace(str, WEEK_SHORT, week);
        str = safeReplace(str, WEEK_ZERO, week);
        str = safeReplace(str, DAY_SHORT, day);
        str = safeReplace(str, DAY_ZERO, day);
        str = safeReplace(str, MINOR, minor);
        str = safeReplace(str, MICRO, micro);
        str = safeReplace(str, MODIFIER, modifier);
        str = safeReplace(str, "[", "");
        str = safeReplace(str, "]", "");

        return str;
    }

    private String safeReplace(String str, CharSequence target, CharSequence replacement) {
        if (null != replacement) {
            return str.replace(target, replacement);
        }
        return str;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, year, month, week, day, minor, micro, modifier);
    }

    @Override
    public int compareTo(CalVer other) {
        int result = pattern.compareTo(other.pattern);

        if (result == 0) {
            result = yearAsInt - (other.hasYear() ? other.yearAsInt : 0);
        }

        if (result == 0) {
            result = hasMonth() ? monthAsInt - (other.hasMonth() ? other.monthAsInt : 0) : 0;
        }

        if (result == 0) {
            result = hasWeek() ? weekAsInt - (other.hasWeek() ? other.weekAsInt : 0) : 0;
        }

        if (result == 0) {
            result = hasDay() ? dayAsInt - (other.hasDay() ? other.dayAsInt : 0) : 0;
        }

        if (result == 0) {
            result = hasMinor() ? minorAsInt - (other.hasMinor() ? other.minorAsInt : 0) : 0;
        }

        if (result == 0) {
            result = hasMicro() ? microAsInt - (other.hasMicro() ? other.microAsInt : 0) : 0;
        }

        if (result == 0 && isNotBlank(modifier)) {
            result = isNotBlank(other.modifier) ? modifier.compareTo(other.modifier) : -1;
        }

        return result;
    }

    @Override
    public boolean equalsSpec(CalVer version) {
        return pattern.equals(version.pattern);
    }

    private static int parseInt(String str) {
        if ("0".equals(str)) return 0;
        if (str.startsWith("0")) {
            return Integer.parseInt(str.substring(1));
        }
        return Integer.parseInt(str);
    }

    public static CalVer of(String format, String version) {
        requireNonBlank(format, "Argument 'format' must not be blank");
        requireNonBlank(version, "Argument 'version' must not be blank");

        List<String> tokens = new ArrayList<>();

        List<Character> delims = listOf('.', '_', '-', '[');
        String f = format.trim();
        String y = null;
        String m = null;
        String w = null;
        String d = null;
        String n = null;
        String r = null;
        String o = null;
        int i = 0;

        Tuple s = take(f, i, delims);
        if (binarySearch(YEARS, s.token) < 0) {
            throw new IllegalArgumentException(RB.$("ERROR_calver_year", f));
        }
        y = s.token;
        tokens.add(y);
        if (isNotBlank(s.sep)) tokens.add(s.sep);
        i = y.length() + 1;

        s = take(f, i, delims);
        if (binarySearch(MONTHS, s.token) >= 0) {
            // cannot have weeks
            if (f.contains(WEEK_ZERO) || f.contains(WEEK_SHORT)) {
                throw new IllegalArgumentException(RB.$("ERROR_calver_month", f));
            }
            m = s.token;
            tokens.add(m);
            if (isNotBlank(s.sep)) tokens.add(s.sep);
            i += m.length() + 1;

            s = take(f, i, delims);
            if (binarySearch(DAYS, s.token) >= 0) {
                d = s.token;
                tokens.add(d);
                if (isNotBlank(s.sep)) tokens.add(s.sep);
                i += d.length() + 1;
                s = take(f, i, delims);
            }
        } else if (binarySearch(WEEKS, s.token) >= 0) {
            // cannot have months nor days
            if (f.contains(MONTH_ZERO) || f.contains(MONTH_SHORT)) {
                throw new IllegalArgumentException(RB.$("ERROR_calver_week_month", f));
            }
            if (f.contains(DAY_ZERO) || f.contains(DAY_SHORT)) {
                throw new IllegalArgumentException(RB.$("ERROR_calver_week_day", f));
            }
            w = s.token;
            tokens.add(w);
            if (isNotBlank(s.sep)) tokens.add(s.sep);
            i += w.length() + 1;

            s = take(f, i, delims);
        }

        boolean micro = false;
        boolean done = false;
        if (binarySearch(NUMBERS, s.token) >= 0) {
            tokens.add(s.token);
            if (isNotBlank(s.sep)) tokens.add(s.sep);
            i += s.token.length() + 1;
            micro = MICRO.equals(s.token);
            n = !micro ? s.token : null;
            r = micro ? s.token : null;
            s = take(f, i, delims);
            done = isBlank(s.token);
        } else {
            s = take(f, i, emptyList());
            o = s.token;
            if (isNotBlank(o)) tokens.add(o);
            done = true;
        }

        if (!done) {
            if (binarySearch(NUMBERS, s.token) >= 0) {
                if (micro) {
                    if (MICRO.equals(s.token)) {
                        throw new IllegalArgumentException(RB.$("ERROR_calver_micro_duplicate", f));
                    } else {
                        throw new IllegalArgumentException(RB.$("ERROR_calver_micro_minor", f));
                    }
                } else if (MINOR.equals(s.token)) {
                    throw new IllegalArgumentException(RB.$("ERROR_calver_minor_duplicate", f));
                }
                tokens.add(s.token);
                if (isNotBlank(s.sep)) tokens.add(s.sep);
                r = s.token;
                i += r.length() + 1;
                s = take(f, i, emptyList());
                o = s.token;
                if (isNotBlank(o)) tokens.add(o);
            } else {
                s = take(f, i, emptyList());
                o = s.token;
                if (isNotBlank(o)) tokens.add(o);
            }
        }

        if (tokens.size() > 0 && tokens.get(tokens.size() - 1).endsWith(MODIFIER_OP2)) {
            String sep = tokens.remove(tokens.size() - 2);
            String mod = "(?:" + sep + PATTERNS.get(MODIFIER_OP);
            tokens.set(tokens.size() - 1, mod);
        }

        Pattern pattern = Pattern.compile("^" + tokens.stream()
            .map(t -> PATTERNS.getOrDefault(t, t))
            .collect(Collectors.joining("")) + "$");

        Matcher matcher = pattern.matcher(version.trim());

        if (matcher.matches()) {
            i = 1;
            Map<String, String> elements = new LinkedHashMap<>();
            elements.put(YEAR, matcher.group(i++));
            if (isNotBlank(w)) {
                elements.put(WEEK, matcher.group(i++));
            }
            if (isNotBlank(m)) {
                elements.put(MONTH, matcher.group(i++));
            }
            if (isNotBlank(d)) {
                elements.put(DAY, matcher.group(i++));
            }
            if (isNotBlank(n)) {
                elements.put(MINOR, matcher.group(i++));
            }
            if (isNotBlank(r)) {
                elements.put(MICRO, matcher.group(i++));
            }
            if (i <= matcher.groupCount()) {
                elements.put(MODIFIER, matcher.group(matcher.groupCount()));
            }

            return new CalVer(format, elements);
        }

        throw new IllegalArgumentException(RB.$("ERROR_version_parse_with", version, f));
    }

    public static CalVer defaultOf(String format) {
        requireNonBlank(format, "Argument 'format' must not be blank");

        return of(format, format.replace(YEAR_LONG, "2000")
            .replace(YEAR_SHORT, "0")
            .replace(YEAR_ZERO, "0")
            .replace(MONTH_SHORT, "1")
            .replace(MONTH_ZERO, "01")
            .replace(WEEK_SHORT, "1")
            .replace(WEEK_ZERO, "01")
            .replace(DAY_SHORT, "1")
            .replace(DAY_ZERO, "01")
            .replace(MINOR, "0")
            .replace(MICRO, "0")
            .replace(MODIFIER, "A")
            .replace("[", "")
            .replace("]", ""));
    }

    private static Tuple take(String str, int index, List<Character> delims) {
        StringBuilder b = new StringBuilder();

        for (int i = index; i < str.length(); i++) {
            char c = str.charAt(i);
            if (delims.contains(c)) {
                if (c == '[' && str.length() > i + 1) {
                    c = str.charAt(i + 1);
                }
                return new Tuple(b.toString(), c);
            }
            b.append(c);
        }

        return new Tuple(b.toString(), (char) 0);
    }

    private static class Tuple {
        private final String token;
        private final String sep;

        private Tuple(String token, char sep) {
            this.token = token;
            this.sep = sep != ((char) 0) ? escape(sep) : "";
        }

        private static String escape(char sep) {
            if ('.' == sep) {
                return "\\.";
            }
            return String.valueOf(sep);
        }
    }
}
