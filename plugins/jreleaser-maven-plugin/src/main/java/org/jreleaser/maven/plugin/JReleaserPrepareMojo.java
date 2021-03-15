/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.tools.DistributionProcessor;
import org.jreleaser.tools.ToolProcessingException;

import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.maven.plugin.JReleaserChecksumMojo.checksum;

@Mojo(name = "prepare")
public class JReleaserPrepareMojo extends AbstractJReleaserMojo {
    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.prepare.skip")
    private boolean skip;

    /**
     * Stops on the first error.
     */
    @Parameter(property = "jreleaser.failfast", defaultValue = "true")
    private boolean failFast;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) return;

        JReleaserContext context = createContext();
        checksum(context);
        prepare(context, failFast);
    }

    static void prepare(JReleaserContext context, boolean failFast) throws MojoExecutionException {
        List<Exception> exceptions = new ArrayList<>();
        for (Distribution distribution : context.getModel().getDistributions().values()) {
            for (String toolName : Distribution.supportedTools()) {
                try {
                    DistributionProcessor processor = createDistributionProcessor(context,
                        distribution,
                        toolName);

                    if (!processor.prepareDistribution()) {
                        continue;
                    }

                    context.getLogger().info("Prepared " + distribution.getName() + " distribution with " + toolName);
                } catch (JReleaserException | ToolProcessingException e) {
                    if (failFast) throw new MojoExecutionException("Unexpected error", e);
                    exceptions.add(e);
                    context.getLogger().warn("Unexpected error", e);
                }
            }
        }

        if (!exceptions.isEmpty()) {
            throw new MojoExecutionException("There were " + exceptions.size() + " failure(s)");
        }
    }

    private static DistributionProcessor createDistributionProcessor(JReleaserContext context,
                                                                     Distribution distribution,
                                                                     String toolName) {
        return DistributionProcessor.builder()
            .context(context)
            .distributionName(distribution.getName())
            .toolName(toolName)
            .build();
    }
}
