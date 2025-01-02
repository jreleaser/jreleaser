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
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.engine.context.ModelValidator
import org.jreleaser.gradle.plugin.internal.GradleJReleaserModelPrinter
import org.jreleaser.model.api.JReleaserCommand
import org.jreleaser.model.internal.JReleaserContext

import javax.inject.Inject

import static org.jreleaser.model.api.JReleaserContext.Mode.ANNOUNCE
import static org.jreleaser.model.api.JReleaserContext.Mode.ASSEMBLE
import static org.jreleaser.model.api.JReleaserContext.Mode.CHANGELOG
import static org.jreleaser.model.api.JReleaserContext.Mode.CONFIG
import static org.jreleaser.model.api.JReleaserContext.Mode.DOWNLOAD

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserConfigTask extends AbstractPlatformAwareJReleaserTask {
    static final String NAME = 'jreleaserConfig'

    @Input
    final Property<Boolean> full

    @Input
    final Property<Boolean> announce

    @Input
    final Property<Boolean> assembly

    @Input
    final Property<Boolean> changelog

    @Input
    final Property<Boolean> download

    @Inject
    JReleaserConfigTask(ObjectFactory objects) {
        super(objects)
        full = objects.property(Boolean).convention(false)
        assembly = objects.property(Boolean).convention(false)
        announce = objects.property(Boolean).convention(false)
        changelog = objects.property(Boolean).convention(false)
        download = objects.property(Boolean).convention(false)
        command = JReleaserCommand.CONFIG
    }

    @Option(option = 'full', description = 'Display full configuration (OPTIONAL).')
    void setFull(boolean full) {
        this.full.set(full)
    }

    @Option(option = 'announce', description = 'Display announce configuration (OPTIONAL).')
    void setAnnounce(boolean announce) {
        this.announce.set(announce)
    }

    @Option(option = 'assembly', description = 'Display assembly configuration (OPTIONAL).')
    void setAssembly(boolean assembly) {
        this.assembly.set(assembly)
    }

    @Option(option = 'changelog', description = 'Display changelog configuration (OPTIONAL).')
    void setChangelog(boolean changelog) {
        this.changelog.set(changelog)
    }

    @Option(option = 'download', description = 'Display download configuration (OPTIONAL).')
    void setDownload(boolean download) {
        this.download.set(download)
    }

    @TaskAction
    void performAction() {
        if (download.get()) {
            mode = DOWNLOAD
        } else if (announce.get()) {
            mode = ANNOUNCE
        } else if (assembly.get()) {
            mode = ASSEMBLE
        } else if (changelog.get()) {
            mode = CHANGELOG
        } else {
            mode = CONFIG
        }

        JReleaserContext context = createContext()
        ModelValidator.validate(context)
        new GradleJReleaserModelPrinter(project)
            .print(context.model.asMap(full.get()))
        context.report()
    }
}
