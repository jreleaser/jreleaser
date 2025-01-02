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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
class JavaModuleVersionTest {
    @ParameterizedTest
    @MethodSource("version_parsing")
    void testVersionParsing(String input, String version, String prerelease, String build) {
        // given:
        JavaModuleVersion v = JavaModuleVersion.of(input);

        // then:
        assertThat(v.getVersion(), equalTo(version));
        assertThat(v.getPrerelease(), equalTo(prerelease));
        assertThat(v.getBuild(), equalTo(build));
    }

    @ParameterizedTest
    @MethodSource("version_comparison")
    void testVersionComparison(String input1, String input2) {
        // given:
        JavaModuleVersion v1 = JavaModuleVersion.of(input1);
        JavaModuleVersion v2 = JavaModuleVersion.of(input2);

        // then:
        Assertions.assertTrue(ComparatorUtils.lessThan(v1, v2));
        Assertions.assertTrue(ComparatorUtils.greaterThan(v2, v1));
    }

    @Test
    void testVersionSort() {
        List<JavaModuleVersion> asc = new ArrayList<>(asList(
            JavaModuleVersion.of("0-ea"),
            JavaModuleVersion.of("2021.01.22"),
            JavaModuleVersion.of("2021.01.24"),
            JavaModuleVersion.of("2021.02"),
            JavaModuleVersion.of("2021.02.24"),
            JavaModuleVersion.of("2021.03"),
            JavaModuleVersion.of("2021.04.01"),
            JavaModuleVersion.of("2021.04.13"),
            JavaModuleVersion.of("2021.05.01"),
            JavaModuleVersion.of("2021.05.20")));

        List<JavaModuleVersion> desc = new ArrayList<>(asList(
            JavaModuleVersion.of("2021.05.20"),
            JavaModuleVersion.of("2021.05.01"),
            JavaModuleVersion.of("2021.04.13"),
            JavaModuleVersion.of("2021.04.01"),
            JavaModuleVersion.of("2021.03"),
            JavaModuleVersion.of("2021.02.24"),
            JavaModuleVersion.of("2021.02"),
            JavaModuleVersion.of("2021.01.24"),
            JavaModuleVersion.of("2021.01.22"),
            JavaModuleVersion.of("0-ea")));

        // given:
        List<JavaModuleVersion> sortedAsc = new ArrayList<>(desc);
        Collections.sort(sortedAsc, JavaModuleVersion::compareTo);

        // then:
        assertThat(
            sortedAsc.stream().map(JavaModuleVersion::toString).collect(Collectors.joining(",")),
            equalTo(asc.stream().map(JavaModuleVersion::toString).collect(Collectors.joining(","))));

        // given:
        List<JavaModuleVersion> sortedDesc = new ArrayList<>(asc);
        Collections.sort(sortedDesc, Comparator.reverseOrder());

        // then:
        assertThat(
            sortedDesc.stream().map(JavaModuleVersion::toString).collect(Collectors.joining(",")),
            equalTo(desc.stream().map(JavaModuleVersion::toString).collect(Collectors.joining(","))));
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
