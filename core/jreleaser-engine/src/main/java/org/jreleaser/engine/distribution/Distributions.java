/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.packager.spi.PackagerProcessingException;
import org.jreleaser.util.JReleaserException;

import java.util.List;
import java.util.Locale;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Distributions {
    public static void process(JReleaserContext context, String action, PackagerProcessingFunction function) {
        List<Distribution> activeDistributions = context.getModel().getActiveDistributions();

        if (activeDistributions.isEmpty()) {
            context.getLogger().debug(RB.$("distributions.not.enabled"), action.toLowerCase(Locale.ENGLISH));
            return;
        }

        if (!context.getIncludedDistributions().isEmpty()) {
            for (String distributionName : context.getIncludedDistributions()) {
                Distribution distribution = activeDistributions.stream()
                    .filter(d -> distributionName.equals(d.getName()))
                    .findFirst().orElse(null);

                if (null == distribution) {
                    context.getLogger().error(RB.$("distributions.no.match"), distributionName);
                    return;
                }

                if (!context.getIncludedPackagers().isEmpty()) {
                    for (String packagerName : context.getIncludedPackagers()) {
                        if (!Distribution.supportedPackagers().contains(packagerName)) {
                            context.getLogger().warn(RB.$("ERROR_unsupported_packager", packagerName));
                            continue;
                        }

                        context.getLogger().info(RB.$("distributions.apply.action"), action);

                        processDistribution(context, action, distribution, packagerName, function);
                    }
                } else {
                    processDistribution(context, action, distribution, function);
                }
            }
        } else if (!context.getIncludedPackagers().isEmpty()) {
            for (String packagerName : context.getIncludedPackagers()) {
                if (!Distribution.supportedPackagers().contains(packagerName)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_packager", packagerName));
                    continue;
                }

                context.getLogger().info(RB.$("distributions.apply.action"), action);
                for (Distribution distribution : activeDistributions) {
                    processDistribution(context, action, distribution, packagerName, function);
                }
            }
        } else {
            // process all
            context.getLogger().info(RB.$("distributions.apply.action"), action);
            for (Distribution distribution : activeDistributions) {
                if (context.getExcludedDistributions().contains(distribution.getName())) {
                    context.getLogger().info(RB.$("distributions.distribution.excluded"), distribution.getName());
                    continue;
                }

                processDistribution(context, action, distribution, function);
            }
        }
    }

    private static void processDistribution(JReleaserContext context, String action, Distribution distribution, PackagerProcessingFunction function) {
        context.getLogger().increaseIndent();
        context.getLogger().info(RB.$("distributions.apply.action.to"), action, distribution.getName());

        for (String packagerName : Distribution.supportedPackagers()) {
            if (context.getExcludedPackagers().contains(packagerName)) {
                context.getLogger().info(RB.$("packagers.packager.excluded"), packagerName);
                continue;
            }
            processPackager(context, distribution, packagerName, function);
        }

        context.getLogger().decreaseIndent();
    }

    private static void processDistribution(JReleaserContext context, String action, Distribution distribution, String packagerName, PackagerProcessingFunction function) {
        context.getLogger().increaseIndent();
        context.getLogger().info(RB.$("distributions.apply.action.to"), action, distribution.getName());

        processPackager(context, distribution, packagerName, function);

        context.getLogger().decreaseIndent();
    }

    private static void processPackager(JReleaserContext context, Distribution distribution, String packagerName, PackagerProcessingFunction function) {
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix(packagerName);
        try {
            DistributionProcessor processor = createDistributionProcessor(context,
                distribution,
                packagerName);

            function.consume(processor);
        } catch (PackagerProcessingException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        }
        context.getLogger().restorePrefix();
        context.getLogger().decreaseIndent();
    }

    private static DistributionProcessor createDistributionProcessor(JReleaserContext context,
                                                                     Distribution distribution,
                                                                     String packagerName) {
        return DistributionProcessor.builder()
            .context(context)
            .distributionName(distribution.getName())
            .packagerName(packagerName)
            .build();
    }
}
