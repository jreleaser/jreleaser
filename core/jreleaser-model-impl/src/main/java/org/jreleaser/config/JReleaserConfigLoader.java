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
package org.jreleaser.config;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JReleaserConfigLoader {
    private JReleaserConfigLoader() {
        // noop
    }

    public static JReleaserModel loadConfig(Path configFile) {
        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class, JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            if (parser.supports(configFile)) {
                try {
                    parser.validate(configFile);
                } catch (IOException e) {
                    throw new JReleaserException(RB.$("ERROR_invalid_config_file", configFile), e);
                }
                try (InputStream inputStream = configFile.toUri().toURL().openStream()) {
                    return parser.parse(inputStream);
                } catch (IOException e) {
                    throw new JReleaserException(RB.$("ERROR_parsing_config_file", configFile), e);
                }
            }
        }
        throw new JReleaserException(RB.$("ERROR_unsupported_config_format", configFile));
    }

    public static Map<String, String> loadProperties(Path file) throws IOException {
        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class, JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            if (parser.supports(file)) {
                try (InputStream inputStream = file.toUri().toURL().openStream()) {
                    return parser.properties(inputStream);
                }
            }
        }
        throw new JReleaserException(RB.$("ERROR_unsupported_config_format", file));
    }

    public static <T> T load(Class<T> type, String resource, InputStream inputStream) throws IOException {
        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class, JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            if (parser.supports(resource)) {
                try {
                    return parser.load(type, inputStream);
                } catch (IOException e) {
                    throw new JReleaserException(RB.$("ERROR_load_resource", resource), e);
                }
            }
        }
        throw new JReleaserException(RB.$("ERROR_unsupported_config_format", resource));
    }
}
