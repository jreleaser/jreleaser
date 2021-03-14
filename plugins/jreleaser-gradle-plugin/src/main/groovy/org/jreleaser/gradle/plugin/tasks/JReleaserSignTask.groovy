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
package org.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jreleaser.gradle.plugin.internal.JReleaserLoggerAdapter
import org.jreleaser.model.JReleaserModel
import org.jreleaser.signer.Signer
import org.jreleaser.tools.Checksums
import org.jreleaser.util.Logger

import javax.inject.Inject
import java.nio.file.Path

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserSignTask extends DefaultTask {
    @Internal
    final Property<JReleaserModel> jreleaserModel

    @OutputDirectory
    final DirectoryProperty outputDirectory

    @Inject
    JReleaserSignTask(ObjectFactory objects) {
        jreleaserModel = objects.property(JReleaserModel)
        outputDirectory = objects.directoryProperty()
    }

    @TaskAction
    void signArtifacts() {
        Path checksumDirectory = outputDirectory.getAsFile().get().toPath().resolve("checksums")

        Logger logger = new JReleaserLoggerAdapter(project.logger)

        Checksums.collectAndWriteChecksums(logger,
            jreleaserModel.get(),
            checksumDirectory)

        Signer.sign(logger,
            jreleaserModel.get(),
            outputDirectory.getAsFile().get().toPath())
    }
}
