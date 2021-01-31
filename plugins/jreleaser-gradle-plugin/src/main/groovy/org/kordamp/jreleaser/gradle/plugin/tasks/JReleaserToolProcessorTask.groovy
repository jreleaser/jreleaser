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
package org.kordamp.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.kordamp.jreleaser.gradle.plugin.internal.JReleaserLoggerAdapter
import org.kordamp.jreleaser.model.JReleaserModel
import org.kordamp.jreleaser.tools.DistributionProcessor

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserToolProcessorTask extends DefaultTask {
    @Internal
    final Property<JReleaserModel> jreleaserModel

    @Input
    final Property<String> distributionName

    @Input
    final Property<String> toolName

    @InputFiles
    final ConfigurableFileCollection artifacts

    @InputDirectory
    final DirectoryProperty checksumDirectory

    @OutputDirectory
    final DirectoryProperty outputDirectory

    @Inject
    JReleaserToolProcessorTask(ObjectFactory objects) {
        jreleaserModel = objects.property(JReleaserModel)
        distributionName = objects.property(String)
        toolName = objects.property(String)
        artifacts = objects.fileCollection()
        checksumDirectory = objects.directoryProperty()

        outputDirectory = objects.directoryProperty()
    }

    @TaskAction
    void createOutput() {
        boolean result = DistributionProcessor.builder()
                .logger(new JReleaserLoggerAdapter(project.logger))
                .model(jreleaserModel.get())
                .distributionName(distributionName.get())
                .toolName(toolName.get())
                .checksumDirectory(checksumDirectory.get().asFile.toPath())
                .outputDirectory(outputDirectory.get().asFile.toPath())
                .build()
                .prepareDistribution()

        if (result) {
            println("Prepared ${distributionName.get()} distribution with tool ${toolName.get()}")
        }
    }
}
