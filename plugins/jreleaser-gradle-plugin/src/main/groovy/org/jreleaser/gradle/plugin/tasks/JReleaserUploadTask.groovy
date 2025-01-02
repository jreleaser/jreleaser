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
package org.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.model.api.JReleaserCommand
import org.jreleaser.model.internal.JReleaserContext
import org.jreleaser.workflow.Workflows

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
abstract class JReleaserUploadTask extends AbstractJReleaserDistributionTask {
    static final String NAME = 'jreleaserUpload'

    @Input
    @Optional
    final ListProperty<String> uploaderTypes

    @Input
    @Optional
    final ListProperty<String> excludedUploaderTypes

    @Input
    @Optional
    final ListProperty<String> uploaderNames

    @Input
    @Optional
    final ListProperty<String> excludedUploaderNames

    @Input
    @Optional
    final ListProperty<String> catalogers

    @Input
    @Optional
    final ListProperty<String> excludedCatalogers

    @Inject
    JReleaserUploadTask(ObjectFactory objects) {
        super(objects)
        uploaderTypes = objects.listProperty(String).convention([])
        excludedUploaderTypes = objects.listProperty(String).convention([])
        uploaderNames = objects.listProperty(String).convention([])
        excludedUploaderNames = objects.listProperty(String).convention([])
        catalogers = objects.listProperty(String).convention([])
        excludedCatalogers = objects.listProperty(String).convention([])
        command = JReleaserCommand.UPLOAD
    }

    @Option(option = 'uploader', description = 'Include an uploader by type (OPTIONAL).')
    void setUploaderType(List<String> uploaderTypes) {
        this.uploaderTypes.set(uploaderTypes)
    }

    @Option(option = 'exclude-uploader', description = 'Exclude an uploader by type (OPTIONAL).')
    void setExcludeUploaderType(List<String> excludedUploaderTypes) {
        this.excludedUploaderTypes.set(excludedUploaderTypes)
    }

    @Option(option = 'uploader-name', description = 'Include an uploader by name (OPTIONAL).')
    void setUploaderName(List<String> uploaderNames) {
        this.uploaderNames.set(uploaderNames)
    }

    @Option(option = 'exclude-uploader-name', description = 'Exclude an uploader by name (OPTIONAL).')
    void setExcludeUploaderName(List<String> excludedUploaderNames) {
        this.excludedUploaderNames.set(excludedUploaderNames)
    }

    @Option(option = 'cataloger', description = 'Include a cataloger (OPTIONAL).')
    void setCataloger(List<String> cataloges) {
        this.catalogers.set(cataloges)
    }

    @Option(option = 'exclude-cataloger', description = 'Exclude a cataloger (OPTIONAL).')
    void setExcludeCataloger(List<String> excludedCatalogers) {
        this.excludedCatalogers.set(excludedCatalogers)
    }

    @TaskAction
    void performAction() {
        JReleaserContext ctx = setupContext()
        ctx.includedUploaderTypes = uploaderTypes.orNull
        ctx.excludedUploaderTypes = excludedUploaderTypes.orNull
        ctx.includedUploaderNames = uploaderNames.orNull
        ctx.excludedUploaderNames = excludedUploaderNames.orNull
        ctx.includedCatalogers = catalogers.orNull
        ctx.excludedCatalogers = excludedCatalogers.orNull
        Workflows.upload(ctx).execute()
    }
}
