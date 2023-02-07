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
    }

    static class Include {
        @CommandLine.Option(names = {"--distribution"},
            paramLabel = "<distribution>")
        String[] includedDistributions;

        @CommandLine.Option(names = {"--cataloger"},
            paramLabel = "<cataloger>")
        String[] includedCatalogers;
    }

    static class Exclude {
        @CommandLine.Option(names = {"--exclude-distribution"},
            paramLabel = "<distribution>")
        String[] excludedDistributions;

        @CommandLine.Option(names = {"--exclude-cataloger"},
            paramLabel = "<cataloger>")
        String[] excludedCatalogers;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        if (null != composite) {
            context.setIncludedDistributions(collectEntries(composite.includedDistributions()));
            context.setExcludedDistributions(collectEntries(composite.excludedDistributions()));
            context.setIncludedCatalogers(collectEntries(composite.includedCatalogers(), true));
            context.setExcludedCatalogers(collectEntries(composite.excludedCatalogers(), true));
        }
        Workflows.catalog(context).execute();
    }
}
