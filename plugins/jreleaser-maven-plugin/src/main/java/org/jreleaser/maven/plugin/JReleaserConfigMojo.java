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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.engine.context.ModelValidator;
import org.jreleaser.maven.plugin.internal.MavenJReleaserModelPrinter;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;

import static org.jreleaser.util.IoUtils.newPrintWriter;

/**
 * Display current configuration.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@Mojo(threadSafe = true, name = "config")
public class JReleaserConfigMojo extends AbstractPlatformAwareMojo {
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
     * Display announce configuration.
     */
    @Parameter(property = "jreleaser.config.announce")
    private boolean announce;
    /**
     * Display assembly configuration.
     */
    @Parameter(property = "jreleaser.config.assembly")
    private boolean assembly;
    /**
     * Display changelog configuration.
     */
    @Parameter(property = "jreleaser.config.changelog")
    private boolean changelog;
    /**
     * Display download configuration.
     */
    @Parameter(property = "jreleaser.config.download")
    private boolean download;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        JReleaserContext context = createContext();
        ModelValidator.validate(context);
        new MavenJReleaserModelPrinter(newPrintWriter(System.out))
            .print(context.getModel().asMap(full));
        context.report();
    }

    @Override
    protected Mode getMode() {
        if (download) return Mode.DOWNLOAD;
        if (assembly) return Mode.ASSEMBLE;
        if (changelog) return Mode.CHANGELOG;
        if (announce) return Mode.ANNOUNCE;
        return Mode.CONFIG;
    }

    @Override
    protected boolean isSkip() {
        return skip;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.CONFIG;
    }
}
