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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;
import picocli.CommandLine;

import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
@CommandLine.Command(name = "catalog")
public class Catalog extends AbstractPlatformAwareModelCommand<Main> {
    @CommandLine.ArgGroup
    Composite composite;

    static class Composite {
        @CommandLine.ArgGroup(exclusive = false, order = 1,
            headingKey = "include.filter.header")
        Include include;

        @CommandLine.ArgGroup(exclusive = false, order = 2,
            headingKey = "exclude.filter.header")
        Exclude exclude;

        String[] includedDistributions() {
            return null != include ? include.includedDistributions : null;
        }

        String[] excludedDistributions() {
            return null != exclude ? exclude.excludedDistributions : null;
        }

        String[] includedCatalogers() {
            return null != include ? include.includedCatalogers : null;
        }

        String[] excludedCatalogers() {
            return null != exclude ? exclude.excludedCatalogers : null;
        }

        String[] includedDeployerTypes() {
            return null != include ? include.includedDeployerTypes : null;
        }

        String[] includedDeployerNames() {
            return null != include ? include.includedDeployerNames : null;
        }

        String[] excludedDeployerTypes() {
            return null != exclude ? exclude.excludedDeployerTypes : null;
        }

        String[] excludedDeployerNames() {
            return null != exclude ? exclude.excludedDeployerNames : null;
        }
    }

    static class Include {
        @CommandLine.Option(names = {"--distribution"},
            paramLabel = "<distribution>")
        String[] includedDistributions;

        @CommandLine.Option(names = {"--cataloger"},
            paramLabel = "<cataloger>")
        String[] includedCatalogers;

        @CommandLine.Option(names = {"-y", "--deployer"},
            paramLabel = "<deployer>")
        String[] includedDeployerTypes;

        @CommandLine.Option(names = {"-yn", "--deployer-name"},
            paramLabel = "<name>")
        String[] includedDeployerNames;
    }

    static class Exclude {
        @CommandLine.Option(names = {"--exclude-distribution"},
            paramLabel = "<distribution>")
        String[] excludedDistributions;

        @CommandLine.Option(names = {"--exclude-cataloger"},
            paramLabel = "<cataloger>")
        String[] excludedCatalogers;

        @CommandLine.Option(names = {"-xy", "--exclude-deployer"},
            paramLabel = "<deployer>")
        String[] excludedDeployerTypes;

        @CommandLine.Option(names = {"-xyn", "--exclude-deployer-name"},
            paramLabel = "<name>")
        String[] excludedDeployerNames;
    }

    @Override
    protected void collectCandidateDeprecatedArgs(Set<DeprecatedArg> args) {
        super.collectCandidateDeprecatedArgs(args);
        args.add(new DeprecatedArg("-y", "--deployer", "1.5.0"));
        args.add(new DeprecatedArg("-yn", "--deployer-name", "1.5.0"));
        args.add(new DeprecatedArg("-xy", "--exclude-deployer", "1.5.0"));
        args.add(new DeprecatedArg("-xyn", "--exclude-deployer-name", "1.5.0"));
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        if (null != composite) {
            context.setIncludedDistributions(collectEntries(composite.includedDistributions()));
            context.setExcludedDistributions(collectEntries(composite.excludedDistributions()));
            context.setIncludedCatalogers(collectEntries(composite.includedCatalogers(), true));
            context.setExcludedCatalogers(collectEntries(composite.excludedCatalogers(), true));
            context.setIncludedDeployerTypes(collectEntries(composite.includedDeployerTypes(), true));
            context.setIncludedDeployerNames(collectEntries(composite.includedDeployerNames()));
            context.setExcludedDeployerTypes(collectEntries(composite.excludedDeployerTypes(), true));
            context.setExcludedDeployerNames(collectEntries(composite.excludedDeployerNames()));
        }
        Workflows.catalog(context).execute();
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.CATALOG;
    }
}
