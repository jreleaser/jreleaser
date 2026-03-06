/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.gradle.plugin.internal.JReleaserGradleProjectCapture
import org.jreleaser.gradle.plugin.internal.JReleaserLoggerService
import org.jreleaser.gradle.plugin.internal.JReleaserProjectConfigurer

import javax.inject.Inject

/**
 *
 * @author Markus Hoffrogge
 * @since 1.24.0
 */
@CompileStatic
abstract class AbstractJReleaserDefaultTask extends DefaultTask {
    @Internal
    final Property<JReleaserExtension> extension

    @Internal
    final Property<JReleaserLoggerService> jlogger

    @Internal
    final DirectoryProperty projectDirectory

    @Input
    final Property<JReleaserGradleProjectCapture> gradleProjectCapture

    @OutputDirectory
    final DirectoryProperty outputDirectory

    @Inject
    AbstractJReleaserDefaultTask(ObjectFactory objects) {
        extension = objects.property(JReleaserExtension)
        jlogger = objects.property(JReleaserLoggerService)
        gradleProjectCapture = objects.property(JReleaserGradleProjectCapture)
        projectDirectory = objects.directoryProperty()
        outputDirectory = objects.directoryProperty()

        // Configure properties accessing project at configuration time only
        extension.convention(JReleaserProjectConfigurer.getJReleaserExtension(project))
        Provider<JReleaserLoggerService> jloggerServiceProvider
            = JReleaserProjectConfigurer.getJReleaserLoggerServiceProvider(project)
        jlogger.convention(jloggerServiceProvider)
        usesService(jloggerServiceProvider)
        gradleProjectCapture.convention(JReleaserGradleProjectCapture.of(project))
        projectDirectory.convention(project.layout.projectDirectory)
        outputDirectory.convention(JReleaserProjectConfigurer.getJReleaserOutputDirectoryProvider(project))
        // Use project.pluginManager.withPlugin() to defer the dependency configuration until the plugin is actually applied
        project.pluginManager.withPlugin('distribution') {
            dependsOn(project.provider {
                extension.get().dependsOnAssemble.getOrElse(true) ?
                    [JReleaserProjectConfigurer.ASSEMBLE_DIST_TASK_NAME] :
                    []
            })
        }
    }

}
