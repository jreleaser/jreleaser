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
package org.jreleaser.engine.assemble;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.Assemble;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;

import java.util.Map;

import static org.jreleaser.model.internal.JReleaserSupport.supportedAssemblers;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Assemblers {
    public static void assemble(JReleaserContext context) {
        Assemble assemble = context.getModel().getAssemble();
        if (!assemble.isEnabled()) {
            context.getLogger().info(RB.$("assemblers.not.enabled"));
            return;
        }

        if (!context.getIncludedAssemblers().isEmpty()) {
            for (String assemblerType : context.getIncludedAssemblers()) {
                // check if the assemblerType is valid
                if (!supportedAssemblers().contains(assemblerType)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_assembler", assemblerType));
                    continue;
                }

                Map<String, Assembler> assemblers = assemble.findAssemblersByType(assemblerType);

                if (assemblers.isEmpty()) {
                    context.getLogger().debug(RB.$("assemblers.no.match"), assemblerType);
                    return;
                }

                if (!context.getIncludedDistributions().isEmpty()) {
                    for (String distributionName : context.getIncludedDistributions()) {
                        if (!assemblers.containsKey(distributionName)) {
                            context.getLogger().error(RB.$("assemblers.distribution.not.configured"), assemblerType, distributionName);
                            continue;
                        }

                        assemble.findAllAssemblers().stream()
                            .filter(a -> distributionName.equals(a.getName()))
                            .peek(assembler -> context.getLogger().info(RB.$("assemblers.assemble.distribution.with"),
                                distributionName, assembler.getName()))
                            .forEach(assembler -> assemble(context, assembler));
                    }
                } else {
                    context.getLogger().info(RB.$("assemblers.assemble.all.distributions.with"), assemblerType);
                    assemblers.values().forEach(assembler -> assemble(context, assembler));
                }
            }
        } else if (!context.getIncludedDistributions().isEmpty()) {
            for (String distributionName : context.getIncludedDistributions()) {
                context.getLogger().info(RB.$("assemblers.assemble.distribution.with.all"), distributionName);
                assemble.findAllAssemblers().stream()
                    .filter(a -> distributionName.equals(a.getName()))
                    .forEach(assembler -> assemble(context, assembler));
            }
        } else {
            context.getLogger().info(RB.$("assemblers.assemble.all.distributions"));
            for (Assembler assembler : assemble.findAllAssemblers()) {
                String assemblerType = assembler.getType();
                String distributionName = assembler.getName();
                if (context.getExcludedAssemblers().contains(assemblerType) ||
                    context.getExcludedDistributions().contains(distributionName)) {
                    context.getLogger().info(RB.$("assemblers.assembler.excluded"), assemblerType, distributionName);
                    continue;
                }

                assemble(context, assembler);
            }
        }
    }

    private static void assemble(JReleaserContext context, Assembler assembler) {
        try {
            context.getLogger().increaseIndent();
            context.getLogger().setPrefix(assembler.getType());
            DistributionAssembler processor = createDistributionAssembler(context, assembler);
            processor.assemble();
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        } catch (AssemblerProcessingException e) {
            throw new JReleaserException(e.getMessage(), e);
        }
    }

    private static DistributionAssembler createDistributionAssembler(JReleaserContext context,
                                                                     Assembler assembler) {
        return DistributionAssembler.builder()
            .context(context)
            .assembler(assembler)
            .build();
    }
}
