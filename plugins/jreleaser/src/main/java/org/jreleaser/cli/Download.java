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
 * @since 1.1.0
 */
@CommandLine.Command(name = "download")
public class Download extends AbstractModelCommand<Main> {
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

        String[] includedDownloaderTypes() {
            return null != include ? include.includedDownloaderTypes : null;
        }

        String[] includedDownloaderNames() {
            return null != include ? include.includedDownloaderNames : null;
        }

        String[] excludedDownloaderTypes() {
            return null != exclude ? exclude.excludedDownloaderTypes : null;
        }

        String[] excludedDownloaderNames() {
            return null != exclude ? exclude.excludedDownloaderNames : null;
        }
    }

    static class Include {
        @CommandLine.Option(names = {"-d", "--downloader"},
            paramLabel = "<downloader>")
        String[] includedDownloaderTypes;

        @CommandLine.Option(names = {"-dn", "--downloader-name"},
            paramLabel = "<name>")
        String[] includedDownloaderNames;
    }

    static class Exclude {
        @CommandLine.Option(names = {"-xd", "--exclude-downloader"},
            paramLabel = "<downloader>")
        String[] excludedDownloaderTypes;

        @CommandLine.Option(names = {"-xdn", "--exclude-downloader-name"},
            paramLabel = "<name>")
        String[] excludedDownloaderNames;
    }

    @Override
    protected void collectCandidateDeprecatedArgs(Set<AbstractCommand.DeprecatedArg> args) {
        super.collectCandidateDeprecatedArgs(args);
        args.add(new DeprecatedArg("-d", "--downloader", "1.5.0"));
        args.add(new DeprecatedArg("-dn", "--downloader-name", "1.5.0"));
        args.add(new DeprecatedArg("-xd", "--exclude-downloader", "1.5.0"));
        args.add(new DeprecatedArg("-xdn", "--exclude-downloader-name", "1.5.0"));
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        if (null != composite) {
            context.setIncludedDownloaderTypes(collectEntries(composite.includedDownloaderTypes(), true));
            context.setIncludedDownloaderNames(collectEntries(composite.includedDownloaderNames()));
            context.setExcludedDownloaderTypes(collectEntries(composite.excludedDownloaderTypes(), true));
            context.setExcludedDownloaderNames(collectEntries(composite.excludedDownloaderNames()));
        }
        Workflows.download(context).execute();
    }

    @Override
    protected Boolean dryrun() {
        return dryrun;
    }

    @Override
    protected Mode getMode() {
        return Mode.DOWNLOAD;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.DOWNLOAD;
    }
}
