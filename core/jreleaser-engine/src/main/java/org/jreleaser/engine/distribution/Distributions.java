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
package org.jreleaser.engine.distribution;

import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.tool.spi.ToolProcessingException;

import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Distributions {
    public static void process(JReleaserContext context, String action, ToolProcessingFunction function) {
        List<Distribution> activeDistributions = context.getModel().getActiveDistributions();

        if (activeDistributions.isEmpty()) {
            context.getLogger().debug("No active distributions [" + action.toLowerCase() + "]. Skipping");
            return;
        }

        if (context.hasDistributionName()) {
            Distribution distribution = context.getModel().findDistribution(context.getDistributionName());

            if (null == distribution) {
                context.getLogger().error("Distribution {} does not exist", context.getDistributionName());
                return;
            }

            if (context.hasToolName()) {
                processDistribution(context, action, distribution, context.getToolName(), function);
            } else {
                processDistribution(context, action, distribution, function);
            }
            return;
        } else if (context.hasToolName()) {
            context.getLogger().info("{} distributions", action);
            for (Distribution distribution : activeDistributions) {
                processDistribution(context, action, distribution, context.getToolName(), function);
            }
            return;
        }

        // process all
        context.getLogger().info("{} distributions", action);
        for (Distribution distribution : activeDistributions) {
            processDistribution(context, action, distribution, function);
        }
    }

    private static void processDistribution(JReleaserContext context, String action, Distribution distribution, ToolProcessingFunction function) {
        context.getLogger().increaseIndent();
        context.getLogger().info("- {} {} distribution", action, distribution.getName());

        for (String toolName : Distribution.supportedTools()) {
            processTool(context, distribution, toolName, function);
        }

        context.getLogger().decreaseIndent();
    }

    private static void processDistribution(JReleaserContext context, String action, Distribution distribution, String toolName, ToolProcessingFunction function) {
        context.getLogger().increaseIndent();
        context.getLogger().info("- {} {} distribution", action, distribution.getName());

        processTool(context, distribution, toolName, function);

        context.getLogger().decreaseIndent();
    }

    private static void processTool(JReleaserContext context, Distribution distribution, String toolName, ToolProcessingFunction function) {
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix(toolName);
        try {
            DistributionProcessor processor = createDistributionProcessor(context,
                distribution,
                toolName);

            function.consume(processor);
        } catch (ToolProcessingException e) {
            throw new JReleaserException("Unexpected error", e);
        }
        context.getLogger().restorePrefix();
        context.getLogger().decreaseIndent();
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
