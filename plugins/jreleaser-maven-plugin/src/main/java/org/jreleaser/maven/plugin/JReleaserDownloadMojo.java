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
package org.jreleaser.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;

/**
 * Downloads assets.
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
@Mojo(threadSafe = true, name = "download")
public class JReleaserDownloadMojo extends AbstractJReleaserMojo {
    /**
     * Include a downloader by type.
     */
    @Parameter(property = "jreleaser.downloaders")
    private String[] includedDownloaders;

    /**
     * Exclude a downloader by type.
     */
    @Parameter(property = "jreleaser.excluded.downloaders")
    private String[] excludedDownloaders;

    /**
     * Include a downloader by name.
     */
    @Parameter(property = "jreleaser.downloader.names")
    private String[] includedDownloaderNames;

    /**
     * Exclude a downloader by name.
     */
    @Parameter(property = "jreleaser.excluded.downloader.names")
    private String[] excludedDownloaderNames;

    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.download.skip")
    private boolean skip;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        JReleaserContext context = createContext();
        context.setIncludedDownloaderTypes(collectEntries(includedDownloaders, true));
        context.setIncludedDownloaderNames(collectEntries(includedDownloaderNames));
        context.setExcludedDownloaderTypes(collectEntries(excludedDownloaders, true));
        context.setExcludedDownloaderNames(collectEntries(excludedDownloaderNames));
        Workflows.download(context).execute();
    }

    @Override
    protected Mode getMode() {
        return Mode.DOWNLOAD;
    }

    @Override
    protected boolean isSkip() {
        return skip;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.DOWNLOAD;
    }
}
