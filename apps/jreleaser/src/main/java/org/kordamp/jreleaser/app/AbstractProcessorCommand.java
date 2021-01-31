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
package org.kordamp.jreleaser.app;

import org.kordamp.jreleaser.model.Distribution;
import org.kordamp.jreleaser.model.JReleaserModel;
import org.kordamp.jreleaser.tools.DistributionProcessor;
import org.kordamp.jreleaser.tools.ToolProcessingException;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command
public abstract class AbstractProcessorCommand extends AbstractModelCommand {
    @CommandLine.Option(names = {"--fail-fast"},
        description = "Fail fast")
    boolean failFast = true;

    Path outputDirectory;

    @Override
    protected void consumeModel(JReleaserModel jreleaserModel) {
        outputDirectory = actualBasedir.resolve("out");

        Path checksumDirectory = computeChecksums(jreleaserModel, outputDirectory);

        List<ToolProcessingException> exceptions = new ArrayList<>();
        for (Distribution distribution : jreleaserModel.getDistributions().values()) {
            for (String toolName : Distribution.supportedTools()) {
                try {
                    DistributionProcessor processor = createDistributionProcessor(jreleaserModel,
                        checksumDirectory,
                        outputDirectory,
                        distribution,
                        toolName);

                    consumeProcessor(processor);
                } catch (ToolProcessingException e) {
                    if (failFast) throw new IllegalStateException("Unexpected error", e);
                    exceptions.add(e);
                    logger.warn("Unexpected error", e);
                }
            }
        }

        if (!exceptions.isEmpty()) {
            throw new IllegalStateException("There were " + exceptions.size() + " failure(s)");
        }
    }

    protected abstract void consumeProcessor(DistributionProcessor processor) throws ToolProcessingException;

    private DistributionProcessor createDistributionProcessor(JReleaserModel jreleaserModel,
                                                              Path checksumDirectory,
                                                              Path outputDirectory,
                                                              Distribution distribution,
                                                              String toolName) {
        return DistributionProcessor.builder()
            .logger(logger)
            .model(jreleaserModel)
            .distributionName(distribution.getName())
            .toolName(toolName)
            .checksumDirectory(checksumDirectory)
            .outputDirectory(outputDirectory
                .resolve("jreleaser")
                .resolve(distribution.getName())
                .resolve(toolName))
            .build();
    }

    protected Path computeChecksums(JReleaserModel jreleaserModel, Path outputDirectory) {
        return outputDirectory.resolve("jreleaser")
            .resolve("checksums");
    }
}
