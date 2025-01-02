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

import org.jreleaser.util.ComparatorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
class CalVerTest {
    @ParameterizedTest
    @MethodSource("version_parsing")
    void testVersionParsing(String format, String input,
                            String year, String month, String day, String week,
                            String minor, String micro, String modifier) {
        // given:
        CalVer v = CalVer.of(format, input);

        // then:
        assertThat(v.getYear(), equalTo(year));
        assertThat(v.getMonth(), equalTo(month));
        assertThat(v.getDay(), equalTo(day));
        assertThat(v.getWeek(), equalTo(week));
        assertThat(v.getMinor(), equalTo(minor));
        assertThat(v.getMicro(), equalTo(micro));
        assertThat(v.getModifier(), equalTo(modifier));
    }

    @ParameterizedTest
    @MethodSource("default_version_parsing")
    void testDefaultVersionParsing(String format, String year, String month, String day,
                                   String week, String minor, String micro, String modifier) {
        // given:
        CalVer v = CalVer.defaultOf(format);

        // then:
        assertThat(v.getYear(), equalTo(year));
        assertThat(v.getMonth(), equalTo(month));
        assertThat(v.getDay(), equalTo(day));
        assertThat(v.getWeek(), equalTo(week));
        assertThat(v.getMinor(), equalTo(minor));
        assertThat(v.getMicro(), equalTo(micro));
        assertThat(v.getModifier(), equalTo(modifier));
    }

    @ParameterizedTest
    @MethodSource("version_invalid")
    void testVersionInvalid(String format, String input) {
        // expect:
        assertThrows(IllegalArgumentException.class, () -> CalVer.of(format, input));
    }

    @ParameterizedTest
    @MethodSource("version_comparison")
    void testVersionComparison(String format, String input1, String input2) {
        // given:
        CalVer v1 = CalVer.of(format, input1);
        CalVer v2 = CalVer.of(format, input2);

        // then:
        Assertions.assertTrue(ComparatorUtils.lessThan(v1, v2));
        Assertions.assertTrue(ComparatorUtils.greaterThan(v2, v1));
    }

    private static Stream<Arguments> version_parsing() {
        return Stream.of(
            Arguments.of("YYYY", "2021", "2021", null, null, null, null, null, null),
            Arguments.of("YY", "21", "21", null, null, null, null, null, null),
            Arguments.of("0Y", "06", "06", null, null, null, null, null, null),
            Arguments.of("YYYY.MM", "2021.1", "2021", "1", null, null, null, null, null),
            Arguments.of("YYYY.0M", "2021.01", "2021", "01", null, null, null, null, null),
            Arguments.of("YYYY.MM.DD", "2021.1.1", "2021", "1", "1", null, null, null, null),
            Arguments.of("YYYY.MM.0D", "2021.1.01", "2021", "1", "01", null, null, null, null),
            Arguments.of("YYYY.WW", "2021.1", "2021", null, null, "1", null, null, null),
            Arguments.of("YYYY.0W", "2021.01", "2021", null, null, "01", null, null, null),
            Arguments.of("YYYY.MINOR.MICRO", "2021.1.2", "2021", null, null, null, "1", "2", null),
            Arguments.of("YYYY.MM.DD_MICRO", "2021.1.2_3", "2021", "1", "2", null, null, "3", null),
            Arguments.of("YYYY.MODIFIER", "2021.ALPHA1", "2021", null, null, null, null, null, "ALPHA1"),
            Arguments.of("YYYY.MINOR.MICRO.MODIFIER", "2021.1.2.ALPHA1", "2021", null, null, null, "1", "2", "ALPHA1"),
            Arguments.of("YYYY.MINOR.MICRO.MODIFIER", "2022.0.0.beta2", "2022", null, null, null, "0", "0", "beta2"),
            Arguments.of("YYYY.MINOR.MICRO-MODIFIER", "2021.1.2-ALPHA1", "2021", null, null, null, "1", "2", "ALPHA1"),
            Arguments.of("YYYY.MINOR.MICRO-MODIFIER", "2022.0.0-beta2", "2022", null, null, null, "0", "0", "beta2"),
            Arguments.of("YYYY.MINOR.MICRO_MODIFIER", "2021.1.2_ALPHA1", "2021", null, null, null, "1", "2", "ALPHA1"),
            Arguments.of("YYYY.MINOR.MICRO_MODIFIER", "2022.0.0_beta2", "2022", null, null, null, "0", "0", "beta2"),
            Arguments.of("YYYY.MINOR.MICRO[.MODIFIER]", "2022.1.1.beta2", "2022", null, null, null, "1", "1", "beta2"),
            Arguments.of("YYYY.MINOR.MICRO[.MODIFIER]", "2022.1.1", "2022", null, null, null, "1", "1", null),
            Arguments.of("YYYY.MINOR.MICRO[-MODIFIER]", "2022.1.1-beta2", "2022", null, null, null, "1", "1", "beta2"),
            Arguments.of("YYYY.MINOR.MICRO[-MODIFIER]", "2022.1.1", "2022", null, null, null, "1", "1", null),
            Arguments.of("YYYY.MINOR.MICRO[_MODIFIER]", "2022.1.1_beta2", "2022", null, null, null, "1", "1", "beta2"),
            Arguments.of("YYYY.MINOR.MICRO[_MODIFIER]", "2022.1.1", "2022", null, null, null, "1", "1", null),
            Arguments.of("YYYY.MODIFIER", "2021.FOO-BAR", "2021", null, null, null, null, null, "FOO-BAR"),
            Arguments.of("0Y.0M.MICRO", "24.09.1", "24", "09", null, null, null, "1", null),
            Arguments.of("0Y.0M.MICRO", "01.01.0", "01", "01", null, null, null, "0", null),
            Arguments.of("0Y.MM.MICRO", "24.9.1", "24", "9", null, null, null, "1", null),
            Arguments.of("0Y.MM.MICRO", "01.1.0", "01", "1", null, null, null, "0", null),
            Arguments.of("YY.0M.MICRO", "24.09.1", "24", "09", null, null, null, "1", null),
            Arguments.of("YY.0M.MICRO", "1.01.0", "1", "01", null, null, null, "0", null),
            Arguments.of("YY.MM.MICRO", "24.9.1", "24", "9", null, null, null, "1", null),
            Arguments.of("YY.MM.MICRO", "1.1.0", "1", "1", null, null, null, "0", null)
        );
    }

    private static Stream<Arguments> default_version_parsing() {
        return Stream.of(
            Arguments.of("YYYY", "2000", null, null, null, null, null, null),
            Arguments.of("YY", "0", null, null, null, null, null, null),
            Arguments.of("0Y", "00", null, null, null, null, null, null),
            Arguments.of("YYYY.MM", "2000", "1", null, null, null, null, null),
            Arguments.of("YYYY.0M", "2000", "01", null, null, null, null, null),
            Arguments.of("YYYY.MM.DD", "2000", "1", "1", null, null, null, null),
            Arguments.of("YYYY.MM.0D", "2000", "1", "01", null, null, null, null),
            Arguments.of("YYYY.WW", "2000", null, null, "1", null, null, null),
            Arguments.of("YYYY.0W", "2000", null, null, "01", null, null, null),
            Arguments.of("YYYY.MINOR.MICRO", "2000", null, null, null, "0", "0", null),
            Arguments.of("YYYY.MM.DD_MICRO", "2000", "1", "1", null, null, "0", null),
            Arguments.of("YYYY.MODIFIER", "2000", null, null, null, null, null, "A"),
            Arguments.of("YYYY.MINOR.MICRO.MODIFIER", "2000", null, null, null, "0", "0", "A"),
            Arguments.of("YYYY.MINOR.MICRO-MODIFIER", "2000", null, null, null, "0", "0", "A"),
            Arguments.of("YYYY.MINOR.MICRO_MODIFIER", "2000", null, null, null, "0", "0", "A"),
            Arguments.of("YYYY.MINOR.MICRO[.MODIFIER]", "2000", null, null, null, "0", "0", "A"),
            Arguments.of("YYYY.MINOR.MICRO[-MODIFIER]", "2000", null, null, null, "0", "0", "A"),
            Arguments.of("YYYY.MINOR.MICRO[_MODIFIER]", "2000", null, null, null, "0", "0", "A"),
            Arguments.of("0Y.0M.MICRO", "00", "01", null, null, null, "0", null),
            Arguments.of("0Y.MM.MICRO", "00", "1", null, null, null, "0", null),
            Arguments.of("YY.0M.MICRO", "0", "01", null, null, null, "0", null),
            Arguments.of("YY.MM.MICRO", "0", "1", null, null, null, "0", null)
        );
    }

    private static Stream<Arguments> version_invalid() {
        return Stream.of(
            Arguments.of("YYYY", "999"),
            Arguments.of("YYYY", "1999"),
            Arguments.of("YYYY", "10000"),
            Arguments.of("YY", "1000"),
            Arguments.of("0Y", "1000"),
            Arguments.of("YY.MM", "21.13"),
            Arguments.of("YY.0M", "21.13"),
            Arguments.of("YY.WW", "21.53"),
            Arguments.of("YY.0W", "21.53"),
            Arguments.of("YY.DD", "21.32"),
            Arguments.of("YY.0D", "21.32"),
            Arguments.of("YY.MINOR", "21.A"),
            Arguments.of("YY.MICRO", "21.A"),
            Arguments.of("YY.MODIFIER", "21.A/B"),
            Arguments.of("YY.MODIFIER", "21/A"),
            Arguments.of("YYYY.0M.DD", "2001.02.29"),
            Arguments.of("YYYY.0M.DD", "2001.09.31"),
            Arguments.of("YYYY.0M.DD", "2001.01.32"),
            Arguments.of("YY.0M.MICRO", "00.01.0"),
            Arguments.of("YY.MM.MICRO", "00.1.0")
        );
    }

    private static Stream<Arguments> version_comparison() {
        return Stream.of(
            Arguments.of("YYYY", "2020", "2021"),
            Arguments.of("YYYY.MM", "2021.1", "2021.2"),
            Arguments.of("YYYY.MM.DD", "2021.1.1", "2021.1.2"),
            Arguments.of("YYYY.WW", "2021.1", "2021.2"),
            Arguments.of("YYYY.MINOR.MICRO", "2021.1.2", "2021.1.3"),
            Arguments.of("YYYY.MODIFIER", "2021.ALPHA", "2021.BETA"),
            Arguments.of("YYYY.MINOR.MICRO[.MODIFIER]", "2000.0.0.ALPHA", "2000.0.0.BETA"),
            Arguments.of("YYYY.MINOR.MICRO[.MODIFIER]", CalVer.defaultOf("YYYY.MINOR.MICRO[.MODIFIER]").toString(), "2000.0.0.B"),
            Arguments.of("0Y.0M.MICRO", "00.01.0", "01.01.0"),
            Arguments.of("0Y.MM.MICRO", "00.1.0", "01.1.0"),
            Arguments.of("YY.0M.MICRO", "0.01.0", "1.01.0"),
            Arguments.of("YY.MM.MICRO", "0.1.0", "1.1.0")
            );
    }
}
