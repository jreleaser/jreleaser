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
package org.jreleaser.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jreleaser.maven.plugin.internal.JReleaserLoggerAdapter;
import org.jreleaser.model.Distribution;
import org.jreleaser.templates.TemplateGenerationException;
import org.jreleaser.templates.TemplateGenerator;
import org.jreleaser.util.JReleaserLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generate a packager/announcer template.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@Mojo(name = "template")
public class JReleaserTemplateMojo extends AbstractMojo {
    /**
     * The project whose model will be checked.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.template.skip")
    private boolean skip;

    /**
     * The name of the distribution.
     */
    @Parameter(property = "jreleaser.template.distribution.name")
    private String distributionName;

    /**
     * The type of the distribution.
     */
    @Parameter(property = "jreleaser.template.distribution.type", defaultValue = "JAVA_BINARY")
    private final Distribution.DistributionType distributionType = Distribution.DistributionType.JAVA_BINARY;

    /**
     * The name of the packager.
     */
    @Parameter(property = "jreleaser.template.packager.name")
    private String packagerName;

    /**
     * The name of the announcer.
     */
    @Parameter(property = "jreleaser.announcer.name")
    private String announcerName;

    /**
     * Overwrite existing files.
     */
    @Parameter(property = "jreleaser.template.overwrite")
    private boolean overwrite;

    /**
     * Use snapshot templates.
     */
    @Parameter(property = "jreleaser.template.snapshot")
    private boolean snapshot;

    @Parameter(property = "jreleaser.output.directory", defaultValue = "${project.build.directory}/jreleaser")
    protected File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) {
            getLog().info("Execution has been explicitly skipped.");
            return;
        }

        try {
            Path outputDirectory = Paths.get(project.getBasedir().getAbsolutePath())
                .resolve("src")
                .resolve("jreleaser");

            Path output = TemplateGenerator.builder()
                .logger(getLogger())
                .distributionName(distributionName)
                .distributionType(distributionType)
                .packagerName(packagerName)
                .announcerName(announcerName)
                .outputDirectory(outputDirectory)
                .overwrite(overwrite)
                .snapshot(snapshot)
                .build()
                .generate();

            if (null != output) {
                getLogger().info("Template generated at {}", output.toAbsolutePath());
            }
        } catch (TemplateGenerationException e) {
            throw new MojoExecutionException("Unexpected error", e);
        }
    }

    protected JReleaserLogger getLogger() throws MojoExecutionException {
        return new JReleaserLoggerAdapter(createTracer(), getLog());
    }

    protected PrintWriter createTracer() throws MojoExecutionException {
        try {
            java.nio.file.Files.createDirectories(outputDirectory.toPath());
            return new PrintWriter(new FileOutputStream(outputDirectory.toPath().resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new MojoExecutionException("Could not initialize trace file", e);
        }
    }
}
