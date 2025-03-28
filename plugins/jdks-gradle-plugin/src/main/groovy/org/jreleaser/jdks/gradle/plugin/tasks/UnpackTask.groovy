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
package org.jreleaser.jdks.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jreleaser.util.FileUtils

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.0.0
 */
@CompileStatic
abstract class UnpackTask extends DefaultTask {
    @InputFile
    final RegularFileProperty inputFile

    @OutputDirectory
    final DirectoryProperty outputDirectory

    @Inject
    UnpackTask(ObjectFactory objects) {
        inputFile = objects.fileProperty()
        outputDirectory = objects.directoryProperty()
    }

    @TaskAction
    void unpack() {
        FileUtils.unpackArchive(
            inputFile.get().asFile.toPath(),
            outputDirectory.get().asFile.toPath(),
            false)
    }
}
