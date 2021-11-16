/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.workflow.Workflows;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "announce")
public class Announce extends AbstractPlatformAwareModelCommand {
    @CommandLine.Option(names = {"-y", "--dryrun"})
    boolean dryrun;

    @CommandLine.ArgGroup(headingKey = "filter.header")
    Composite composite;

    static class Composite {
        @CommandLine.Option(names = {"-a", "--announcer"},
            paramLabel = "<announcer>",
            required = true)
        String[] includedAnnouncers;

        @CommandLine.Option(names = {"-xa", "--exclude-announcer"},
            paramLabel = "<announcer>",
            required = true)
        String[] excludedAnnouncers;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        if (null != composite) {
            context.setIncludedAnnouncers(collectEntries(composite.includedAnnouncers, true));
            context.setExcludedAnnouncers(collectEntries(composite.excludedAnnouncers, true));
        }
        Workflows.announce(context).execute();
    }

    @Override
    protected boolean dryrun() {
        return dryrun;
    }
}
