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
package org.jreleaser.app;

import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.tools.DistributionProcessor;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import picocli.CommandLine;

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

    @Override
    protected void consumeModel(JReleaserModel jreleaserModel) {
        JReleaserContext context = createContext(jreleaserModel);

        List<Exception> exceptions = new ArrayList<>();
        for (Distribution distribution : jreleaserModel.getDistributions().values()) {
            for (String toolName : Distribution.supportedTools()) {
                try {
                    DistributionProcessor processor = createDistributionProcessor(context,
                        distribution,
                        toolName);

                    consumeProcessor(processor);
                } catch (JReleaserException | ToolProcessingException e) {
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

    private DistributionProcessor createDistributionProcessor(JReleaserContext context,
                                                              Distribution distribution,
                                                              String toolName) {
        return DistributionProcessor.builder()
            .context(context)
            .distributionName(distribution.getName())
            .toolName(toolName)
            .build();
    }
}
