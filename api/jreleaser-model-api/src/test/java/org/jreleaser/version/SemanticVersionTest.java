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

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
class SemanticVersionTest {
    @ParameterizedTest
    @MethodSource("version_parsing")
    void testVersionParsing(String input, int major, int minor, int patch, String tag, String build) {
        // given:
        SemanticVersion version = SemanticVersion.of(input);

        // then:
        assertThat(version.getMajor(), equalTo(major));
        assertThat(version.getMinor(), equalTo(minor));
        assertThat(version.getPatch(), equalTo(patch));
        assertThat(version.getTag(), equalTo(tag));
        assertThat(version.getBuild(), equalTo(build));
        assertThat(version.toString(), equalTo(input));
    }

    @ParameterizedTest
    @MethodSource("same_version")
    void testVersionIdentity(String input) {
        // given:
        SemanticVersion version = SemanticVersion.of(input);

        // then:
        assertThat(version, equalTo(version));
        assertThat(version.hashCode(), equalTo(version.hashCode()));
        Assertions.assertTrue(ComparatorUtils.greaterThanOrEqualTo(version, version));
        Assertions.assertTrue(ComparatorUtils.lessThanOrEqualTo(version, version));
        assertThat(version.compareTo(SemanticVersion.of(input)), equalTo(0));
    }

    @ParameterizedTest
    @MethodSource("version_comparison")
    void testVersionComparison(String input1, String input2) {
        // given:
        SemanticVersion v1 = SemanticVersion.of(input1);
        SemanticVersion v2 = SemanticVersion.of(input2);

        // then:
        Assertions.assertTrue(ComparatorUtils.lessThan(v1, v2));
        Assertions.assertTrue(ComparatorUtils.greaterThan(v2, v1));
    }

    private static Stream<Arguments> version_parsing() {
        return Stream.of(
            Arguments.of("0", 0, -1, -1, null, null),
            Arguments.of("0.2", 0, 2, -1, null, null),
            Arguments.of("0.2.3", 0, 2, 3, null, null),
            Arguments.of("0-TAG", 0, -1, -1, "TAG", null),
            Arguments.of("0.2-TAG", 0, 2, -1, "TAG", null),
            Arguments.of("0.2.3-TAG", 0, 2, 3, "TAG", null),
            Arguments.of("0+456", 0, -1, -1, null, "456"),
            Arguments.of("0.2+456", 0, 2, -1, null, "456"),
            Arguments.of("0.2.3+456", 0, 2, 3, null, "456"),
            Arguments.of("0-TAG+456", 0, -1, -1, "TAG", "456"),
            Arguments.of("0.2-TAG+456", 0, 2, -1, "TAG", "456"),
            Arguments.of("0.2.3-TAG+456", 0, 2, 3, "TAG", "456"),

            Arguments.of("1", 1, -1, -1, null, null),
            Arguments.of("1.2", 1, 2, -1, null, null),
            Arguments.of("1.2.3", 1, 2, 3, null, null),
            Arguments.of("1-TAG", 1, -1, -1, "TAG", null),
            Arguments.of("1.2-TAG", 1, 2, -1, "TAG", null),
            Arguments.of("1.2.3-TAG", 1, 2, 3, "TAG", null),
            Arguments.of("1+456", 1, -1, -1, null, "456"),
            Arguments.of("1.2+456", 1, 2, -1, null, "456"),
            Arguments.of("1.2.3+456", 1, 2, 3, null, "456"),
            Arguments.of("1-TAG+456", 1, -1, -1, "TAG", "456"),
            Arguments.of("1.2-TAG+456", 1, 2, -1, "TAG", "456"),
            Arguments.of("1.2.3-TAG+456", 1, 2, 3, "TAG", "456")
        );
    }

    private static Stream<Arguments> same_version() {
        return Stream.of(
            Arguments.of("0"),
            Arguments.of("0.2"),
            Arguments.of("0.2.3"),
            Arguments.of("0-TAG"),
            Arguments.of("0.2-TAG"),
            Arguments.of("0.2.3-TAG"),
            Arguments.of("0+456"),
            Arguments.of("0.2+456"),
            Arguments.of("0.2.3+456"),
            Arguments.of("0-TAG+456"),
            Arguments.of("0.2-TAG+456"),
            Arguments.of("0.2.3-TAG+456")
        );
    }

    private static Stream<Arguments> version_comparison() {
        return Stream.of(
            Arguments.of("0", "1"),
            Arguments.of("0.1", "0.2"),
            Arguments.of("0.1.2", "0.1.3"),
            Arguments.of("0", "0.1"),
            Arguments.of("0", "0.1.2"),
            Arguments.of("0.1", "0.1.2"),
            Arguments.of("0.1.0.Alpha1", "0.1.0.Alpha2"),
            Arguments.of("0.1.0+1", "0.1.0+2"),
            Arguments.of("0.1.0.Alpha1+1", "0.1.0.Alpha1+2")
        );
    }
}
