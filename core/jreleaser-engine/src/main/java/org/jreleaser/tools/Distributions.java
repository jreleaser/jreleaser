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
package org.jreleaser.tools;

import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.tool.spi.ToolProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Distributions {
    public static void process(JReleaserContext context, boolean failFast, String action, ToolProcessingFunction function) {
        if (context.getModel().getDistributions().isEmpty()) {
            context.getLogger().debug("No configured distributions [" + action.toLowerCase() + "]. Skipping");
            return;
        }

        context.getLogger().info("{} distributions", action);
        List<Exception> exceptions = new ArrayList<>();
        for (Distribution distribution : context.getModel().getDistributions().values()) {
            context.getLogger().increaseIndent();
            context.getLogger().info("- {} {} distribution", action, distribution.getName());
            for (String toolName : Distribution.supportedTools()) {
                context.getLogger().increaseIndent();
                context.getLogger().setPrefix(toolName);
                try {
                    DistributionProcessor processor = createDistributionProcessor(context,
                        distribution,
                        toolName);

                    function.consume(processor);
                } catch (ToolProcessingException e) {
                    if (failFast) throw new JReleaserException("Unexpected error", e);
                    exceptions.add(e);
                    context.getLogger().warn("Unexpected error", e);
                } catch (JReleaserException e) {
                    if (failFast) throw e;
                    exceptions.add(e);
                    context.getLogger().warn("Unexpected error", e);
                }
                context.getLogger().restorePrefix();
                context.getLogger().decreaseIndent();
            }
            context.getLogger().decreaseIndent();
        }

        if (!exceptions.isEmpty()) {
            throw new JReleaserException("There were " + exceptions.size() + " failure(s)" +
                System.lineSeparator() +
                exceptions.stream()
                    .map(Exception::getMessage)
                    .collect(Collectors.joining(System.lineSeparator())));
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
