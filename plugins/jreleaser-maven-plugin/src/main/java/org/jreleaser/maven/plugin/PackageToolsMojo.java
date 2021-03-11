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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.maven.plugin.internal.JReleaserModelConfigurer;
import org.jreleaser.maven.plugin.internal.JReleaserModelConverter;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserModelValidator;
import org.jreleaser.tools.Checksums;
import org.jreleaser.tools.DistributionProcessor;
import org.jreleaser.tools.ToolProcessingException;
import org.jreleaser.util.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "package-tools", defaultPhase = LifecyclePhase.PACKAGE)
public class PackageToolsMojo extends AbstractJReleaserMojo {
    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.tools.skip")
    private boolean skip;

    @Parameter(required = true)
    private Jreleaser jreleaser;

    @Parameter(property = "jreleaser.output.directory", defaultValue = "${project.build.directory}")
    private File outputDirectory;

    @Parameter(property = "jreleaser.checksum.directory", required = true)
    private File checksumDirectory;

    @Parameter(property = "jreleaser.tools.failfast", defaultValue = "true")
    private boolean failFast;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) return;

        Logger logger = getLogger();
        JReleaserModel jreleaserModel = JReleaserModelConverter.convert(jreleaser);
        JReleaserModelConfigurer.configure(jreleaserModel, project);
        List<String> errors = JReleaserModelValidator.validate(logger, project.getBasedir().toPath(), jreleaserModel);
        if (!errors.isEmpty()) {
            getLog().error("== JReleaser ==");
            errors.forEach(getLog()::error);
            throw new MojoExecutionException("JReleaser for project " + project.getArtifactId() + " has not been properly configured.");
        }

        List<Exception> exceptions = new ArrayList<>();
        for (Distribution distribution : jreleaserModel.getDistributions().values()) {
            for (String toolName : Distribution.supportedTools()) {
                try {
                    DistributionProcessor processor = createDistributionProcessor(jreleaserModel,
                        logger,
                        distribution,
                        toolName);

                    if (!processor.prepareDistribution()) {
                        continue;
                    }

                    getLog().info("Prepared " + distribution.getName() + " distribution with tool " + toolName);

                    if (!processor.packageDistribution()) {
                        continue;
                    }

                    getLog().info("Packaged " + distribution.getName() + " distribution with tool " + toolName);
                } catch (JReleaserException | ToolProcessingException e) {
                    if (failFast) throw new MojoExecutionException("Unexpected error", e);
                    exceptions.add(e);
                    getLog().warn(e);
                }
            }
        }

        if (exceptions.isEmpty()) {
            Path checksumsFilePath = checksumDirectory.toPath().resolve("checksums.txt");
            try {
                Checksums.collectAndWriteChecksums(logger, jreleaserModel, checksumDirectory.toPath());
            } catch (JReleaserException e) {
                if (failFast) {
                    throw new MojoExecutionException("Unexpected error writing checksums to " + checksumsFilePath.toAbsolutePath(), e);
                }
                exceptions.add(e);
                getLog().warn(e);
            }
        }

        if (!exceptions.isEmpty()) {
            throw new MojoExecutionException("There were " + exceptions.size() + " failure(s)");
        }
    }

    private DistributionProcessor createDistributionProcessor(JReleaserModel jreleaserModel,
                                                              Logger logger,
                                                              Distribution distribution,
                                                              String toolName) {
        return DistributionProcessor.builder()
            .logger(logger)
            .model(jreleaserModel)
            .distributionName(distribution.getName())
            .toolName(toolName)
            .checksumDirectory(checksumDirectory.toPath())
            .outputDirectory(outputDirectory.toPath()
                .resolve("jreleaser")
                .resolve(distribution.getName())
                .resolve(toolName))
            .build();
    }
}
