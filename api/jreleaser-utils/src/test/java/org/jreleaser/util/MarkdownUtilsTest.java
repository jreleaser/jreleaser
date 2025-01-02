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

import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class MarkdownUtilsTest {
    @ParameterizedTest
    @MethodSource("text_factory")
    void testAutoLinks(String input, String expected) {
        // given:
        Parser parser = MarkdownUtils.createMarkdownParser();
        TextContentRenderer renderer = MarkdownUtils.createTextContentRenderer();
        String actual = renderer.render(parser.parse(input));

        // then:
        assertThat(actual, equalTo(expected));
    }

    private static Stream<Arguments> text_factory() {
        return Stream.of(
            Arguments.of("Foo! ftp://acme.com/links", "Foo! [ftp://acme.com/links](ftp://acme.com/links)"),
            Arguments.of("Foo! https://acme.com/links", "Foo! [https://acme.com/links](https://acme.com/links)"),
            Arguments.of("Foo! [https://acme.com/links](https://acme.com/links)", "Foo! [https://acme.com/links](https://acme.com/links)"),
            Arguments.of("Foo! [Bar](https://acme.com/links)", "Foo! [Bar](https://acme.com/links)")
        );
    }
}
