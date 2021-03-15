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
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.tools.DistributionProcessor;
import org.jreleaser.tools.ToolProcessingException;
import org.jreleaser.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.maven.plugin.JReleaserChecksumsMojo.checksums;

@Mojo(name = "repare")
public class JReleaserPrepareMojo extends AbstractJReleaserMojo {
    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.prepare.skip")
    private boolean skip;

    @Parameter(property = "jreleaser.failfast", defaultValue = "true")
    private boolean failFast;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) return;

        JReleaserModel jreleaserModel = convertAndValidateModel();
        prepare(getLogger(), jreleaserModel, outputDirectory, failFast);
        checksums(getLogger(), jreleaserModel, outputDirectory);
    }

    static void prepare(Logger logger,
                        JReleaserModel jreleaserModel,
                        File outputDirectory,
                        boolean failFast) throws MojoExecutionException {
        List<Exception> exceptions = new ArrayList<>();
        for (Distribution distribution : jreleaserModel.getDistributions().values()) {
            for (String toolName : Distribution.supportedTools()) {
                try {
                    DistributionProcessor processor = createDistributionProcessor(logger,
                        jreleaserModel,
                        outputDirectory,
                        distribution,
                        toolName);

                    if (!processor.prepareDistribution()) {
                        continue;
                    }

                    logger.info("Prepared " + distribution.getName() + " distribution with tool " + toolName);
                } catch (JReleaserException | ToolProcessingException e) {
                    if (failFast) throw new MojoExecutionException("Unexpected error", e);
                    exceptions.add(e);
                    logger.warn("Unexpected error", e);
                }
            }
        }

        if (!exceptions.isEmpty()) {
            throw new MojoExecutionException("There were " + exceptions.size() + " failure(s)");
        }
    }

    private static DistributionProcessor createDistributionProcessor(Logger logger,
                                                                     JReleaserModel jreleaserModel,
                                                                     File outputDirectory,
                                                                     Distribution distribution,
                                                                     String toolName) {
        return DistributionProcessor.builder()
            .logger(logger)
            .model(jreleaserModel)
            .distributionName(distribution.getName())
            .toolName(toolName)
            .checksumDirectory(outputDirectory.toPath()
                .resolve("checksums"))
            .outputDirectory(outputDirectory.toPath()
                .resolve(distribution.getName())
                .resolve(toolName))
            .build();
    }
}
