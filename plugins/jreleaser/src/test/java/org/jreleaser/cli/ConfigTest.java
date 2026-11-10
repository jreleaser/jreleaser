/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.cli;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Anyul Rivas
 */
class ConfigTest {
    @Test
    void shouldAcceptLongFlags() {
        // given:
        Config assemblyConfig = new Config();
        CommandLine assemblyCmd = new CommandLine(assemblyConfig);

        Config downloadConfig = new Config();
        CommandLine downloadCmd = new CommandLine(downloadConfig);

        // when:
        assemblyCmd.parseArgs("--assembly");
        downloadCmd.parseArgs("--download");

        // then:
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(assemblyConfig.exclusive.assembly).isTrue();
            softly.assertThat(downloadConfig.exclusive.download).isTrue();
        });
    }

    @Test
    void shouldRejectShortFlags() {
        // given:
        CommandLine assemblyCmd = new CommandLine(new Config());
        CommandLine downloadCmd = new CommandLine(new Config());

        // when:
        Throwable assemblyException = catchThrowable(() -> assemblyCmd.parseArgs("-a"));
        Throwable downloadException = catchThrowable(() -> downloadCmd.parseArgs("-d"));

        // then:
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(assemblyException).isInstanceOf(CommandLine.UnmatchedArgumentException.class);
            softly.assertThat(downloadException).isInstanceOf(CommandLine.UnmatchedArgumentException.class);
        });
    }
}
