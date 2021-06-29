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
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.jreleaser.engine.context.ContextCreator
import org.jreleaser.model.JReleaserContext
import org.jreleaser.model.JReleaserModel
import org.jreleaser.model.JReleaserVersion
import org.jreleaser.util.JReleaserLogger

import javax.inject.Inject

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
    final Property<Boolean> gitRootSearch

    @Input
    final DirectoryProperty outputDirectory

    @Internal
    final Property<JReleaserModel> model

    @Internal
    final Property<JReleaserLogger> jlogger

    @Internal
    JReleaserContext.Mode mode

    @Inject
    AbstractJReleaserTask(ObjectFactory objects) {
        model = objects.property(JReleaserModel)
        jlogger = objects.property(JReleaserLogger)
        mode = JReleaserContext.Mode.FULL
        dryrun = objects.property(Boolean).convention(false)
        gitRootSearch = objects.property(Boolean).convention(false)
        outputDirectory = objects.directoryProperty()
    }

    protected JReleaserContext createContext() {
        JReleaserLogger logger = jlogger.get()

        logger.info('JReleaser {}', JReleaserVersion.getPlainVersion())
        JReleaserVersion.banner(logger.getTracer(), false)
        logger.increaseIndent()
        logger.info('- basedir set to {}', project.projectDir.toPath().toAbsolutePath())
        logger.decreaseIndent()

        return ContextCreator.create(
            logger,
            mode,
            model.get(),
            project.projectDir.toPath(),
            outputDirectory.get().asFile.toPath(),
            dryrun.get(),
            gitRootSearch.get())
    }
}
