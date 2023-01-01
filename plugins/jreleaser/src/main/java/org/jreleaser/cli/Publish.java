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
@CommandLine.Command(name = "publish")
public class Publish extends AbstractPlatformAwareModelCommand {
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

        String[] includedPackagers() {
            return include != null ? include.includedPackagers : null;
        }

        String[] includedDistributions() {
            return include != null ? include.includedDistributions : null;
        }

        String[] excludedPackagers() {
            return exclude != null ? exclude.excludedPackagers : null;
        }

        String[] excludedDistributions() {
            return exclude != null ? exclude.excludedDistributions : null;
        }
    }

    static class Include {
        @CommandLine.Option(names = {"-d", "--distribution"},
            paramLabel = "<distribution>")
        String[] includedDistributions;

        @CommandLine.Option(names = {"-p", "--packager"},
            paramLabel = "<packager>")
        String[] includedPackagers;
    }

    static class Exclude {
        @CommandLine.Option(names = {"-xd", "--exclude-distribution"},
            paramLabel = "<distribution>")
        String[] excludedDistributions;

        @CommandLine.Option(names = {"-xp", "--exclude-packager"},
            paramLabel = "<packager>")
        String[] excludedPackagers;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        if (null != composite) {
            context.setIncludedPackagers(collectEntries(composite.includedPackagers(), true));
            context.setIncludedDistributions(collectEntries(composite.includedDistributions()));
            context.setExcludedPackagers(collectEntries(composite.excludedPackagers(), true));
            context.setExcludedDistributions(collectEntries(composite.excludedDistributions()));
        }
        Workflows.publish(context).execute();
    }

    @Override
    protected Boolean dryrun() {
        return dryrun;
    }
}
