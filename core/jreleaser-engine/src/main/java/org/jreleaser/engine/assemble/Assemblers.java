/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
import org.jreleaser.extensions.api.workflow.WorkflowListenerException;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
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
public final class Assemblers {
    private Assemblers() {
        // noop
    }

    public static void assemble(JReleaserContext context) {
        context.getLogger().info(RB.$("assemblers.header"));
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("assemble");

        Assemble assemble = context.getModel().getAssemble();
        if (!assemble.isEnabled()) {
            context.getLogger().info(RB.$("assemblers.not.enabled"));
            context.getLogger().restorePrefix();
            return;
        }

        try {
            doAssemble(context, assemble);
        } finally {
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
        }
    }

    private static void doAssemble(JReleaserContext context, Assemble assemble) {
        if (!context.getIncludedAssemblers().isEmpty()) {
            for (String assemblerType : context.getIncludedAssemblers()) {
                // check if the assemblerType is valid
                if (!supportedAssemblers().contains(assemblerType)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_assembler", assemblerType));
                    continue;
                }

                Map<String, Assembler<?>> assemblers = assemble.findAssemblersByType(assemblerType);

                if (assemblers.isEmpty()) {
                    context.getLogger().info(RB.$("assemblers.no.match"), assemblerType);
                    return;
                }

                if (!context.getIncludedDistributions().isEmpty()) {
                    for (String distributionName : context.getIncludedDistributions()) {
                        boolean[] assembled = new boolean[]{false};
                        if (!assemblers.containsKey(distributionName)) {
                            context.getLogger().error(RB.$("assemblers.distribution.not.configured"), assemblerType, distributionName);
                            continue;
                        }

                        assemble.findAllAssemblers().stream()
                            .filter(a -> distributionName.equals(a.getName()))
                            .peek(assembler -> context.getLogger().info(RB.$("assemblers.assemble.distribution.with"),
                                distributionName, assembler.getName()))
                            .forEach(assembler -> {
                                if (assemble(context, assembler)) assembled[0] = true;
                            });

                        if (!assembled[0]) {
                            context.getLogger().info(RB.$("assemblers.not.triggered"));
                        }
                    }
                } else {
                    context.getLogger().info(RB.$("assemblers.assemble.all.distributions.with"), assemblerType);
                    boolean[] assembled = new boolean[]{false};
                    assemblers.values().forEach(assembler -> {
                        if (assemble(context, assembler)) assembled[0] = true;
                    });

                    if (!assembled[0]) {
                        context.getLogger().info(RB.$("assemblers.not.triggered"));
                    }
                }
            }
        } else if (!context.getIncludedDistributions().isEmpty()) {
            for (String distributionName : context.getIncludedDistributions()) {
                context.getLogger().info(RB.$("assemblers.assemble.distribution.with.all"), distributionName);

                boolean[] assembled = new boolean[]{false};
                assemble.findAllAssemblers().stream()
                    .filter(a -> distributionName.equals(a.getName()))
                    .forEach(assembler -> {
                        if (assemble(context, assembler)) assembled[0] = true;
                    });

                if (!assembled[0]) {
                    context.getLogger().info(RB.$("assemblers.not.triggered"));
                }
            }
        } else {
            context.getLogger().info(RB.$("assemblers.assemble.all.distributions"));
            boolean assembled = false;
            for (Assembler<?> assembler : assemble.findAllAssemblers()) {
                String assemblerType = assembler.getType();
                String distributionName = assembler.getName();
                if (context.getExcludedAssemblers().contains(assemblerType) ||
                    context.getExcludedDistributions().contains(distributionName)) {
                    context.getLogger().info(RB.$("assemblers.assembler.excluded"), assemblerType, distributionName);
                    continue;
                }

                if (assemble(context, assembler)) assembled = true;
            }

            if (!assembled) {
                context.getLogger().info(RB.$("assemblers.not.triggered"));
            }
        }
    }

    private static boolean assemble(JReleaserContext context, Assembler<?> assembler) {
        try {
            context.getLogger().increaseIndent();
            context.getLogger().setPrefix(assembler.getType());

            fireAssembleEvent(ExecutionEvent.before(JReleaserCommand.ASSEMBLE.toStep()), context, assembler);

            DistributionAssembler processor = createDistributionAssembler(context, assembler);
            boolean assembled = processor.assemble();

            fireAssembleEvent(ExecutionEvent.success(JReleaserCommand.ASSEMBLE.toStep()), context, assembler);
            return assembled;
        } catch (AssemblerProcessingException e) {
            fireAssembleEvent(ExecutionEvent.failure(JReleaserCommand.ASSEMBLE.toStep(), e), context, assembler);
            throw new JReleaserException(e.getMessage(), e);
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static DistributionAssembler createDistributionAssembler(JReleaserContext context,
                                                                     Assembler<?> assembler) {
        return DistributionAssembler.builder()
            .context(context)
            .assembler(assembler)
            .build();
    }

    private static void fireAssembleEvent(ExecutionEvent event, JReleaserContext context, Assembler<?> assembler) {
        if (!assembler.isEnabled()) return;

        try {
            context.fireAssembleStepEvent(event, assembler.asImmutable());
        } catch (WorkflowListenerException e) {
            context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
            context.getLogger().trace(e);
            if (event.getType() != ExecutionEvent.Type.FAILURE && !e.getListener().isContinueOnError()) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else {
                    throw new JReleaserException(RB.$("ERROR_unexpected_error"), e.getCause());
                }
            }
        }
    }
}
