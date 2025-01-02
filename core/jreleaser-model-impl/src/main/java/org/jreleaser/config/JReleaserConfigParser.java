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

import org.jreleaser.model.internal.JReleaserModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

/**
 * Allows external configuration to be parsed with a custom format.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface JReleaserConfigParser {
    /**
     * Returns the preferred file extension supported by this parser.
     *
     * @return the preferred file extension supported by this parser, should never return {@code null}.
     */
    String getPreferredFileExtension();

    /**
     * Whether the given config file format is supported or not.</p>
     * Implementors would typically look at the file extension.
     *
     * @param configFile the configuration file to inspect
     * @return {@code true} if the given format is supported, {@code false} otherwise.
     */
    boolean supports(Path configFile);

    /**
     * Whether the given resource format is supported or not.</p>
     * Implementors would typically look at the file extension.
     *
     * @param resource the resource to inspect
     * @return {@code true} if the given format is supported, {@code false} otherwise.
     */
    boolean supports(String resource);

    /**
     * Checks the contents of the config file for syntax compliance.
     *
     * @param configFile the configuration file to inspect
     */
    void validate(Path configFile) throws IOException;

    /**
     * Reads and parses external configuration into a {@code JReleaserModel} instance.
     *
     * @param inputStream the configuration's input source
     * @return a configured {@code JReleaserModel} instance, should never return {@code null}.
     * @throws IOException if an error occurs while reading from the {@code InputStream}.
     */
    JReleaserModel parse(InputStream inputStream) throws IOException;

    /**
     * Loads a resource into a given type.
     *
     * @param inputStream the resources' input source
     * @return the parsed instance, should never return {@code null}.
     * @throws IOException if an error occurs while reading from the {@code InputStream}.
     */
    <T> T load(Class<T> type, InputStream inputStream) throws IOException;

    /**
     * Reads and parses external configuration into a {@code Map} instance.
     * The input sorce defines key/values as an alternative to the Java properties format.
     *
     * @param inputStream the configuration's input source
     * @return a {@code Map} instance, should never return {@code null}.
     * @throws IOException if an error occurs while reading from the {@code InputStream}.
     * @since 0.2.0
     */
    Map<String, String> properties(InputStream inputStream) throws IOException;
}
