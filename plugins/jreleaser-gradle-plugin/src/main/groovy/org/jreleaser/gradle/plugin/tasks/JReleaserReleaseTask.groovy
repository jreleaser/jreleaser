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
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.gradle.plugin.internal.JReleaserLoggerAdapter
import org.jreleaser.model.JReleaserModel
import org.jreleaser.releaser.Releasers
import org.jreleaser.tools.Checksums

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserReleaseTask extends DefaultTask {
    @Internal
    final Property<JReleaserModel> jreleaserModel

    @Input
    final Property<Boolean> dryrun

    @OutputDirectory
    final DirectoryProperty checksumDirectory

    @Inject
    JReleaserReleaseTask(ObjectFactory objects) {
        jreleaserModel = objects.property(JReleaserModel)
        checksumDirectory = objects.directoryProperty()
        dryrun = objects.property(Boolean).convention(false)
    }

    @Option(option = 'dryrun', description = 'Skips network operations (OPTIONAL).')
    void setDryrun(boolean dryrun) {
        this.dryrun.set(dryrun)
    }

    @TaskAction
    void createRelease() {
        Checksums.collectAndWriteChecksums(new JReleaserLoggerAdapter(project.logger),
            jreleaserModel.get(),
            checksumDirectory.getAsFile().get().toPath())

        Releasers.release(new JReleaserLoggerAdapter(project.logger),
            jreleaserModel.get(),
            project.projectDir.toPath(),
            checksumDirectory.get().asFile.toPath(),
            dryrun.get())
    }
}
