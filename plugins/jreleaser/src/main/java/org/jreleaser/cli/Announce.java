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
 * @since 0.1.0
 */
@CommandLine.Command(name = "announce")
public class Announce extends AbstractModelCommand<Main> {
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

        String[] includedAnnouncers() {
            return null != include ? include.includedAnnouncers : null;
        }

        String[] excludedAnnouncers() {
            return null != exclude ? exclude.excludedAnnouncers : null;
        }
    }

    static class Include {
        @CommandLine.Option(names = {"-a", "--announcer"},
            paramLabel = "<announcer>")
        String[] includedAnnouncers;
    }

    static class Exclude {
        @CommandLine.Option(names = {"-xa", "--exclude-announcer"},
            paramLabel = "<announcer>")
        String[] excludedAnnouncers;
    }

    @Override
    protected void collectCandidateDeprecatedArgs(Set<AbstractCommand.DeprecatedArg> args) {
        super.collectCandidateDeprecatedArgs(args);
        args.add(new DeprecatedArg("-a", "--announcer", "1.5.0"));
        args.add(new DeprecatedArg("-xa", "--exclude-announcer", "1.5.0"));
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        if (null != composite) {
            context.setIncludedAnnouncers(collectEntries(composite.includedAnnouncers(), true));
            context.setExcludedAnnouncers(collectEntries(composite.excludedAnnouncers(), true));
        }
        Workflows.announce(context).execute();
    }

    @Override
    protected Boolean dryrun() {
        return dryrun;
    }

    @Override
    protected Mode getMode() {
        return Mode.ANNOUNCE;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.ANNOUNCE;
    }
}
