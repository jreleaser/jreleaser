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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.api.tasks.options.Option
import org.jreleaser.engine.init.Init
import org.jreleaser.gradle.plugin.internal.JReleaserLoggerService
import org.jreleaser.logging.JReleaserLogger
import org.jreleaser.model.JReleaserException

import javax.inject.Inject

import static org.jreleaser.bundle.RB.$

/**
 *
 * @author Andres Almiray
 * @since 1.4.0
 */
@CompileStatic
@UntrackedTask(because = 'writes to project.basedir')
abstract class JReleaserInitTask extends DefaultTask {
    static final String NAME = 'jreleaserInit'

    @Input
    @Optional
    final Property<String> format

    @OutputDirectory
    final DirectoryProperty outputDirectory

    @Input
    final Property<Boolean> overwrite

    @Internal
    final Property<JReleaserLoggerService> jlogger

    @Inject
    JReleaserInitTask(ObjectFactory objects) {
        jlogger = objects.property(JReleaserLoggerService)
        format = objects.property(String).convention(Providers.<String> notDefined())
        overwrite = objects.property(Boolean).convention(false)

        outputDirectory = objects.directoryProperty()
    }

    @Option(option = 'overwrite', description = 'Overwrite existing files (OPTIONAL).')
    void setOverwrite(boolean overwrite) {
        this.overwrite.set(overwrite)
    }

    @Option(option = 'format', description = 'Configuration file format (REQUIRED).')
    void setFormat(String format) {
        this.format.set(format)
    }

    @TaskAction
    void performAction() {
        JReleaserLogger logger = jlogger.get().logger
        try {
            Init.execute(logger, format.orNull, overwrite.getOrElse(false), outputDirectory.getAsFile().get().toPath())
        } catch (IllegalStateException e) {
            throw new JReleaserException($('ERROR_unexpected_error'), e)
        } finally {
            if (null != logger) logger.close()
        }
    }
}
