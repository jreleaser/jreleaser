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
package org.jreleaser.workflow;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.util.Constants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class WorkflowImpl implements Workflow {
    private final JReleaserContext context;
    private final List<WorkflowItem> items = new ArrayList<>();

    public WorkflowImpl(JReleaserContext context, List<WorkflowItem> items) {
        this.context = context;
        this.items.addAll(items);
    }

    public void execute() {
        JReleaserException exception = null;

        Instant start = Instant.now();
        context.getLogger().info("dryrun set to {}", context.isDryrun());

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

        if (null == exception) {
            context.getLogger().info("JReleaser succeeded after {}s", String.format("%.3f", duration));
            report(context);
        } else {
            context.getLogger().error("JReleaser failed after {}s", String.format("%.3f", duration));
            report(context);
            throw exception;
        }
    }

    private void report(JReleaserContext context) {
        JReleaserModel model = context.getModel();

        Properties props = new Properties();
        props.put(Constants.KEY_TIMESTAMP, model.getTimestamp());
        props.put(Constants.KEY_COMMIT_SHORT_HASH, model.getCommit().getShortHash());
        props.put(Constants.KEY_COMMIT_FULL_HASH, model.getCommit().getFullHash());
        props.put(Constants.KEY_PROJECT_VERSION, model.getProject().getResolvedVersion());
        props.put(Constants.KEY_TAG_NAME, model.getRelease().getGitService().getResolvedTagName(model.getProject()));

        Path output = context.getOutputDirectory().resolve("output.properties");

        try (FileOutputStream out = new FileOutputStream(output.toFile())) {
            context.getLogger().info("Writing output properties to {}",
                context.getBasedir().relativize(output));
            props.store(out, "JReleaser " + JReleaserVersion.getPlainVersion());
        } catch (IOException ignored) {
            context.getLogger().warn("Could not write output properties to {}",
                context.getBasedir().relativize(output));
        }
    }
}
