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
package org.jreleaser.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jreleaser.ant.tasks.internal.JReleaserLoggerAdapter;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.templates.TemplateGenerationException;
import org.jreleaser.templates.TemplateGenerator;
import org.jreleaser.util.JReleaserLogger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserTemplateTask extends Task {
    private boolean skip;
    private String distributionName;
    private Distribution.DistributionType distributionType = Distribution.DistributionType.JAVA_BINARY;
    private String toolName;
    private boolean overwrite;
    private boolean snapshot;
    private JReleaserLogger logger;

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void setDistributionName(String distributionName) {
        this.distributionName = distributionName;
    }

    public void setDistributionType(Distribution.DistributionType distributionType) {
        this.distributionType = distributionType;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setSnapshot(boolean snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void execute() throws BuildException {
        Banner.display(new PrintWriter(System.out, true));
        if (skip) return;

        try {
            initLogger();

            Path outputDirectory = getOutputDirectory()
                .resolve("src")
                .resolve("jreleaser")
                .resolve("distributions");

            Path output = TemplateGenerator.builder()
                .logger(logger)
                .distributionName(distributionName)
                .distributionType(distributionType)
                .toolName(toolName)
                .outputDirectory(outputDirectory)
                .overwrite(overwrite)
                .snapshot(snapshot)
                .build()
                .generate();

            if (null != output) {
                logger.info("Template generated at {}", output.toAbsolutePath());
            }
        } catch (TemplateGenerationException e) {
            logger.trace(e);
            throw new JReleaserException("Unexpected error", e);
        }
    }

    private Path getOutputDirectory() {
        return getProject().getBaseDir().toPath().normalize();
    }

    private JReleaserLogger initLogger() {
        if (null == logger) {
            logger = new JReleaserLoggerAdapter(createTracer(), getProject());
        }
        return logger;
    }

    private PrintWriter createTracer() {
        try {
            Path outputDirectory = getOutputDirectory().resolve("out")
                .resolve("jreleaser");
            Files.createDirectories(outputDirectory);
            return new PrintWriter(new FileOutputStream(
                outputDirectory
                    .resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize trace file", e);
        }
    }
}
