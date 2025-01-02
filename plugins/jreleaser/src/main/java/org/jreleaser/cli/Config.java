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

import org.jreleaser.cli.internal.CliJReleaserModelPrinter;
import org.jreleaser.engine.context.ModelValidator;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import picocli.CommandLine;

import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "config")
public class Config extends AbstractPlatformAwareModelCommand<Main> {
    @CommandLine.Option(names = {"-f", "--full"})
    boolean full;

    @CommandLine.ArgGroup
    Exclusive exclusive;

    static class Exclusive {
        @CommandLine.Option(names = {"--announce"}, required = true)
        boolean announce;

        @CommandLine.Option(names = {"-a", "--assembly"}, required = true)
        boolean assembly;

        @CommandLine.Option(names = {"--changelog"}, required = true)
        boolean changelog;

        @CommandLine.Option(names = {"-d", "--download"}, required = true)
        boolean download;
    }

    @Override
    protected void collectCandidateDeprecatedArgs(Set<AbstractCommand.DeprecatedArg> args) {
        super.collectCandidateDeprecatedArgs(args);
        args.add(new DeprecatedArg("-a", "--assembly", "1.5.0"));
        args.add(new DeprecatedArg("-d", "--download", "1.5.0"));
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        ModelValidator.validate(context);
        new CliJReleaserModelPrinter(parent().getOut()).print(context.getModel().asMap(full));
        context.report();
    }

    @Override
    protected Mode getMode() {
        if (download()) return Mode.DOWNLOAD;
        if (assembly()) return Mode.ASSEMBLE;
        if (changelog()) return Mode.CHANGELOG;
        if (announce()) return Mode.ANNOUNCE;
        return Mode.CONFIG;
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
