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
package org.jreleaser.engine.distribution;

import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.workflow.WorkflowListenerException;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.Packager;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;

import java.util.List;
import java.util.Locale;

import static org.jreleaser.model.internal.JReleaserSupport.supportedPackagers;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Distributions {
    private Distributions() {
        // noop
    }

    public static void process(JReleaserContext context, DistributionProcessor.PackagingAction action) {
        List<Distribution> activeDistributions = context.getModel().getActiveDistributions();

        if (activeDistributions.isEmpty()) {
            context.getLogger().debug(RB.$("distributions.not.enabled"), action.getText().toLowerCase(Locale.ENGLISH));
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
                        if (!supportedPackagers().contains(packagerName)) {
                            context.getLogger().warn(RB.$("ERROR_unsupported_packager", packagerName));
                            continue;
                        }

                        context.getLogger().info(RB.$("distributions.apply.action"), action.getText());

                        processDistribution(context, distribution, packagerName, action);
                    }
                } else {
                    processDistribution(context, distribution, action);
                }
            }
        } else if (!context.getIncludedPackagers().isEmpty()) {
            for (String packagerName : context.getIncludedPackagers()) {
                if (!supportedPackagers().contains(packagerName)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_packager", packagerName));
                    continue;
                }

                context.getLogger().info(RB.$("distributions.apply.action"), action.getText());
                for (Distribution distribution : activeDistributions) {
                    processDistribution(context, distribution, packagerName, action);
                }
            }
        } else {
            // process all
            context.getLogger().info(RB.$("distributions.apply.action"), action.getText());
            for (Distribution distribution : activeDistributions) {
                if (context.getExcludedDistributions().contains(distribution.getName())) {
                    context.getLogger().info(RB.$("distributions.distribution.excluded"), distribution.getName());
                    continue;
                }

                processDistribution(context, distribution, action);
            }
        }
    }

    private static void processDistribution(JReleaserContext context, Distribution distribution, DistributionProcessor.PackagingAction action) {
        context.getLogger().increaseIndent();
        context.getLogger().info(RB.$("distributions.apply.action.to"), action.getText(), distribution.getName());

        fireDistributionStartEvent(context, distribution);

        for (String packagerName : supportedPackagers()) {
            if (context.getExcludedPackagers().contains(packagerName)) {
                context.getLogger().info(RB.$("packagers.packager.excluded"), packagerName);
                continue;
            }
            processPackager(context, distribution, packagerName, action);
        }

        fireDistributionEndEvent(context, distribution);

        context.getLogger().decreaseIndent();
    }

    private static void processDistribution(JReleaserContext context, Distribution distribution, String packagerName, DistributionProcessor.PackagingAction action) {
        context.getLogger().increaseIndent();
        context.getLogger().info(RB.$("distributions.apply.action.to"), action.getText(), distribution.getName());

        processPackager(context, distribution, packagerName, action);

        context.getLogger().decreaseIndent();
    }

    private static void processPackager(JReleaserContext context, Distribution distribution, String packagerName, DistributionProcessor.PackagingAction action) {
        Packager<?> packager = distribution.findPackager(packagerName);

        try {
            context.getLogger().increaseIndent();
            context.getLogger().setPrefix(packagerName);
            firePackagerEvent(ExecutionEvent.before(actionToStep(action.getType())), context, distribution, action.getType(), packager);

            DistributionProcessor processor = createDistributionProcessor(context,
                distribution,
                packagerName);

            action.getFunction().consume(processor);

            firePackagerEvent(ExecutionEvent.success(actionToStep(action.getType())), context, distribution, action.getType(), packager);
        } catch (PackagerProcessingException e) {
            firePackagerEvent(ExecutionEvent.failure(actionToStep(action.getType()), e), context, distribution, action.getType(), packager);
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static String actionToStep(DistributionProcessor.PackagingAction.Type type) {
        switch (type) {
            case PACKAGE:
                return JReleaserCommand.PACKAGE.toStep();
            case PUBLISH:
                return JReleaserCommand.PUBLISH.toStep();
            default:
                // noop
                break;
        }
        return JReleaserCommand.PREPARE.toStep();
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

    private static void fireDistributionStartEvent(JReleaserContext context, Distribution distribution) {
        try {
            context.fireDistributionStartEvent(distribution.asImmutable());
        } catch (WorkflowListenerException e) {
            context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
            context.getLogger().trace(e);
            if (!e.getListener().isContinueOnError()) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else {
                    throw new JReleaserException(RB.$("ERROR_unexpected_error"), e.getCause());
                }
            }
        }
    }

    private static void fireDistributionEndEvent(JReleaserContext context, Distribution distribution) {
        if (!distribution.isEnabled()) return;

        try {
            context.fireDistributionEndEvent(distribution.asImmutable());
        } catch (WorkflowListenerException e) {
            context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
            context.getLogger().trace(e);
            if (!e.getListener().isContinueOnError()) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else {
                    throw new JReleaserException(RB.$("ERROR_unexpected_error"), e.getCause());
                }
            }
        }
    }

    private static void firePackagerEvent(ExecutionEvent event, JReleaserContext context, Distribution distribution, DistributionProcessor.PackagingAction.Type type, Packager<?> packager) {
        if (!packager.isEnabled()) return;

        try {
            switch (type) {
                case PREPARE:
                    context.firePackagerPrepareEvent(event, distribution.asImmutable(), packager.asImmutable());
                    break;
                case PACKAGE:
                    context.firePackagerPackageEvent(event, distribution.asImmutable(), packager.asImmutable());
                    break;
                case PUBLISH:
                    context.firePackagerPublishEvent(event, distribution.asImmutable(), packager.asImmutable());
                    break;
            }
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
