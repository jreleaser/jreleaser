/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
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
 * @since 0.3.0
 */
@CompileStatic
abstract class JReleaserUploadTask extends AbstractJReleaserTask {
    @Input
    @Optional
    final Property<String> uploaderType

    @Input
    @Optional
    final Property<String> uploaderName

    @Inject
    JReleaserUploadTask(ObjectFactory objects) {
        super(objects)
        uploaderType = objects.property(String).convention(Providers.notDefined())
        uploaderName = objects.property(String).convention(Providers.notDefined())
    }

    @Option(option = 'uploader-type', description = 'The type of the uploader (OPTIONAL).')
    void setUploaderType(String uploaderType) {
        this.uploaderType.set(uploaderType)
    }

    @Option(option = 'uploader-name', description = 'The name of the uploader (OPTIONAL).')
    void setUploaderName(String uploaderName) {
        this.uploaderName.set(uploaderName)
    }


    @TaskAction
    void performAction() {
        JReleaserContext ctx = createContext()
        ctx.uploaderType = uploaderType.orNull
        ctx.uploaderName = uploaderName.orNull
        Workflows.upload(ctx).execute()
    }
}
