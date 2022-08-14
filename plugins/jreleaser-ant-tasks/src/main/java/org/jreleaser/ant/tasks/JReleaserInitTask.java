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
package org.jreleaser.ant.tasks;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jreleaser.ant.tasks.internal.JReleaserLoggerAdapter;
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.templates.TemplateResource;
import org.jreleaser.templates.TemplateUtils;
import org.jreleaser.util.JReleaserException;
import org.jreleaser.util.JReleaserLogger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

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
        Banner.display(new PrintWriter(System.out, true));

        try {
            initLogger();

            if (!getSupportedConfigFormats().contains(format)) {
                throw new BuildException("Unsupported file format. Must be one of [" +
                    String.join("|", getSupportedConfigFormats()) + "]");
            }

            Path outputDirectory = getOutputDirectory();
            Path outputFile = outputDirectory.resolve("jreleaser." + format);

            TemplateResource template = TemplateUtils.resolveTemplate(logger, "jreleaser." + format + ".tpl");

            logger.info("Writing file " + outputFile.toAbsolutePath());
            try (Writer writer = Files.newBufferedWriter(outputFile, (overwrite ? CREATE : CREATE_NEW), WRITE, TRUNCATE_EXISTING)) {
                IOUtils.copy(template.getReader(), writer);
            } catch (FileAlreadyExistsException e) {
                logger.error("File {} already exists and overwrite was set to false.", outputFile.toAbsolutePath());
                return;
            }

            logger.info("JReleaser initialized at " + outputDirectory.toAbsolutePath());
        } catch (IllegalStateException | IOException e) {
            logger.trace(e);
            throw new JReleaserException("Unexpected error", e);
        }
    }

    private PrintWriter createTracer() {
        try {
            Path outputDirectory = getOutputDirectory()
                .resolve("out")
                .resolve("jreleaser");
            Files.createDirectories(outputDirectory);
            return new PrintWriter(new FileOutputStream(
                outputDirectory.resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize trace file", e);
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

    private Set<String> getSupportedConfigFormats() {
        Set<String> extensions = new LinkedHashSet<>();

        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class,
            JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            extensions.add(parser.getPreferredFileExtension());
        }

        return extensions;
    }
}
