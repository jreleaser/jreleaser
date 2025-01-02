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
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.jreleaser.engine.environment.Environment
import org.jreleaser.logging.JReleaserLogger

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.5.0
 */
@CompileStatic
abstract class JReleaserEnvTask extends DefaultTask {
    static final String NAME = 'jreleaserEnv'

    @Input
    final Property<JReleaserLogger> jlogger

    @InputDirectory
    final DirectoryProperty basedir

    @Inject
    JReleaserEnvTask(ObjectFactory objects) {
        jlogger = objects.property(JReleaserLogger)
        basedir = objects.directoryProperty()
    }

    @TaskAction
    void performAction() {
        Environment.display(jlogger.get(), basedir.get().asFile.toPath())
    }
}
