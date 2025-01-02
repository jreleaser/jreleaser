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
package org.jreleaser.jdks.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jreleaser.sdk.disco.Disco;
import org.jreleaser.util.Errors;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
abstract class AbstractDiscoMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(required = true)
    protected List<Pkg> pkgs;

    @Parameter(property = "disco.output.directory", defaultValue = "${project.build.directory}/jdks")
    protected File outputDirectory;

    @Parameter(defaultValue = "${session}")
    protected MavenSession session;

    @Component
    protected BuildPluginManager pluginManager;

    @Parameter(property = "disco.setup.connect.timeout")
    protected int connectTimeout;

    @Parameter(property = "disco.setup.read.timeout")
    protected int readTimeout;

    /**
     * Skip execution.
     */
    @Parameter(property = "disco.setup.skip")
    protected boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) return;

        if (null == pkgs || pkgs.isEmpty()) return;
        validate();

        doExecute(initializeDisco());
    }

    protected abstract void doExecute(Disco disco) throws MojoExecutionException, MojoFailureException;

    private Disco initializeDisco() throws MojoExecutionException {
        try {
            return new Disco(new JReleaserLoggerAdapter(getLog()), connectTimeout, readTimeout);
        } catch (RuntimeException e) {
            throw new MojoExecutionException("Could not initialize Disco client", e);
        }
    }

    private void validate() throws MojoFailureException {
        if (connectTimeout <= 0 || connectTimeout > 300) {
            connectTimeout = 20;
        }
        if (readTimeout <= 0 || readTimeout > 300) {
            readTimeout = 60;
        }

        Errors errors = new Errors();
        pkgs.forEach(pkg -> pkg.validate(errors));

        if (errors.hasErrors()) {
            StringWriter s = new StringWriter();
            PrintWriter w = new PrintWriter(s, true);
            errors.logErrors(w);
            throw new MojoFailureException(s.toString());
        }
    }
}
