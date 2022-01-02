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
package org.jreleaser.jdks.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.jreleaser.bundle.RB;
import org.jreleaser.sdk.disco.Disco;
import org.jreleaser.sdk.disco.RestAPIException;
import org.jreleaser.util.Errors;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Lists all configured JDKs with the Foojay's Disco API.
 *
 * @author Andres Almiray
 * @since 0.9.0
 */
@Mojo(name = "list-disco")
public class ListDiscoMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(required = true)
    protected List<Pkg> pkgs;

    @Parameter(property = "disco.output.directory", defaultValue = "${project.build.directory}/jdks")
    private File outputDirectory;

    @Parameter(defaultValue = "${session}")
    private MavenSession session;

    @Component
    private BuildPluginManager pluginManager;

    @Parameter(property = "disco.setup.connect.timeout")
    private int connectTimeout;

    @Parameter(property = "disco.setup.read.timeout")
    private int readTimeout;

    /**
     * Skip execution.
     */
    @Parameter(property = "disco.setup.skip")
    private boolean skip;

    @Component
    private ArchiverManager archiverManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) return;

        if (pkgs == null || pkgs.isEmpty()) return;
        validate();

        Disco disco = initializeDisco();

        for (Pkg pkg : pkgs) {
            printPkg(pkg, disco);
        }
    }

    private Disco initializeDisco() throws MojoExecutionException {
        try {
            return new Disco(new JReleaserLoggerAdapter(getLog()), connectTimeout, readTimeout);
        } catch (IOException e) {
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

    private void printPkg(Pkg pkg, Disco disco) throws MojoExecutionException {
        try {
            List<org.jreleaser.sdk.disco.api.Pkg> packages = disco.packages(pkg.asDiscoPkg());

            if (packages.isEmpty()) return;

            getLog().info("== Pkg " + pkg.getName() + " ==");
            getLog().info("version:       " + pkg.getVersion());
            getLog().info("archiveType:   " + pkg.getArchiveType());
            getLog().info("platform:      " + pkg.getPlatform());
            getLog().info("distribution:  " + pkg.getDistribution());
            getLog().info("javafxBundled: " + pkg.isJavafxBundled());
            getLog().info("package(s):    " + packages.size());

            for (org.jreleaser.sdk.disco.api.Pkg dpkg : packages) {
                if (!dpkg.isDirectlyDownloadable()) {
                    disco.getLogger().warn(RB.$("disco.package.not.downloadable", dpkg.getFilename()));
                    continue;
                }

                getLog().info("filename:      " + dpkg.getFilename());
            }

        } catch (RestAPIException e) {
            getLog().error(e);
            throw new MojoExecutionException("Could not resolve " + pkg, e);
        }
    }
}
