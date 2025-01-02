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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;

/**
 * Catalog artifacts and files.
 *
 * @author Andres Almiray
 * @since 1.5.0
 */
@Mojo(threadSafe = true, name = "catalog")
public class JReleaserCatalogMojo extends AbstractDistributionMojo {
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
     * Skip execution.
     */
    @Parameter(property = "jreleaser.catalog.skip")
    private boolean skip;

    @Override
    protected void doExecute(JReleaserContext context) {
        context.setIncludedCatalogers(collectEntries(includedCatalogers, true));
        context.setExcludedCatalogers(collectEntries(excludedCatalogers, true));
        context.setIncludedDeployerTypes(collectEntries(includedDeployers, true));
        context.setIncludedDeployerNames(collectEntries(includedDeployerNames));
        context.setExcludedDeployerTypes(collectEntries(excludedDeployers, true));
        context.setExcludedDeployerNames(collectEntries(excludedDeployerNames));
        Workflows.catalog(context).execute();
    }

    @Override
    protected boolean isSkip() {
        return skip;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.CATALOG;
    }
}
