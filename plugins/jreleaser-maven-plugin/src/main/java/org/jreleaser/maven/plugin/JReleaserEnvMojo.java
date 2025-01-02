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
import org.jreleaser.engine.environment.Environment;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.maven.plugin.internal.JReleaserLoggerAdapter;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.jreleaser.util.IoUtils.newPrintWriter;

/**
 * Display environment variable names.
 *
 * @author Andres Almiray
 * @since 1.5.0
 */
@Mojo(threadSafe = true, name = "env")
public class JReleaserEnvMojo extends AbstractMojo {
    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.env.skip")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) {
            getLog().info("Execution has been explicitly skipped.");
            return;
        }

        Environment.display(getLogger(), project.getBasedir().toPath());
    }

    protected JReleaserLogger getLogger() {
        return new JReleaserLoggerAdapter(createTracer(), getLog());
    }

    protected PrintWriter createTracer() {
        return newPrintWriter(new ByteArrayOutputStream());
    }
}
