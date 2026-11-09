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

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Anyul Rivas
 */
class ConfigTest {
    @Test
    void shouldAcceptLongFlags() {
        Config config = new Config();
        CommandLine cmd = new CommandLine(config);
        cmd.parseArgs("--assembly");
        assertThat(config.exclusive.assembly).isTrue();

        config = new Config();
        cmd = new CommandLine(config);
        cmd.parseArgs("--download");
        assertThat(config.exclusive.download).isTrue();
    }

    @Test
    void shouldRejectShortFlags() {
        Config config = new Config();
        CommandLine cmd = new CommandLine(config);
        assertThatThrownBy(() -> cmd.parseArgs("-a"))
            .isInstanceOf(CommandLine.UnmatchedArgumentException.class);
        assertThatThrownBy(() -> cmd.parseArgs("-d"))
            .isInstanceOf(CommandLine.UnmatchedArgumentException.class);
    }
}
