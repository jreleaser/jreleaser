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
 * @since 1.0.0
 */
class ChronVerTest {
    @ParameterizedTest
    @MethodSource("version_parsing")
    void testVersionParsing(String input, int year, int month, int day, String changeset,
                            int change1, String tag, int change2) {
        // given:
        ChronVer v = ChronVer.of(input);

        // then:
        assertThat(v.getYear(), equalTo(year));
        assertThat(v.getMonth(), equalTo(month));
        assertThat(v.getDay(), equalTo(day));
        assertThat(v.getChangeset(), equalTo(ChronVer.Changeset.of(changeset)));

        if (v.hasChangeset()) {
            ChronVer.Changeset c = v.getChangeset();
            assertThat(c.getChange(), equalTo(change1));
            if (c.hasTag()) {
                assertThat(c.getTag(), equalTo(tag));
            }
            if (c.hasChange2()) {
                assertThat(c.getChange2(), equalTo(change2));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("version_invalid")
    void testVersionInvalid(String input) {
        // expect:
        assertThrows(IllegalArgumentException.class, () -> ChronVer.of(input));
    }

    @ParameterizedTest
    @MethodSource("version_comparison")
    void testVersionComparison(String input1, String input2) {
        // given:
        ChronVer v1 = ChronVer.of(input1);
        ChronVer v2 = ChronVer.of(input2);

        // then:
        Assertions.assertTrue(ComparatorUtils.lessThan(v1, v2));
        Assertions.assertTrue(ComparatorUtils.greaterThan(v2, v1));
    }

    private static Stream<Arguments> version_parsing() {
        return Stream.of(
            Arguments.of("2022.01.02", 2022, 1, 2, null, 0, null, 0),
            Arguments.of("2022.01.02.1", 2022, 1, 2, "1", 1, null, 0),
            Arguments.of("2022.01.02.1-break", 2022, 1, 2, "1-break", 1, "break", 0),
            Arguments.of("2022.01.02.1-break.2", 2022, 1, 2, "1-break.2", 1, "break", 2),
            Arguments.of("2022.01.02-tag", 2022, 1, 2, null, 0, "tag", 0)
        );
    }

    private static Stream<Arguments> version_invalid() {
        return Stream.of(
            Arguments.of("1999.01.01"),
            Arguments.of("2000.01.32"),
            Arguments.of("2000.09.31"),
            Arguments.of("2001.02.29"),
            Arguments.of("2000.13.01"),
            Arguments.of("2000.12.01.1-"),
            Arguments.of("2000.12.01.1-1-"),
            Arguments.of("2000.12.01.A"),
            Arguments.of("2000.12.01.01"),
            Arguments.of("2000.12.01.1.2")
        );
    }

    private static Stream<Arguments> version_comparison() {
        return Stream.of(
            Arguments.of("2022.01.01", "2022.02.01"),
            Arguments.of("2022.01.01.1", "2022.02.01.2"),
            Arguments.of("2022.01.01.1-foo.1", "2022.02.01.1-foo.2")
        );
    }
}
