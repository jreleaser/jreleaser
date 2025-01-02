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
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

import java.io.File;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * Downloads, verifies, and unpacks JDKs.
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@Mojo(threadSafe = true, name = "setup-jdks")
public class SetupJdksMojo extends AbstractJdksMojo {
    @Parameter(property = "jdks.output.directory", defaultValue = "${project.build.directory}/jdks")
    private File outputDirectory;

    @Parameter(defaultValue = "${session}")
    private MavenSession session;

    @Component
    private BuildPluginManager pluginManager;

    /**
     * The name of the JDK to be downloaded.
     */
    @Parameter(property = "jdk.name")
    private String jdkName;

    /**
     * Skip execution.
     */
    @Parameter(property = "jdks.setup.skip")
    private boolean skip;

    @Parameter(property = "jdks.setup.unpack", defaultValue = "true")
    private boolean unpack;

    @Component
    private ArchiverManager archiverManager;

    @Override
    protected void doExecute() throws MojoExecutionException {
        JdkHelper jdkHelper = new JdkHelper(project, getLog(), outputDirectory,
            session, pluginManager, archiverManager);

        if (isNotBlank(jdkName)) {
            // find the given JDK
            Jdk jdk = jdks.stream()
                .filter(j -> j.getName().equals(jdkName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Jdk " + jdkName + " was not found"));
            jdkHelper.setupJdk(jdk, unpack);
        } else {
            for (Jdk jdk : jdks) {
                jdkHelper.setupJdk(jdk, unpack);
            }
        }
    }
}
