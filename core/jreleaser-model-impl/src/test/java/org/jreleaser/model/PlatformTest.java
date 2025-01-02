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
package org.jreleaser.model;

import org.jreleaser.model.internal.platform.Platform;
import org.jreleaser.util.CollectionUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
class PlatformTest {
    @ParameterizedTest
    @MethodSource("platform_inputs")
    void testReplacements(String input, String output) {
        // given:
        Platform platform = new Platform();
        platform.setReplacements(CollectionUtils.<String, String>map()
            .e("osx-x86_64", "mac")
            .e("aarch_64", "aarch64")
            .e("x86_64", "amd64")
            .e("osx", "darwin"));

        // then:
        assertThat(platform.applyReplacements(input), equalTo(output));
    }

    private static Stream<Arguments> platform_inputs() {
        return Stream.of(
            Arguments.of("osx-x86_64", "mac"),
            Arguments.of("linux-aarch_64", "linux-aarch64"),
            Arguments.of("windows-x86_64", "windows-amd64"),
            Arguments.of("osx-aarch_64", "darwin-aarch64")
        );
    }
}
