/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.internal

import groovy.transform.CompileStatic
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.jreleaser.logging.JReleaserLogger
import org.kordamp.gradle.util.AnsiConsole

import java.nio.file.Files
import java.nio.file.Path

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
@CompileStatic
abstract class JReleaserLoggerService implements BuildService<JReleaserLoggerService.Params>, AutoCloseable {
    interface Params extends BuildServiceParameters {
        Property<AnsiConsole> getConsole()

        Property<LogLevel> getLogLevel()

        DirectoryProperty getOutputDirectory()
    }

    private final JReleaserLogger logger

    JReleaserLoggerService() {
        Path outputDirectoryPath = parameters.outputDirectory.get().asFile.toPath()
        Files.createDirectories(outputDirectoryPath)
        PrintWriter tracer = new PrintWriter(new FileOutputStream(outputDirectoryPath
            .resolve('trace.log').toFile()), true)

        logger = new JReleaserLoggerAdapter(parameters.console.get(), parameters.logLevel.get(), tracer)
    }

    JReleaserLogger getLogger() {
        return logger
    }

    @Override
    void close() {
        logger.close()
    }
}
