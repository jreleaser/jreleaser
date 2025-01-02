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
 * @since 0.8.0
 */
class JavaRuntimeVersionTest {
    @ParameterizedTest
    @MethodSource("version_parsing")
    void testVersionParsing(String input, String version, String prerelease, String build, String optional) {
        // given:
        JavaRuntimeVersion v = JavaRuntimeVersion.of(input);

        // then:
        assertThat(v.getVersion(), equalTo(version));
        assertThat(v.getPrerelease(), equalTo(prerelease));
        assertThat(v.getBuild(), equalTo(build));
        assertThat(v.getOptional(), equalTo(optional));
    }

    @ParameterizedTest
    @MethodSource("version_comparison")
    void testVersionComparison(String input1, String input2) {
        // given:
        JavaRuntimeVersion v1 = JavaRuntimeVersion.of(input1);
        JavaRuntimeVersion v2 = JavaRuntimeVersion.of(input2);

        // then:
        Assertions.assertTrue(ComparatorUtils.lessThan(v1, v2));
        Assertions.assertTrue(ComparatorUtils.greaterThan(v2, v1));
    }

    private static Stream<Arguments> version_parsing() {
        return Stream.of(
            Arguments.of("1", "1", null, null, null),
            Arguments.of("1.2", "1.2", null, null, null),
            Arguments.of("1.2.3", "1.2.3", null, null, null),
            Arguments.of("1-PRE", "1", "PRE", null, null),
            Arguments.of("1.2-PRE", "1.2", "PRE", null, null),
            Arguments.of("1.2.3-PRE", "1.2.3", "PRE", null, null),
            Arguments.of("1+456", "1", null, "456", null),
            Arguments.of("1.2+456", "1.2", null, "456", null),
            Arguments.of("1.2.3+456", "1.2.3", null, "456", null),
            Arguments.of("1-PRE+456", "1", "PRE", "456", null),
            Arguments.of("1.2-PRE+456", "1.2", "PRE", "456", null),
            Arguments.of("1.2.3-PRE+456", "1.2.3", "PRE", "456", null),
            Arguments.of("1-PRE-OPT", "1", "PRE", null, "OPT"),
            Arguments.of("1.2-PRE-OPT", "1.2", "PRE", null, "OPT"),
            Arguments.of("1.2.3-PRE-OPT", "1.2.3", "PRE", null, "OPT"),
            Arguments.of("1-PRE+456-OPT", "1", "PRE", "456", "OPT"),
            Arguments.of("1.2-PRE+456-OPT", "1.2", "PRE", "456", "OPT"),
            Arguments.of("1.2.3-PRE+456-OPT", "1.2.3", "PRE", "456", "OPT"),
            Arguments.of("1+-OPT", "1", null, null, "OPT"),
            Arguments.of("1.2+-OPT", "1.2", null, null, "OPT"),
            Arguments.of("1.2.3+-OPT", "1.2.3", null, null, "OPT")
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
            Arguments.of("0.1-PRE", "0.1"),
            Arguments.of("0.1", "0.1+123"),
            Arguments.of("0.1-PRE", "0.1-PRE+123")
        );
    }
}
