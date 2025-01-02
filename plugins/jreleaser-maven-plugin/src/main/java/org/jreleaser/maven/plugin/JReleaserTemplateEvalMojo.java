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
import org.jreleaser.engine.templates.TemplateEvaluator;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;

import java.io.File;

/**
 * Evaluate a template or templates.
 *
 * @author Andres Almiray
 * @since 1.5.0
 */
@Mojo(threadSafe = true, name = "template-eval")
public class JReleaserTemplateEvalMojo extends AbstractPlatformAwareMojo {
    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.template.skip")
    private boolean skip;
    /**
     * Eval model in full configuration.
     */
    @Parameter(property = "jreleaser.config.full")
    private boolean full;
    /**
     * Eval model in announce configuration.
     */
    @Parameter(property = "jreleaser.config.announce")
    private boolean announce;
    /**
     * Eval model in assembly configuration.
     */
    @Parameter(property = "jreleaser.config.assembly")
    private boolean assembly;
    /**
     * Eval model in changelog configuration.
     */
    @Parameter(property = "jreleaser.config.changelog")
    private boolean changelog;
    /**
     * Eval model in download configuration.
     */
    @Parameter(property = "jreleaser.config.download")
    private boolean download;
    /**
     * Overwrite existing files.
     */
    @Parameter(property = "jreleaser.template.overwrite")
    private boolean overwrite;
    /**
     * An input template file.
     */
    @Parameter(property = "jreleaser.template.input.file")
    protected File templateFile;
    /**
     * A directory with input templates.
     */
    @Parameter(property = "jreleaser.template.input.directory")
    protected File inputDirectory;
    /**
     * Directory where evaluated template(s) will be placed.
     */
    @Parameter(property = "jreleaser.template.target.directory", required = true)
    protected File targetDirectory;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        JReleaserContext context = createContext();
        ModelValidator.validate(context);

        if (null != templateFile) {
            TemplateEvaluator.generateTemplate(context, templateFile.toPath(),
                context.relativizeToBasedir(targetDirectory.toPath()), overwrite);
        } else if (null != inputDirectory)  {
            TemplateEvaluator.generateTemplates(context, inputDirectory.toPath(),
                context.relativizeToBasedir(targetDirectory.toPath()), overwrite);
        }

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
