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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jreleaser.util.ComparatorUtils.greaterThan;
import static org.jreleaser.util.ComparatorUtils.lessThan;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public class JavaModuleVersionTest {
    @ParameterizedTest
    @MethodSource("version_parsing")
    public void testVersionParsing(String input, String version, String prerelease, String build) {
        // given:
        JavaModuleVersion v = JavaModuleVersion.of(input);

        // then:
        assertThat(v.getVersion(), equalTo(version));
        assertThat(v.getPrerelease(), equalTo(prerelease));
        assertThat(v.getBuild(), equalTo(build));
    }

    @ParameterizedTest
    @MethodSource("version_comparison")
    public void testVersionComparison(String input1, String input2) {
        // given:
        JavaModuleVersion v1 = JavaModuleVersion.of(input1);
        JavaModuleVersion v2 = JavaModuleVersion.of(input2);

        // then:
        assertTrue(lessThan(v1, v2));
        assertTrue(greaterThan(v2, v1));
    }

    private static Stream<Arguments> version_parsing() {
        return Stream.of(
            Arguments.of("1", "1", null, null),
            Arguments.of("1.2", "1.2", null, null),
            Arguments.of("1.2.3", "1.2.3", null, null),
            Arguments.of("1-TAG", "1", "TAG", null),
            Arguments.of("1.2-TAG", "1.2", "TAG", null),
            Arguments.of("1.2.3-TAG", "1.2.3", "TAG", null),
            Arguments.of("1+456", "1", null, "456"),
            Arguments.of("1.2+456", "1.2", null, "456"),
            Arguments.of("1.2.3+456", "1.2.3", null, "456"),
            Arguments.of("1-TAG+456", "1", "TAG", "456"),
            Arguments.of("1.2-TAG+456", "1.2", "TAG", "456"),
            Arguments.of("1.2.3-TAG+456", "1.2.3", "TAG", "456")
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
            Arguments.of("0.1", "0.1+BUILD"),
            Arguments.of("0.1-PRE", "0.1-PRE+BUILD")
        );
    }
}
