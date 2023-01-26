/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "full-release")
public class FullRelease extends AbstractPlatformAwareModelCommand<Main> {
    @CommandLine.Option(names = {"--dry-run"})
    Boolean dryrun;

    @CommandLine.ArgGroup
    Composite composite;

    static class Composite {
        @CommandLine.ArgGroup(exclusive = false, order = 1,
            headingKey = "include.filter.header")
        Include include;

        @CommandLine.ArgGroup(exclusive = false, order = 2,
            headingKey = "exclude.filter.header")
        Exclude exclude;

        String[] includedDeployerTypes() {
            return null != include ? include.includedDeployerTypes : null;
        }

        String[] includedDeployerNames() {
            return null != include ? include.includedDeployerNames : null;
        }

        String[] includedUploaderTypes() {
            return null != include ? include.includedUploaderTypes : null;
        }

        String[] includedUploaderNames() {
            return null != include ? include.includedUploaderNames : null;
        }

        String[] includedDistributions() {
            return null != include ? include.includedDistributions : null;
        }

        String[] includedPackagers() {
            return null != include ? include.includedPackagers : null;
        }

        String[] includedAnnouncers() {
            return null != include ? include.includedAnnouncers : null;
        }

        String[] excludedDeployerTypes() {
            return null != exclude ? exclude.excludedDeployerTypes : null;
        }

        String[] excludedDeployerNames() {
            return null != exclude ? exclude.excludedDeployerNames : null;
        }

        String[] excludedUploaderTypes() {
            return null != exclude ? exclude.excludedUploaderTypes : null;
        }

        String[] excludedUploaderNames() {
            return null != exclude ? exclude.excludedUploaderNames : null;
        }

        String[] excludedDistributions() {
            return null != exclude ? exclude.excludedDistributions : null;
        }

        String[] excludedPackagers() {
            return null != exclude ? exclude.excludedPackagers : null;
        }

        String[] excludedAnnouncers() {
            return null != exclude ? exclude.excludedAnnouncers : null;
        }
    }

    static class Include {
        @CommandLine.Option(names = {"-y", "--deployer"},
            paramLabel = "<deployer>")
        String[] includedDeployerTypes;

        @CommandLine.Option(names = {"-yn", "--deployer-name"},
            paramLabel = "<name>")
        String[] includedDeployerNames;

        @CommandLine.Option(names = {"-u", "--uploader"},
            paramLabel = "<uploader>")
        String[] includedUploaderTypes;

        @CommandLine.Option(names = {"-un", "--uploader-name"},
            paramLabel = "<name>")
        String[] includedUploaderNames;

        @CommandLine.Option(names = {"-d", "--distribution"},
            paramLabel = "<distribution>")
        String[] includedDistributions;

        @CommandLine.Option(names = {"-p", "--packager"},
            paramLabel = "<packager>")
        String[] includedPackagers;

        @CommandLine.Option(names = {"-a", "--announcer"},
            paramLabel = "<announcer>")
        String[] includedAnnouncers;
    }

    static class Exclude {
        @CommandLine.Option(names = {"-xy", "--exclude-deployer"},
            paramLabel = "<deployer>")
        String[] excludedDeployerTypes;

        @CommandLine.Option(names = {"-xyn", "--exclude-deployer-name"},
            paramLabel = "<name>")
        String[] excludedDeployerNames;

        @CommandLine.Option(names = {"-xu", "--exclude-uploader"},
            paramLabel = "<uploader>")
        String[] excludedUploaderTypes;

        @CommandLine.Option(names = {"-xun", "--exclude-uploader-name"},
            paramLabel = "<name>")
        String[] excludedUploaderNames;

        @CommandLine.Option(names = {"-xd", "--exclude-distribution"},
            paramLabel = "<distribution>")
        String[] excludedDistributions;

        @CommandLine.Option(names = {"-xp", "--exclude-packager"},
            paramLabel = "<packager>")
        String[] excludedPackagers;

        @CommandLine.Option(names = {"-xa", "--exclude-announcer"},
            paramLabel = "<announcer>")
        String[] excludedAnnouncers;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        if (null != composite) {
            context.setIncludedDeployerTypes(collectEntries(composite.includedDeployerTypes(), true));
            context.setIncludedDeployerNames(collectEntries(composite.includedDeployerNames()));
            context.setIncludedUploaderTypes(collectEntries(composite.includedUploaderTypes(), true));
            context.setIncludedUploaderNames(collectEntries(composite.includedUploaderNames()));
            context.setIncludedDistributions(collectEntries(composite.includedDistributions()));
            context.setIncludedPackagers(collectEntries(composite.includedPackagers(), true));
            context.setIncludedAnnouncers(collectEntries(composite.includedAnnouncers(), true));
            context.setExcludedDeployerTypes(collectEntries(composite.excludedDeployerTypes(), true));
            context.setExcludedDeployerNames(collectEntries(composite.excludedDeployerNames()));
            context.setExcludedUploaderTypes(collectEntries(composite.excludedUploaderTypes(), true));
            context.setExcludedUploaderNames(collectEntries(composite.excludedUploaderNames()));
            context.setExcludedDistributions(collectEntries(composite.excludedDistributions()));
            context.setExcludedPackagers(collectEntries(composite.excludedPackagers(), true));
            context.setExcludedAnnouncers(collectEntries(composite.excludedAnnouncers(), true));
        }
        Workflows.fullRelease(context).execute();
    }

    @Override
    protected Boolean dryrun() {
        return dryrun;
    }
}
