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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.engine.context.ModelValidator;
import org.jreleaser.maven.plugin.internal.JReleaserModelPrinter;
import org.jreleaser.model.JReleaserContext;

import java.io.PrintWriter;

/**
 * Display current configuration.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@Mojo(name = "config")
public class JReleaserConfigMojo extends AbstractPlatformAwareJReleaserMojo {
    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.config.skip")
    private boolean skip;
    /**
     * Display full configuration.
     */
    @Parameter(property = "jreleaser.config.full")
    private boolean full;
    /**
     * Display assembly configuration.
     */
    @Parameter(property = "jreleaser.config.assembly")
    private boolean assembly;
    /**
     * Display download configuration.
     */
    @Parameter(property = "jreleaser.config.download")
    private boolean download;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) {
            getLog().info("Execution has been explicitly skipped.");
            return;
        }

        JReleaserContext context = createContext();
        ModelValidator.validate(context);
        new JReleaserModelPrinter(new PrintWriter(System.out, true))
            .print(context.getModel().asMap(full));
        context.report();
    }

    protected JReleaserContext.Mode getMode() {
        if (download) return JReleaserContext.Mode.DOWNLOAD;
        if (assembly) return JReleaserContext.Mode.ASSEMBLE;
        return JReleaserContext.Mode.CONFIG;
    }
}
