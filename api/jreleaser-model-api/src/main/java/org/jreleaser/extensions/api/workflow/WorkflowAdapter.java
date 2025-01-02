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
package org.jreleaser.extensions.api.workflow;

import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.api.announce.Announcer;
import org.jreleaser.model.api.assemble.Assembler;
import org.jreleaser.model.api.catalog.Cataloger;
import org.jreleaser.model.api.deploy.Deployer;
import org.jreleaser.model.api.distributions.Distribution;
import org.jreleaser.model.api.download.Downloader;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.api.packagers.Packager;
import org.jreleaser.model.api.release.Releaser;
import org.jreleaser.model.api.upload.Uploader;

/**
 * Base implementation of the {@code WorkflowListener} interface.
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
public class WorkflowAdapter implements WorkflowListener {
    @Override
    public boolean isContinueOnError() {
        return false;
    }

    @Override
    public void onSessionStart(JReleaserContext context) {
        // noop
    }

    @Override
    public void onSessionEnd(JReleaserContext context) {
        // noop
    }

    @Override
    public void onWorkflowStep(ExecutionEvent event, JReleaserContext context) {
        // noop
    }

    @Override
    public void onAnnounceStep(ExecutionEvent event, JReleaserContext context, Announcer announcer) {
        // noop
    }

    @Override
    public void onAssembleStep(ExecutionEvent event, JReleaserContext context, Assembler assembler) {
        // noop
    }

    @Override
    public void onCatalogStep(ExecutionEvent event, JReleaserContext context, Cataloger cataloger) {
        // noop
    }

    @Override
    public void onDeployStep(ExecutionEvent event, JReleaserContext context, Deployer deployer) {
        // noop
    }

    @Override
    public void onDownloadStep(ExecutionEvent event, JReleaserContext context, Downloader downloader) {
        // noop
    }

    @Override
    public void onUploadStep(ExecutionEvent event, JReleaserContext context, Uploader uploader) {
        // noop
    }

    @Override
    public void onReleaseStep(ExecutionEvent event, JReleaserContext context, Releaser releaser) {
        // noop
    }

    @Override
    public void onPackagerPrepareStep(ExecutionEvent event, JReleaserContext context, Distribution distribution, Packager packager) {
        // noop
    }

    @Override
    public void onPackagerPackageStep(ExecutionEvent event, JReleaserContext context, Distribution distribution, Packager packager) {
        // noop
    }

    @Override
    public void onPackagerPublishStep(ExecutionEvent event, JReleaserContext context, Distribution distribution, Packager packager) {
        // noop
    }

    @Override
    public void onDistributionStart(JReleaserContext context, Distribution distribution) {
        // noop
    }

    @Override
    public void onDistributionEnd(JReleaserContext context, Distribution distribution) {
        // noop
    }
}
