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
package org.jreleaser.gradle.plugin.internal;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.util.IoUtils;
import org.kordamp.gradle.util.AnsiConsole;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public abstract class JReleaserLoggerService implements BuildService<JReleaserLoggerService.Params>, AutoCloseable {
    private final JReleaserLogger logger;

    public interface Params extends BuildServiceParameters {
        Property<AnsiConsole> getConsole();

        Property<LogLevel> getLogLevel();

        DirectoryProperty getOutputDirectory();
    }

    public JReleaserLoggerService() {
        try {
            Path outputDirectoryPath = getParameters().getOutputDirectory().get().getAsFile().toPath();
            Files.createDirectories(outputDirectoryPath);
            File traceLogFile = outputDirectoryPath.resolve("trace.log").toFile();
            PrintWriter tracer = IoUtils.newPrintWriter(new FileOutputStream(traceLogFile));

            logger = new JReleaserLoggerAdapter(getParameters().getConsole().get(),
                getParameters().getLogLevel().get(), tracer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public JReleaserLogger getLogger() {
        return logger;
    }

    @Override
    public void close() {
        logger.close();
    }
}
