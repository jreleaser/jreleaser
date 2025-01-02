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

import org.jreleaser.extensions.api.ExtensionPoint;
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
 * @author Andres Almiray
 * @since 1.3.0
 */
public interface WorkflowListener extends ExtensionPoint {
    /**
     * Signals JReleaser to halt execution if this listener fails.
     *
     * @return {@code false} to halt execution on failure, {@code true} to continue.
     */
    boolean isContinueOnError();

    /**
     * Triggered when the execution session starts.
     *
     * @param context the execution context.
     */
    void onSessionStart(JReleaserContext context);

    /**
     * Triggered when the execution session ends.
     *
     * @param context the execution context.
     */
    void onSessionEnd(JReleaserContext context);

    /**
     * Triggered when a workflow step starts/ends/fails.
     *
     * @param event   event metadata.
     * @param context the execution context.
     */
    void onWorkflowStep(ExecutionEvent event, JReleaserContext context);

    /**
     * Triggered when an announcer starts/ends/fails.
     *
     * @param event     event metadata.
     * @param context   the execution context.
     * @param announcer the executing announcer.
     */
    void onAnnounceStep(ExecutionEvent event, JReleaserContext context, Announcer announcer);

    /**
     * Triggered when an assembler starts/ends/fails.
     *
     * @param event     event metadata.
     * @param context   the execution context.
     * @param assembler the executing assembler.
     */
    void onAssembleStep(ExecutionEvent event, JReleaserContext context, Assembler assembler);

    /**
     * Triggered when a cataloger starts/ends/fails.
     *
     * @param event     event metadata.
     * @param context   the execution context.
     * @param cataloger the executing cataloger.
     */
    void onCatalogStep(ExecutionEvent event, JReleaserContext context, Cataloger cataloger);

    /**
     * Triggered when a deployer starts/ends/fails.
     *
     * @param event    event metadata.
     * @param context  the execution context.
     * @param deployer the executing deployer.
     */
    void onDeployStep(ExecutionEvent event, JReleaserContext context, Deployer deployer);

    /**
     * Triggered when a downloader starts/ends/fails.
     *
     * @param event      event metadata.
     * @param context    the execution context.
     * @param downloader the executing downloader.
     */
    void onDownloadStep(ExecutionEvent event, JReleaserContext context, Downloader downloader);

    /**
     * Triggered when an uploader starts/ends/fails.
     *
     * @param event    event metadata.
     * @param context  the execution context.
     * @param uploader the executing uploader.
     */
    void onUploadStep(ExecutionEvent event, JReleaserContext context, Uploader uploader);

    /**
     * Triggered when a releaser starts/ends/fails.
     *
     * @param event    event metadata.
     * @param context  the execution context.
     * @param releaser the executing releaser.
     */
    void onReleaseStep(ExecutionEvent event, JReleaserContext context, Releaser releaser);

    /**
     * Triggered when a distribution is about to be processed.
     *
     * @param context      the execution context.
     * @param distribution the distribution to be processed.
     */
    void onDistributionStart(JReleaserContext context, Distribution distribution);

    /**
     * Triggered when a distribution has been processed.
     *
     * @param context      the execution context.
     * @param distribution the processed distribution.
     */
    void onDistributionEnd(JReleaserContext context, Distribution distribution);

    /**
     * Triggered when a packager starts/ends/fails the preparing step.
     *
     * @param event        event metadata.
     * @param context      the execution context.
     * @param distribution the distribution to be processed.
     * @param packager     the executing packager.
     */
    void onPackagerPrepareStep(ExecutionEvent event, JReleaserContext context, Distribution distribution, Packager packager);

    /**
     * Triggered when a packager starts/ends/fails the packaging step.
     *
     * @param event        event metadata.
     * @param context      the execution context.
     * @param distribution the distribution to be processed.
     * @param packager     the executing packager.
     */
    void onPackagerPackageStep(ExecutionEvent event, JReleaserContext context, Distribution distribution, Packager packager);

    /**
     * Triggered when a packager starts/ends/fails the publishing step.
     *
     * @param event        event metadata.
     * @param context      the execution context.
     * @param distribution the distribution to be processed.
     * @param packager     the executing packager.
     */
    void onPackagerPublishStep(ExecutionEvent event, JReleaserContext context, Distribution distribution, Packager packager);
}
