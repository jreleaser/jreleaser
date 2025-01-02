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
package org.jreleaser.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jreleaser.ant.tasks.internal.JReleaserLoggerAdapter;
import org.jreleaser.engine.init.Init;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.JReleaserException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.jreleaser.bundle.RB.$;
import static org.jreleaser.util.IoUtils.newPrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserInitTask extends Task {
    private boolean overwrite;
    private String format;
    private JReleaserLogger logger;

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public void execute() throws BuildException {
        Banner.display(newPrintWriter(System.err));
        try {
            initLogger();
            Init.execute(logger, format, overwrite, getOutputDirectory());
        } catch (IllegalStateException e) {
            throw new JReleaserException($("ERROR_unexpected_error"), e);
        } finally {
            if (null != logger) logger.close();
        }
    }

    private PrintWriter createTracer() {
        try {
            Path outputDirectory = getOutputDirectory()
                .resolve("out")
                .resolve("jreleaser");
            Files.createDirectories(outputDirectory);
            return newPrintWriter(new FileOutputStream(
                outputDirectory.resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize trace file", e);
        }
    }

    private Path getOutputDirectory() {
        return getProject().getBaseDir().toPath().normalize();
    }

    private void initLogger() {
        if (null == logger) {
            logger = new JReleaserLoggerAdapter(createTracer(), getProject());
        }
    }
}
