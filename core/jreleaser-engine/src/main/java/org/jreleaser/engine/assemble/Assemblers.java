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
package org.jreleaser.engine.assemble;

import org.jreleaser.model.Assemble;
import org.jreleaser.model.Assembler;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.assembler.spi.AssemblerProcessingException;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Assemblers {
    public static void assemble(JReleaserContext context) {
        Assemble assemble = context.getModel().getAssemble();
        if (!assemble.isEnabled()) {
            context.getLogger().info("Assembling is not enabled. Skipping.");
            return;
        }

        if (context.hasAssemblerName()) {
            Map<String, Assembler> assemblers = assemble.findAssemblersByType(context.getAssemblerName());

            if (assemblers.isEmpty()) {
                context.getLogger().debug("No assemblers match {}", context.getAssemblerName());
                return;
            }

            if (context.hasDistributionName()) {
                if (!assemblers.containsKey(context.getDistributionName())) {
                    context.getLogger().error("Distribution {} is not configured for assembling with {}",
                        context.getDistributionName(),
                        context.getAssemblerName());
                    return;
                }

                context.getLogger().info("Assembling {} distribution with {}",
                    context.getDistributionName(),
                    context.getAssemblerName());
                assemble(context, assemblers.get(context.getDistributionName()));
            } else {
                context.getLogger().info("Assembling all distributions with {}",
                    context.getAssemblerName());
                assemblers.values().forEach(assembler -> assemble(context, assembler));
            }
        } else if (context.hasDistributionName()) {
            context.getLogger().info("Assembling {} distribution with all assemblers",
                context.getDistributionName());
            assemble.findAllAssemblers().stream()
                .filter(a -> context.getDistributionName().equals(a.getName()))
                .forEach(assembler -> assemble(context, assembler));
        } else {
            context.getLogger().info("Assembling all distributions");
            assemble.findAllAssemblers().forEach(assembler -> assemble(context, assembler));
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
            throw new JReleaserException("Unexpected error", e);
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
