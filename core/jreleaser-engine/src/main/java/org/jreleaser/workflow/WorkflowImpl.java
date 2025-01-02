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
package org.jreleaser.workflow;

import org.jreleaser.bundle.RB;
import org.jreleaser.engine.context.ModelValidator;
import org.jreleaser.engine.hooks.HookExecutor;
import org.jreleaser.extensions.api.ExtensionManagerHolder;
import org.jreleaser.extensions.api.workflow.WorkflowListenerException;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.util.TimeUtils.formatDuration;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class WorkflowImpl implements Workflow {
    private static final String SESSION = "session";
    private final JReleaserContext context;
    private final List<WorkflowItem> items = new ArrayList<>();

    public WorkflowImpl(JReleaserContext context, List<WorkflowItem> items) {
        this.context = context;
        ModelValidator.validate(context);
        this.items.addAll(items);
    }

    @Override
    public void execute() {
        try {
            doExecute();
        } finally {
            ExtensionManagerHolder.cleanup();
            context.getLogger().close();
        }
    }

    private void doExecute() {
        RuntimeException stepException = null;
        Throwable listenerException = null;
        Throwable startSessionException = null;
        Throwable endSessionException = null;

        Instant start = Instant.now();
        context.getLogger().info(RB.$("workflow.dryrun"), context.isDryrun());
        logFilters("workflow.included.downloader.types", context.getIncludedDownloaderTypes());
        logFilters("workflow.excluded.downloader.types", context.getExcludedDownloaderTypes());
        logFilters("workflow.included.downloader.names", context.getIncludedDownloaderNames());
        logFilters("workflow.excluded.downloader.names", context.getExcludedDownloaderNames());
        logFilters("workflow.included.assemblers", context.getIncludedAssemblers());
        logFilters("workflow.excluded.assemblers", context.getExcludedAssemblers());
        logFilters("workflow.included.distributions", context.getIncludedDistributions());
        logFilters("workflow.excluded.distributions", context.getExcludedDistributions());
        logFilters("workflow.included.catalogers", context.getIncludedCatalogers());
        logFilters("workflow.excluded.catalogers", context.getExcludedCatalogers());
        logFilters("workflow.included.packagers", context.getIncludedPackagers());
        logFilters("workflow.excluded.packagers", context.getExcludedPackagers());
        logFilters("workflow.included.deployer.types", context.getIncludedDeployerTypes());
        logFilters("workflow.excluded.deployer.types", context.getExcludedDeployerTypes());
        logFilters("workflow.included.deployer.names", context.getIncludedDeployerNames());
        logFilters("workflow.excluded.deployer.names", context.getExcludedDeployerNames());
        logFilters("workflow.included.uploader.types", context.getIncludedUploaderTypes());
        logFilters("workflow.excluded.uploader.types", context.getExcludedUploaderTypes());
        logFilters("workflow.included.uploader.names", context.getIncludedUploaderNames());
        logFilters("workflow.excluded.uploader.names", context.getExcludedUploaderNames());
        logFilters("workflow.included.announcers", context.getIncludedAnnouncers());
        logFilters("workflow.excluded.announcers", context.getExcludedAnnouncers());

        HookExecutor hooks = new HookExecutor(context);

        try {
            hooks.executeHooks(ExecutionEvent.before(SESSION));
        } catch (RuntimeException e) {
            context.getLogger().error(RB.$("ERROR_hooks_unexpected_error"));
            context.getLogger().trace(e);
            startSessionException = e;
        }

        if (null == startSessionException) {
            try {
                context.fireSessionStartEvent();
            } catch (WorkflowListenerException e) {
                context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
                context.getLogger().trace(e);
                if (!e.getListener().isContinueOnError()) {
                    startSessionException = e.getCause();
                }
            }
        }

        if (null == startSessionException) {
            for (WorkflowItem item : items) {
                boolean failure = false;
                try {
                    context.fireWorkflowEvent(ExecutionEvent.before(item.getCommand().toStep()));
                } catch (WorkflowListenerException beforeException) {
                    context.getLogger().error(RB.$("listener.failure", beforeException.getListener().getClass().getName()));
                    context.getLogger().trace(beforeException);
                    if (!beforeException.getListener().isContinueOnError()) {
                        listenerException = beforeException.getCause();
                        break;
                    }
                }

                try {
                    item.invoke(context);
                } catch (RuntimeException e) {
                    // terminate
                    failure = true;
                    stepException = e;

                    try {
                        context.fireWorkflowEvent(ExecutionEvent.failure(item.getCommand().toStep(), e));
                        break;
                    } catch (WorkflowListenerException failureException) {
                        context.getLogger().error(RB.$("listener.failure", failureException.getListener().getClass().getName()));
                        context.getLogger().trace(failureException);
                        if (!failureException.getListener().isContinueOnError()) {
                            listenerException = failureException.getCause();
                            break;
                        }
                    }
                }

                if (!failure) {
                    try {
                        context.fireWorkflowEvent(ExecutionEvent.success(item.getCommand().toStep()));
                    } catch (WorkflowListenerException afterException) {
                        context.getLogger().error(RB.$("listener.failure", afterException.getListener().getClass().getName()));
                        context.getLogger().trace(afterException);
                        if (!afterException.getListener().isContinueOnError()) {
                            listenerException = afterException.getCause();
                            break;
                        }
                    }
                }
            }
        }

        try {
            context.fireSessionEndEvent();
        } catch (WorkflowListenerException e) {
            context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
            context.getLogger().trace(e);
            if (!e.getListener().isContinueOnError()) {
                endSessionException = e.getCause();
            }
        }

        Instant end = Instant.now();

        double duration = Duration.between(start, end).toMillis() / 1000d;

        context.getLogger().reset();
        context.report();

        if (null != startSessionException) {
            context.getLogger().error(RB.$("workflow.failure"), formatDuration(duration));
            context.getLogger().trace(startSessionException);
            if (startSessionException instanceof RuntimeException) {
                throw (RuntimeException) startSessionException;
            } else {
                throw new JReleaserException(RB.$("ERROR_unexpected_error"), startSessionException);
            }
        } else if (null != endSessionException) {
            try {
                hooks.executeHooks(ExecutionEvent.failure(SESSION, endSessionException));
            } catch (RuntimeException e) {
                context.getLogger().error(RB.$("ERROR_hooks_unexpected_error"));
                context.getLogger().trace(e);
            }

            context.getLogger().error(RB.$("workflow.failure"), formatDuration(duration));
            context.getLogger().trace(endSessionException);
            if (endSessionException instanceof RuntimeException) {
                throw (RuntimeException) endSessionException;
            } else {
                throw new JReleaserException(RB.$("ERROR_unexpected_error"), endSessionException);
            }
        } else {
            if (null == stepException) {
                if (null != listenerException) {
                    if (listenerException instanceof RuntimeException) {
                        throw (RuntimeException) listenerException;
                    } else {
                        throw new JReleaserException(RB.$("ERROR_unexpected_error"), listenerException);
                    }
                }
                try {
                    hooks.executeHooks(ExecutionEvent.success(SESSION));
                } catch (RuntimeException e) {
                    context.getLogger().error(RB.$("ERROR_hooks_unexpected_error"));
                    context.getLogger().trace(e);
                }

                context.getLogger().info(RB.$("workflow.success"), formatDuration(duration));
            } else {
                try {
                    hooks.executeHooks(ExecutionEvent.failure(SESSION, stepException));
                } catch (RuntimeException e) {
                    context.getLogger().error(RB.$("ERROR_hooks_unexpected_error"));
                    context.getLogger().trace(e);
                }

                context.getLogger().error(RB.$("workflow.failure"), formatDuration(duration));
                context.getLogger().trace(stepException);
                throw stepException;
            }
        }
    }

    private void logFilters(String key, List<String> input) {
        if (!input.isEmpty()) {
            context.getLogger().info(RB.$(key, input));
        }
    }
}
