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
package org.jreleaser.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.model.internal.JReleaserContext;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
abstract class AbstractPackagerMojo extends AbstractPlatformAwareMojo {
    /**
     * Include a packager.
     */
    @Parameter(property = "jreleaser.packagers")
    private String[] includedPackagers;

    /**
     * Exclude a packager.
     */
    @Parameter(property = "jreleaser.excluded.packagers")
    private String[] excludedPackagers;

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (isSkip()) {
            getLog().info("Execution has been explicitly skipped.");
            return;
        }

        JReleaserContext context = createContext();
        context.setIncludedPackagers(collectEntries(includedPackagers, true));
        context.setIncludedDistributions(collectEntries(includedDistributions));
        context.setExcludedPackagers(collectEntries(excludedPackagers, true));
        context.setExcludedDistributions(collectEntries(excludedDistributions));
        doExecute(context);
    }

    protected abstract void doExecute(JReleaserContext context);

    protected abstract boolean isSkip();
}
