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
package org.jreleaser.cli;

import org.jreleaser.engine.context.ModelValidator;
import org.jreleaser.engine.templates.TemplateEvaluator;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext;
import picocli.CommandLine;

import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
@CommandLine.Command(name = "eval")
public class TemplateEval extends AbstractPlatformAwareModelCommand<Template> {
    @CommandLine.ArgGroup
    Config.Exclusive exclusive;

    static class Exclusive {
        @CommandLine.Option(names = {"--announce"}, required = true)
        boolean announce;

        @CommandLine.Option(names = {"--assembly"}, required = true)
        boolean assembly;

        @CommandLine.Option(names = {"--changelog"}, required = true)
        boolean changelog;

        @CommandLine.Option(names = { "--download"}, required = true)
        boolean download;
    }

    @CommandLine.ArgGroup(multiplicity = "1")
    Composite composite;

    static class Composite {
        @CommandLine.Option(names = {"--input-file"}, paramLabel = "<file>")
        Path inputFile;

        @CommandLine.Option(names = {"--input-directory"}, paramLabel = "<directory>")
        Path inputDirectory;
    }

    @CommandLine.Option(names = {"--target-directory"}, paramLabel = "<directory>", required = true)
    Path targetDirectory;

    @CommandLine.Option(names = {"-o", "--overwrite"})
    boolean overwrite;

    @Override
    protected void doExecute(org.jreleaser.model.internal.JReleaserContext context) {
        ModelValidator.validate(context);

        if (null != composite.inputFile) {
            TemplateEvaluator.generateTemplate(context, composite.inputFile,
                context.relativizeToBasedir(targetDirectory), overwrite);
        } else {
            TemplateEvaluator.generateTemplates(context, composite.inputDirectory,
                context.relativizeToBasedir(targetDirectory), overwrite);
        }

        context.report();
    }

    @Override
    protected JReleaserContext.Mode getMode() {
        if (download()) return JReleaserContext.Mode.DOWNLOAD;
        if (assembly()) return JReleaserContext.Mode.ASSEMBLE;
        if (changelog()) return JReleaserContext.Mode.CHANGELOG;
        if (announce()) return JReleaserContext.Mode.ANNOUNCE;
        return JReleaserContext.Mode.CONFIG;
    }

    private boolean download() {
        return null != exclusive && exclusive.download;
    }

    private boolean assembly() {
        return null != exclusive && exclusive.assembly;
    }

    private boolean changelog() {
        return null != exclusive && exclusive.changelog;
    }

    private boolean announce() {
        return null != exclusive && exclusive.announce;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.CONFIG;
    }
}
