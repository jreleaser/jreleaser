/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserConfigLoader {
    public static JReleaserModel loadConfig(Path configFile) {
        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class, JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            if (parser.supports(configFile)) {
                try {
                    parser.validate(configFile);
                } catch (IOException e) {
                    throw new JReleaserException("Invalid config file. " + configFile, e);
                }
                try (InputStream inputStream = configFile.toUri().toURL().openStream()) {
                    return parser.parse(inputStream);
                } catch (IOException e) {
                    throw new JReleaserException("Unexpected error parsing config file. " + configFile, e);
                }
            }
        }
        throw new JReleaserException("Unsupported config format. " + configFile);
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
        throw new JReleaserException("Unsupported format. " + file);
    }
}
