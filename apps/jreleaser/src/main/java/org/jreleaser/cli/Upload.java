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
 * @since 0.3.0
 */
@CommandLine.Command(name = "upload")
public class Upload extends AbstractPlatformAwareModelCommand {
    @CommandLine.Option(names = {"-y", "--dryrun"})
    boolean dryrun;

    @CommandLine.ArgGroup
    Composite composite;

    static class Composite {
        @CommandLine.ArgGroup(exclusive = false, order = 1,
            headingKey = "include.filter.header")
        Include include;

        @CommandLine.ArgGroup(exclusive = false, order = 2,
            headingKey = "exclude.filter.header")
        Exclude exclude;

        String[] includedUploaderTypes() {
            return include != null ? include.includedUploaderTypes : null;
        }

        String[] includedUploaderNames() {
            return include != null ? include.includedUploaderNames : null;
        }

        String[] excludedUploaderTypes() {
            return exclude != null ? exclude.excludedUploaderTypes : null;
        }

        String[] excludedUploaderNames() {
            return exclude != null ? exclude.excludedUploaderNames : null;
        }
    }

    static class Include {
        @CommandLine.Option(names = {"-ut", "--uploader-type"},
            paramLabel = "<uploader>")
        String[] includedUploaderTypes;

        @CommandLine.Option(names = {"-un", "--uploader-name"},
            paramLabel = "<name>")
        String[] includedUploaderNames;
    }

    static class Exclude {
        @CommandLine.Option(names = {"-xut", "--exclude-uploader-type"},
            paramLabel = "<uploader>")
        String[] excludedUploaderTypes;

        @CommandLine.Option(names = {"-xun", "--exclude-uploader-name"},
            paramLabel = "<name>")
        String[] excludedUploaderNames;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        if (null != composite) {
            context.setIncludedUploaderTypes(collectEntries(composite.includedUploaderTypes(), true));
            context.setIncludedUploaderNames(collectEntries(composite.includedUploaderNames()));
            context.setExcludedUploaderTypes(collectEntries(composite.excludedUploaderTypes(), true));
            context.setExcludedUploaderNames(collectEntries(composite.excludedUploaderNames()));
        }
        Workflows.upload(context).execute();
    }

    @Override
    protected boolean dryrun() {
        return dryrun;
    }
}
