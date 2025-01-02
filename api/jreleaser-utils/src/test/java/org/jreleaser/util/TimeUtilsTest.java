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
package org.jreleaser.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TimeUtilsTest {
    @ParameterizedTest
    @MethodSource("time_factory")
    void testTimeFactory(String expected, double time) {
        // given:
        String actual = TimeUtils.formatDuration(time);

        // then:
        assertThat(actual, equalTo(expected));
    }

    private static Stream<Arguments> time_factory() {
        return Stream.of(
            Arguments.of("0.500 s", 0.5),
            Arguments.of("0.000 s", -1),
            Arguments.of("0.000 s", 0),
            Arguments.of("1.000 s", 1),
            Arguments.of("59.000 s", 59),
            Arguments.of("01:00 m", 60),
            Arguments.of("01:01 m", 61)
        );
    }
}
