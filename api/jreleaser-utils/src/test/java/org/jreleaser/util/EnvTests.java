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
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.ReadsEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jreleaser.util.StringUtils.isNotBlank;

class EnvTests {
    @ParameterizedTest
    @MethodSource("variable_factory")
    @ReadsEnvironmentVariable
    @SetEnvironmentVariable(key = "JRELEASER_FOO", value = "foo-env")
    @SetEnvironmentVariable(key = "JRELEASER_FOO_BAR", value = "foobar-env")
    @ClearSystemProperty(key = "jreleaser.foo")
    @ClearSystemProperty(key = "jreleaser.foo.bar")
    void testVariableResolution(String expected, String key, String value, boolean setsys) {
        // given:
        if (setsys && isNotBlank(expected)) System.setProperty(Env.sysKey(key), expected);

        // when:
        String actual = Env.resolve(key, value);

        // then:
        assertThat(actual, equalTo(expected));
    }

    private static Stream<Arguments> variable_factory() {
        return Stream.of(
            Arguments.of(null, "bar", null, false),
            Arguments.of(null, "bar", null, true),
            Arguments.of("foo-env", "foo", null, false),
            Arguments.of("value", "foo", "value", true),
            Arguments.of("foo-sys", "foo", null, true),
            Arguments.of("value", "foo", "value", false),
            Arguments.of("foobar-sys", "foo.bar", null, true),
            Arguments.of("foobar-env", "foo.bar", null, false)
        );
    }
}
