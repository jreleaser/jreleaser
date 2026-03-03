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
package org.jreleaser.gradle.plugin.internal

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildServiceSpec
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.model.internal.JReleaserModel
import org.kordamp.gradle.util.AnsiConsole

import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JReleaserProjectConfigurer {
    static final String ASSEMBLE_DIST_TASK_NAME = 'assembleDist'

    static JReleaserExtension getJReleaserExtension(Project gradleProject) {
        return gradleProject.extensions.findByType(JReleaserExtension)
    }

    static Provider<Directory> getJReleaserOutputDirectoryProvider(Project gradleProject) {
        return gradleProject.layout.buildDirectory
            .dir('jreleaser')
    }

    static Provider<JReleaserLoggerService> getJReleaserLoggerServiceProvider(Project gradleProject) {
        Gradle gradle = gradleProject.gradle
        Provider<Directory> outputDirectoryProvider = getJReleaserOutputDirectoryProvider(gradleProject)
        return gradle.sharedServices.registerIfAbsent(
            "jreleaserLogger_${gradleProject.path.replace(':', '_')}",
            JReleaserLoggerService.class) {
            BuildServiceSpec<JReleaserLoggerService.Params> spec ->
                spec.parameters.console.set(new AnsiConsole(gradle, 'JRELEASER'))
                spec.parameters.logLevel.set(gradle.startParameter.logLevel)
                spec.parameters.outputDirectory.set(outputDirectoryProvider)
        }
    }

    static void configure(Project gradleProject) {
        Provider<JReleaserLoggerService> loggerProvider = getJReleaserLoggerServiceProvider(gradleProject)
        gradleProject.tasks.named('clean', new Action<Task>() {
            @Override
            void execute(Task t) {
                t.doFirst(new Action<Task>() {
                    @Override
                    void execute(Task task) {
                        loggerProvider.get().close()
                    }
                })
            }
        })

    }

    static void configureModel(JReleaserGradleProjectCapture gradleProjectCapture, JReleaserModel model) {
        String javaVersion = ''
        if (gradleProjectCapture.hasProperty('targetCompatibility')) {
            javaVersion = String.valueOf(gradleProjectCapture.findProperty('targetCompatibility'))
        }
        if (gradleProjectCapture.hasProperty('compilerRelease')) {
            javaVersion = String.valueOf(gradleProjectCapture.findProperty('compilerRelease'))
        }
        if (isBlank(javaVersion)) {
            javaVersion = JavaVersion.current().toString()
        }

        if (isBlank(model.project.languages.java.version)) model.project.languages.java.version = javaVersion
        if (isBlank(model.project.languages.java.artifactId)) model.project.languages.java.artifactId = gradleProjectCapture.name
        if (isBlank(model.project.languages.java.groupId)) model.project.languages.java.groupId = gradleProjectCapture.group
        if (!model.project.languages.java.multiProjectSet) {
            model.project.languages.java.multiProject = gradleProjectCapture.isMultiProject
        }

        if (isBlank(model.project.languages.java.mainClass)) {
            model.project.languages.java.mainClass = gradleProjectCapture.javaApplicationMainClass
        }
        if (isBlank(model.project.languages.java.mainModule)) {
            model.project.languages.java.mainModule = gradleProjectCapture.javaApplicationMainModule
        }
    }
}
