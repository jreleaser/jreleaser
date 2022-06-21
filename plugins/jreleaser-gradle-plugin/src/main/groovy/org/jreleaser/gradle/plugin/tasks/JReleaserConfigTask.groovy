/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
import org.jreleaser.gradle.plugin.internal.JReleaserModelPrinter
import org.jreleaser.model.JReleaserContext

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserConfigTask extends AbstractPlatformAwareJReleaserTask {
    @Input
    final Property<Boolean> full

    @Input
    final Property<Boolean> assembly

    @Input
    final Property<Boolean> download

    @Inject
    JReleaserConfigTask(ObjectFactory objects) {
        super(objects)
        full = objects.property(Boolean).convention(false)
        assembly = objects.property(Boolean).convention(false)
        download = objects.property(Boolean).convention(false)
    }

    @Option(option = 'full', description = 'Display full configuration (OPTIONAL).')
    void setFull(boolean full) {
        this.full.set(full)
    }

    @Option(option = 'assembly', description = 'Display assembly configuration (OPTIONAL).')
    void setAssembly(boolean assembly) {
        this.assembly.set(assembly)
    }

    @Option(option = 'download', description = 'Display download configuration (OPTIONAL).')
    void setDownload(boolean download) {
        this.download.set(download)
    }

    @TaskAction
    void displayConfig() {
        if (download.get()) {
            mode = JReleaserContext.Mode.DOWNLOAD
        } else if (assembly.get()) {
            mode = JReleaserContext.Mode.ASSEMBLE
        } else {
            mode = JReleaserContext.Mode.CONFIG
        }

        JReleaserContext context = createContext()
        ModelValidator.validate(context)
        new JReleaserModelPrinter(project)
            .print(context.model.asMap(full.get()))
        context.report()
    }
}
