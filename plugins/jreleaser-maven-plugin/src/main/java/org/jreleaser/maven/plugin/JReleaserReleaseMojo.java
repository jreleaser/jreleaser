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
 * Create or update a release.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@Mojo(threadSafe = true, name = "release")
public class JReleaserReleaseMojo extends AbstractPlatformAwareMojo {
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
     * Include a deployer by type.
     */
    @Parameter(property = "jreleaser.deployers")
    private String[] includedDeployers;

    /**
     * Exclude a deployer by type.
     */
    @Parameter(property = "jreleaser.excluded.deployers")
    private String[] excludedDeployers;

    /**
     * Include a deployer by name.
     */
    @Parameter(property = "jreleaser.deployer.names")
    private String[] includedDeployerNames;

    /**
     * Exclude a deployer by name.
     */
    @Parameter(property = "jreleaser.excluded.deployer.names")
    private String[] excludedDeployerNames;

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
    @Parameter(property = "jreleaser.release.skip")
    private boolean skip;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        JReleaserContext context = createContext();
        context.setIncludedDeployerTypes(collectEntries(includedDeployers, true));
        context.setIncludedDeployerNames(collectEntries(includedDeployerNames));
        context.setIncludedUploaderTypes(collectEntries(includedUploaders, true));
        context.setIncludedUploaderNames(collectEntries(includedUploaderNames));
        context.setIncludedDistributions(collectEntries(includedDistributions));
        context.setIncludedCatalogers(collectEntries(includedCatalogers, true));
        context.setExcludedDeployerTypes(collectEntries(excludedDeployers, true));
        context.setExcludedDeployerNames(collectEntries(excludedDeployerNames));
        context.setExcludedUploaderTypes(collectEntries(excludedUploaders, true));
        context.setExcludedUploaderNames(collectEntries(excludedUploaderNames));
        context.setExcludedDistributions(collectEntries(excludedDistributions));
        context.setExcludedCatalogers(collectEntries(excludedCatalogers, true));
        Workflows.release(context).execute();
    }

    @Override
    protected boolean isSkip() {
        return skip;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.RELEASE;
    }
}
