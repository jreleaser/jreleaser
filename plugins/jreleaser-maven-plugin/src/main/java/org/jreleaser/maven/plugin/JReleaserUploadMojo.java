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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;

/**
 * Uploads all artifacts.
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@Mojo(threadSafe = true, name = "upload")
public class JReleaserUploadMojo extends AbstractPlatformAwareMojo {
    /**
     * Include an uploader by type.
     */
    @Parameter(property = "jreleaser.uploaders")
    private String[] includedUploaders;

    /**
     * Exclude an uploader by type.
     */
    @Parameter(property = "jreleaser.excluded.uploaders")
    private String[] excludedUploaders;

    /**
     * Include an uploader by name.
     */
    @Parameter(property = "jreleaser.uploader.names")
    private String[] includedUploaderNames;

    /**
     * Exclude an uploader by name.
     */
    @Parameter(property = "jreleaser.excluded.uploader.names")
    private String[] excludedUploaderNames;

    /**
     * Include a distribution.
     */
    @Parameter(property = "jreleaser.distributions")
    private String[] includedDistributions;

    /**
     * Exclude a distribution.
     */
    @Parameter(property = "jreleaser.excluded.distributions")
    private String[] excludedDistributions;

    /**
     * Include an cataloger.
     */
    @Parameter(property = "jreleaser.catalogers")
    private String[] includedCatalogers;

    /**
     * Exclude an cataloger.
     */
    @Parameter(property = "jreleaser.excluded.catalogers")
    private String[] excludedCatalogers;

    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.upload.skip")
    private boolean skip;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        JReleaserContext context = createContext();
        context.setIncludedUploaderTypes(collectEntries(includedUploaders, true));
        context.setIncludedUploaderNames(collectEntries(includedUploaderNames));
        context.setIncludedDistributions(collectEntries(includedDistributions));
        context.setExcludedUploaderTypes(collectEntries(excludedUploaders, true));
        context.setExcludedUploaderNames(collectEntries(excludedUploaderNames));
        context.setExcludedDistributions(collectEntries(excludedDistributions));
        context.setIncludedCatalogers(collectEntries(includedCatalogers, true));
        context.setExcludedCatalogers(collectEntries(excludedCatalogers, true));
        Workflows.upload(context).execute();
    }

    @Override
    protected boolean isSkip() {
        return skip;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.UPLOAD;
    }
}
