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
import org.jreleaser.model.Project;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.jreleaser.util.Constants.KEY_COMMIT_FULL_HASH;
import static org.jreleaser.util.Constants.KEY_COMMIT_SHORT_HASH;
import static org.jreleaser.util.Constants.KEY_MILESTONE_NAME;
import static org.jreleaser.util.Constants.KEY_PROJECT_SNAPSHOT;
import static org.jreleaser.util.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.util.Constants.KEY_RELEASE_NAME;
import static org.jreleaser.util.Constants.KEY_TAG_NAME;
import static org.jreleaser.util.Constants.KEY_TIMESTAMP;
import static org.jreleaser.util.Constants.KEY_VERSION_BUILD;
import static org.jreleaser.util.Constants.KEY_VERSION_MAJOR;
import static org.jreleaser.util.Constants.KEY_VERSION_MINOR;
import static org.jreleaser.util.Constants.KEY_VERSION_PATCH;
import static org.jreleaser.util.Constants.KEY_VERSION_TAG;
import static org.jreleaser.util.StringUtils.capitalize;

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

        context.getLogger().reset();
        if (null == exception) {
            context.getLogger().info("JReleaser succeeded after {}s", String.format("%.3f", duration));
            report(context);
        } else {
            context.getLogger().error("JReleaser failed after {}s", String.format("%.3f", duration));
            context.getLogger().error(exception.getMessage(), exception);
            report(context);
            throw exception;
        }
    }

    private void report(JReleaserContext context) {
        JReleaserModel model = context.getModel();
        Project project = model.getProject();

        Properties props = new Properties();
        props.put(KEY_TIMESTAMP, model.getTimestamp());
        props.put(KEY_COMMIT_SHORT_HASH, model.getCommit().getShortHash());
        props.put(KEY_COMMIT_FULL_HASH, model.getCommit().getFullHash());
        props.put(KEY_PROJECT_VERSION, project.getResolvedVersion());
        props.put(KEY_PROJECT_SNAPSHOT, String.valueOf(project.isSnapshot()));
        props.put(KEY_TAG_NAME, model.getRelease().getGitService().getEffectiveTagName(project));
        props.put(KEY_RELEASE_NAME, model.getRelease().getGitService().getEffectiveReleaseName());
        props.put(KEY_MILESTONE_NAME, model.getRelease().getGitService().getMilestone().getEffectiveName());

        Map<String, Object> resolvedExtraProperties = project.getResolvedExtraProperties();
        safePut("project" + capitalize(KEY_VERSION_MAJOR), resolvedExtraProperties, props);
        safePut("project" + capitalize(KEY_VERSION_MINOR), resolvedExtraProperties, props);
        safePut("project" + capitalize(KEY_VERSION_PATCH), resolvedExtraProperties, props);
        safePut("project" + capitalize(KEY_VERSION_TAG), resolvedExtraProperties, props);
        safePut("project" + capitalize(KEY_VERSION_BUILD), resolvedExtraProperties, props);

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

    private void safePut(String key, Map<String, Object> src, Properties props) {
        if (src.containsKey(key)) {
            props.put(key, src.get(key));
        }
    }
}
