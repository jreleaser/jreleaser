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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Workflows {
    public static Workflow assemble(JReleaserContext context) {
        return new WorkflowImpl(context, singletonList(
            new AssembleWorkflowItem()
        ));
    }

    public static Workflow changelog(JReleaserContext context) {
        return new WorkflowImpl(context, singletonList(
            new ChangelogWorkflowItem()
        ));
    }

    public static Workflow checksum(JReleaserContext context) {
        return new WorkflowImpl(context, asList(
            new ChangelogWorkflowItem(),
            new ChecksumWorkflowItem()
        ));
    }

    public static Workflow sign(JReleaserContext context) {
        return new WorkflowImpl(context, asList(
            new ChangelogWorkflowItem(),
            new ChecksumWorkflowItem(),
            new SignWorkflowItem()
        ));
    }

    public static Workflow release(JReleaserContext context) {
        return new WorkflowImpl(context, asList(
            new ChangelogWorkflowItem(),
            new ChecksumWorkflowItem(),
            new SignWorkflowItem(),
            new ReleaseWorkflowItem()
        ));
    }

    public static Workflow prepare(JReleaserContext context) {
        return new WorkflowImpl(context, asList(
            new ChangelogWorkflowItem(),
            new ChecksumWorkflowItem(),
            new PrepareWorkflowItem()
        ));
    }

    public static Workflow packageRelease(JReleaserContext context) {
        return new WorkflowImpl(context, asList(
            new ChangelogWorkflowItem(),
            new ChecksumWorkflowItem(),
            new PrepareWorkflowItem(),
            new PackageWorkflowItem()
        ));
    }

    public static Workflow upload(JReleaserContext context) {
        return new WorkflowImpl(context, asList(
            new ChangelogWorkflowItem(),
            new ChecksumWorkflowItem(),
            new PrepareWorkflowItem(),
            new PackageWorkflowItem(),
            new UploadWorkflowItem()
        ));
    }

    public static Workflow announce(JReleaserContext context) {
        return new WorkflowImpl(context, asList(
            new ChangelogWorkflowItem(),
            new AnnounceWorkflowItem()
        ));
    }

    public static Workflow fullRelease(JReleaserContext context) {
        return new WorkflowImpl(context, asList(
            new ChangelogWorkflowItem(),
            new ChecksumWorkflowItem(),
            new SignWorkflowItem(),
            new ReleaseWorkflowItem(),
            new PrepareWorkflowItem(),
            new PackageWorkflowItem(),
            new UploadWorkflowItem(),
            new AnnounceWorkflowItem()
        ));
    }
}
