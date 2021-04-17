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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.jreleaser.engine.context.ContextCreator
import org.jreleaser.gradle.plugin.internal.JReleaserLoggerAdapter
import org.jreleaser.model.JReleaserContext
import org.jreleaser.model.JReleaserModel

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Path

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractJReleaserTask extends DefaultTask {
    @Input
    final Property<Boolean> dryrun

    @Input
    final DirectoryProperty outputDirectory

    @Internal
    final Property<JReleaserModel> model

    @Internal
    JReleaserContext.Mode mode

    @Inject
    AbstractJReleaserTask(ObjectFactory objects) {
        model = objects.property(JReleaserModel)
        mode = JReleaserContext.Mode.FULL
        dryrun = objects.property(Boolean).convention(false)
        outputDirectory = objects.directoryProperty()
    }

    protected JReleaserContext createContext() {
        Path outputDirectoryPath = outputDirectory.get().asFile.toPath()
        Files.createDirectories(outputDirectoryPath)
        PrintWriter tracer = new PrintWriter(new FileOutputStream(outputDirectoryPath
            .resolve('trace.log').toFile()))

        return ContextCreator.create(
            new JReleaserLoggerAdapter(project, tracer),
            mode,
            model.get(),
            project.projectDir.toPath(),
            outputDirectoryPath,
            dryrun.get())
    }
}
