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
package org.jreleaser.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jreleaser.engine.init.Init;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.maven.plugin.internal.JReleaserLoggerAdapter;
import org.jreleaser.model.JReleaserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static org.jreleaser.bundle.RB.$;
import static org.jreleaser.util.IoUtils.newPrintWriter;

/**
 * Create a jreleaser config file.
 *
 * @author Andres Almiray
 * @since 1.4.0
 */
@Mojo(threadSafe = true, name = "init")
public class JReleaserInitMojo extends AbstractMojo {
    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Parameter(property = "jreleaser.output.directory", defaultValue = "${project.basedir}")
    protected File outputDirectory;
    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.init.skip")
    private boolean skip;
    /**
     * Configuration file format.
     */
    @Parameter(property = "jreleaser.init.format")
    private String format;
    /**
     * Overwrite existing files.
     */
    @Parameter(property = "jreleaser.template.overwrite")
    private boolean overwrite;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) {
            getLog().info("Execution has been explicitly skipped.");
            return;
        }

        if (null == outputDirectory) {
            outputDirectory = project.getBasedir();
        }

        JReleaserLogger logger = getLogger();
        try {
            Init.execute(logger, format, overwrite, outputDirectory.toPath());
        } catch (IllegalStateException e) {
            throw new JReleaserException($("ERROR_unexpected_error"), e);
        } finally {
            if (null != logger) logger.close();
        }
    }

    protected JReleaserLogger getLogger() throws MojoExecutionException {
        return new JReleaserLoggerAdapter(createTracer(), getLog());
    }

    protected PrintWriter createTracer() throws MojoExecutionException {
        try {
            java.nio.file.Files.createDirectories(outputDirectory.toPath());
            return newPrintWriter(new FileOutputStream(outputDirectory.toPath().resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new MojoExecutionException("Could not initialize trace file", e);
        }
    }
}
