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
package org.jreleaser.workflow;

import org.jreleaser.bundle.RB;
import org.jreleaser.engine.context.ModelValidator;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.JReleaserException;

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
    private final JReleaserContext context;
    private final List<WorkflowItem> items = new ArrayList<>();

    public WorkflowImpl(JReleaserContext context, List<WorkflowItem> items) {
        this.context = context;
        ModelValidator.validate(context);
        this.items.addAll(items);
    }

    public void execute() {
        JReleaserException exception = null;

        Instant start = Instant.now();
        context.getLogger().info(RB.$("workflow.dryrun"), context.isDryrun());
        logFilters("workflow.included.assemblers", context.getIncludedAssemblers());
        logFilters("workflow.excluded.assemblers", context.getExcludedAssemblers());
        logFilters("workflow.included.distributions", context.getIncludedDistributions());
        logFilters("workflow.excluded.distributions", context.getExcludedDistributions());
        logFilters("workflow.included.packagers", context.getIncludedPackagers());
        logFilters("workflow.excluded.packagers", context.getExcludedPackagers());
        logFilters("workflow.included.uploader.types", context.getIncludedUploaderTypes());
        logFilters("workflow.excluded.uploader.types", context.getExcludedUploaderTypes());
        logFilters("workflow.included.uploader.names", context.getIncludedUploaderNames());
        logFilters("workflow.excluded.uploader.names", context.getExcludedUploaderNames());
        logFilters("workflow.included.announcers", context.getIncludedAnnouncers());
        logFilters("workflow.excluded.announcers", context.getExcludedAnnouncers());

        for (WorkflowItem item : items) {
            try {
                item.invoke(context);
            } catch (JReleaserException e) {
                // terminate
                exception = e;
                break;
            }
        }
        Instant end = Instant.now();

        double duration = Duration.between(start, end).toMillis() / 1000d;

        context.getLogger().reset();
        context.report();
        if (null == exception) {
            context.getLogger().info(RB.$("workflow.success"), formatDuration(duration));
        } else {
            context.getLogger().error(RB.$("workflow.failure"), formatDuration(duration));
            context.getLogger().trace(exception);
            throw exception;
        }
    }

    private void logFilters(String key, List<String> input) {
        if (!input.isEmpty()) {
            context.getLogger().info(RB.$(key, input));
        }
    }
}
