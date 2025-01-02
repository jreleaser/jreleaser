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
package org.jreleaser.config.yaml;

import org.jreleaser.model.internal.JReleaserModel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class YamlJReleaserConfigParserTest {
    @Test
    void support() {
        // given:
        Path resourcesDir = Paths.get(".")
            .resolve("src/test/resources")
            .normalize();
        Path modelPath = resourcesDir.resolve("jreleaser.yml");
        YamlJReleaserConfigParser parser = new YamlJReleaserConfigParser();

        // expect:
        assertThat(parser.supports(modelPath)).isTrue();
        assertThat(parser.supports(modelPath.toAbsolutePath().toString())).isTrue();
    }

    @Test
    void parse() throws IOException {
        // given:
        Path resourcesDir = Paths.get(".")
            .resolve("src/test/resources")
            .normalize();
        Path modelPath = resourcesDir.resolve("jreleaser.yml");

        // when:
        JReleaserModel model = new YamlJReleaserConfigParser()
            .parse(Files.newInputStream(modelPath));

        // then:
        assertThat(model)
            .isNotNull();
        assertThat(model.getProject().getName())
            .isEqualTo("app");
        assertThat(model.getProject().getVersion())
            .isEqualTo("1.0.0-SNAPSHOT");
    }

    @Test
    void load() throws IOException {
        // given:
        Path resourcesDir = Paths.get(".")
            .resolve("src/test/resources")
            .normalize();
        Path modelPath = resourcesDir.resolve("jreleaser.yml");

        // when:
        JReleaserModel model = new YamlJReleaserConfigParser()
            .load(JReleaserModel.class, Files.newInputStream(modelPath));

        // then:
        assertThat(model)
            .isNotNull();
        assertThat(model.getProject().getName())
            .isEqualTo("app");
        assertThat(model.getProject().getVersion())
            .isEqualTo("1.0.0-SNAPSHOT");
    }

    @Test
    void properties() throws IOException {
        // given:
        Path resourcesDir = Paths.get(".")
            .resolve("src/test/resources")
            .normalize();
        Path propertiesPath = resourcesDir.resolve("config.yml");

        // when:
        Map<String, String> properties = new YamlJReleaserConfigParser()
            .properties(Files.newInputStream(propertiesPath));

        System.out.println(properties);
        // then:
        assertThat(properties)
            .isNotNull()
            .containsKey("JRELEASER_GITHUB_TOKEN")
            .containsValue("A1234567890");
    }
}
