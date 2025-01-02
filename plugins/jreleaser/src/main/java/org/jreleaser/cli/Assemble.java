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

import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;
import picocli.CommandLine;

import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CommandLine.Command(name = "assemble")
public class Assemble extends AbstractPlatformAwareModelCommand<Main> {
    @CommandLine.ArgGroup
    Composite composite;

    static class Composite {
        @CommandLine.ArgGroup(exclusive = false, order = 1,
            headingKey = "include.filter.header")
        Include include;

        @CommandLine.ArgGroup(exclusive = false, order = 2,
            headingKey = "exclude.filter.header")
        Exclude exclude;

        String[] includedAssemblers() {
            return null != include ? include.includedAssemblers : null;
        }

        String[] includedDistributions() {
            return null != include ? include.includedDistributions : null;
        }

        String[] excludedAssemblers() {
            return null != exclude ? exclude.excludedAssemblers : null;
        }

        String[] excludedDistributions() {
            return null != exclude ? exclude.excludedDistributions : null;
        }
    }

    static class Include {
        @CommandLine.Option(names = {"-s", "--assembler"},
            paramLabel = "<assembler>")
        String[] includedAssemblers;

        @CommandLine.Option(names = {"-d", "--distribution"},
            paramLabel = "<distribution>")
        String[] includedDistributions;
    }

    static class Exclude {
        @CommandLine.Option(names = {"-xs", "--exclude-assembler"},
            paramLabel = "<assembler>")
        String[] excludedAssemblers;

        @CommandLine.Option(names = {"-xd", "--exclude-distribution"},
            paramLabel = "<distribution>")
        String[] excludedDistributions;
    }

    @Override
    protected void collectCandidateDeprecatedArgs(Set<AbstractCommand.DeprecatedArg> args) {
        super.collectCandidateDeprecatedArgs(args);
        args.add(new DeprecatedArg("-s", "--assembler", "1.5.0"));
        args.add(new DeprecatedArg("-xs", "--exclude-assembler", "1.5.0"));
        args.add(new DeprecatedArg("-d", "--distribution", "1.5.0"));
        args.add(new DeprecatedArg("-xd", "--exclude-distribution", "1.5.0"));
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        if (null != composite) {
            context.setIncludedAssemblers(collectEntries(composite.includedAssemblers(), true));
            context.setIncludedDistributions(collectEntries(composite.includedDistributions()));
            context.setExcludedAssemblers(collectEntries(composite.excludedAssemblers(), true));
            context.setExcludedDistributions(collectEntries(composite.excludedDistributions()));
        }
        Workflows.assemble(context).execute();
    }

    @Override
    protected Mode getMode() {
        return Mode.ASSEMBLE;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.ASSEMBLE;
    }
}
