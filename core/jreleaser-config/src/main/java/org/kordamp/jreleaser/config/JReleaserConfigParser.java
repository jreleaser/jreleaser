/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.kordamp.jreleaser.config;

import org.kordamp.jreleaser.model.JReleaserModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Allows external configuration to be parsed with a custom format.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface JReleaserConfigParser {
    /**
     * Whether the given config file format is supported or not.</p>
     * Implementors would typically look at the file extension.
     *
     * @param configFile the configuration file to inspect
     * @return {@code true} if the given format is supported, {@code false} otherwise.
     */
    boolean supports(Path configFile);

    /**
     * Reads and parses external configuration into a {@code JReleaserModel} instance.
     *
     * @param inputStream the configuration's input source
     * @return a configured {@code JReleaserModel} instance, should never return {@code null}.
     * @throws IOException if an error occurs while reading from the {@code InputStream}.
     */
    JReleaserModel parse(InputStream inputStream) throws IOException;
}
