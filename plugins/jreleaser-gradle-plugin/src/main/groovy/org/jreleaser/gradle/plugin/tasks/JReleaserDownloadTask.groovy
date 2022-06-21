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
package org.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.model.JReleaserContext
import org.jreleaser.workflow.Workflows

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
abstract class JReleaserDownloadTask extends AbstractJReleaserTask {
    @Input
    @Optional
    final ListProperty<String> downloaderTypes

    @Input
    @Optional
    final ListProperty<String> excludedDownloaderTypes

    @Input
    @Optional
    final ListProperty<String> downloaderNames

    @Input
    @Optional
    final ListProperty<String> excludedDownloaderNames

    @Inject
    JReleaserDownloadTask(ObjectFactory objects) {
        super(objects)
        downloaderTypes = objects.listProperty(String).convention([])
        excludedDownloaderTypes = objects.listProperty(String).convention([])
        downloaderNames = objects.listProperty(String).convention([])
        excludedDownloaderNames = objects.listProperty(String).convention([])
    }

    @Option(option = 'downloader', description = 'Include a downloader by type (OPTIONAL).')
    void setDownloaderType(List<String> downloaderTypes) {
        this.downloaderTypes.set(downloaderTypes)
    }

    @Option(option = 'exclude-downloader', description = 'Exclude a downloader by type (OPTIONAL).')
    void setExcludeDownloaderType(List<String> excludedDownloaderTypes) {
        this.excludedDownloaderTypes.set(excludedDownloaderTypes)
    }

    @Option(option = 'downloader-name', description = 'Include a downloader by name (OPTIONAL).')
    void setDownloaderName(List<String> downloaderNames) {
        this.downloaderNames.set(downloaderNames)
    }

    @Option(option = 'exclude-downloader-name', description = 'Exclude a downloader by name (OPTIONAL).')
    void setExcludeDownloaderName(List<String> excludedDownloaderNames) {
        this.excludedDownloaderNames.set(excludedDownloaderNames)
    }

    @TaskAction
    void performAction() {
        mode = JReleaserContext.Mode.DOWNLOAD
        JReleaserContext ctx = createContext()
        ctx.includedDownloaderTypes = downloaderTypes.orNull
        ctx.excludedDownloaderTypes = excludedDownloaderTypes.orNull
        ctx.includedDownloaderNames = downloaderNames.orNull
        ctx.excludedDownloaderNames = excludedDownloaderNames.orNull
        Workflows.download(ctx).execute()
    }
}
