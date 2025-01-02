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
package org.jreleaser.logging;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jreleaser.logging.SimpleJReleaserLoggerAdapter.Level.DEBUG;
import static org.jreleaser.logging.SimpleJReleaserLoggerAdapter.Level.ERROR;
import static org.jreleaser.logging.SimpleJReleaserLoggerAdapter.Level.INFO;
import static org.jreleaser.logging.SimpleJReleaserLoggerAdapter.Level.WARN;

class SimpleJReleaserLoggerAdapterTest {
    private static final String MESSAGE = "MESSAGE";
    private static final String ARGS = "ARGS {}";
    private static final String ARGS_ARGS = "ARGS args";
    private static final String EXCEPTION = "EXCEPTION";
    private static final String RUNTIME_EXCEPTION_BOOM = "RuntimeException: boom";

    @ParameterizedTest
    @MethodSource("logging_values")
    void testLogging(SimpleJReleaserLoggerAdapter.Level currentLevel,
                     SimpleJReleaserLoggerAdapter.Level activeLevel,
                     Consumer<JReleaserLogger> consumer, boolean active) {
        // given:
        StringWriter witness = new StringWriter();
        PrintWriter writer = new PrintWriter(witness);
        JReleaserLogger logger = new SimpleJReleaserLoggerAdapter(writer, activeLevel);

        // when:
        consumer.accept(logger);

        // then:
        if (active) {
            assertThat(witness.toString())
                .contains(currentLevel.toString())
                .contains(MESSAGE)
                .contains(ARGS_ARGS)
                .contains(EXCEPTION)
                .contains(RUNTIME_EXCEPTION_BOOM);
        } else {
            assertThat(witness.toString())
                .doesNotContain(currentLevel.toString())
                .doesNotContain(MESSAGE)
                .doesNotContain(ARGS_ARGS)
                .doesNotContain(EXCEPTION)
                .doesNotContain(RUNTIME_EXCEPTION_BOOM);
        }
    }

    private static void debug(JReleaserLogger logger) {
        Exception exception = new RuntimeException("boom");
        logger.debug(MESSAGE);
        logger.debug(ARGS, "args");
        logger.debug(EXCEPTION, exception);
    }

    private static void info(JReleaserLogger logger) {
        Exception exception = new RuntimeException("boom");
        logger.info(MESSAGE);
        logger.info(ARGS, "args");
        logger.info(EXCEPTION, exception);
    }

    private static void warn(JReleaserLogger logger) {
        Exception exception = new RuntimeException("boom");
        logger.warn(MESSAGE);
        logger.warn(ARGS, "args");
        logger.warn(EXCEPTION, exception);
    }

    private static void error(JReleaserLogger logger) {
        Exception exception = new RuntimeException("boom");
        logger.error(MESSAGE);
        logger.error(ARGS, "args");
        logger.error(EXCEPTION, exception);
    }

    private static Stream<Arguments> logging_values() {
        return Stream.of(
            Arguments.of(DEBUG, DEBUG, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::debug, true),
            Arguments.of(DEBUG, INFO, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::debug, false),
            Arguments.of(DEBUG, WARN, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::debug, false),
            Arguments.of(DEBUG, ERROR, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::debug, false),
            Arguments.of(INFO, DEBUG, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::info, true),
            Arguments.of(INFO, INFO, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::info, true),
            Arguments.of(INFO, WARN, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::info, false),
            Arguments.of(INFO, ERROR, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::info, false),
            Arguments.of(WARN, DEBUG, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::warn, true),
            Arguments.of(WARN, INFO, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::warn, true),
            Arguments.of(WARN, WARN, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::warn, true),
            Arguments.of(WARN, ERROR, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::warn, false),
            Arguments.of(ERROR, DEBUG, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::error, true),
            Arguments.of(ERROR, INFO, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::error, true),
            Arguments.of(ERROR, WARN, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::error, true),
            Arguments.of(ERROR, ERROR, (Consumer<JReleaserLogger>) SimpleJReleaserLoggerAdapterTest::error, true)
        );
    }
}
