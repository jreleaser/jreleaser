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
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.options.OptionValues
import org.jreleaser.logging.JReleaserLogger
import org.jreleaser.model.Distribution
import org.jreleaser.templates.TemplateGenerator

import javax.inject.Inject
import java.nio.file.Path

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserTemplateTask extends DefaultTask {
    @Input
    @Optional
    final Property<Distribution.DistributionType> distributionType

    @Input
    @Optional
    final Property<String> distributionName

    @Input
    @Optional
    final Property<String> packagerName

    @Input
    @Optional
    final Property<String> announcerName

    @Input
    @Optional
    final Property<String> assemblerType

    @OutputDirectory
    final DirectoryProperty outputDirectory

    @Input
    final Property<Boolean> overwrite

    @Input
    final Property<Boolean> snapshot

    @Internal
    final Property<JReleaserLogger> jlogger

    @Inject
    JReleaserTemplateTask(ObjectFactory objects) {
        jlogger = objects.property(JReleaserLogger)
        distributionType = objects.property(Distribution.DistributionType).convention(Distribution.DistributionType.JAVA_BINARY)
        distributionName = objects.property(String)
        packagerName = objects.property(String)
        announcerName = objects.property(String).convention(Providers.<String> notDefined())
        assemblerType = objects.property(String).convention(Providers.<String> notDefined())
        overwrite = objects.property(Boolean).convention(false)
        snapshot = objects.property(Boolean).convention(false)

        outputDirectory = objects.directoryProperty()
    }

    @Option(option = 'overwrite', description = 'Overwrite existing files (OPTIONAL).')
    void setOverwrite(boolean overwrite) {
        this.overwrite.set(overwrite)
    }

    @Option(option = 'snapshot', description = 'Use snapshot template (OPTIONAL).')
    void setSnapshot(boolean snapshot) {
        this.snapshot.set(snapshot)
    }

    @Option(option = 'distribution-name', description = 'The name of the distribution (OPTIONAL).')
    void setDistributionName(String distributionName) {
        this.distributionName.set(distributionName)
    }

    @Option(option = 'packager-name', description = 'The name of the packager (OPTIONAL).')
    void setPackagerName(String packagerName) {
        this.packagerName.set(packagerName)
    }

    @Option(option = 'announcer-name', description = 'The name of the announcer (OPTIONAL).')
    void setAnnouncerName(String announcerName) {
        this.announcerName.set(announcerName)
    }

    @Option(option = 'assembler-type', description = 'The type of the assembler (OPTIONAL).')
    void setAssemblerType(String assemblerType) {
        this.assemblerType.set(assemblerType)
    }

    @Option(option = 'distribution-type', description = 'The type of the distribution (OPTIONAL).')
    void setAction(Distribution.DistributionType distributionType) {
        this.distributionType.set(distributionType)
    }

    @OptionValues('distribution-type')
    List<Distribution.DistributionType> getDistributionTypes() {
        return new ArrayList<Distribution.DistributionType>(Arrays.asList(Distribution.DistributionType.values()))
    }

    @TaskAction
    void generateTemplate() {
        Path output = TemplateGenerator.builder()
            .logger(jlogger.get())
            .distributionName(distributionName.orNull)
            .distributionType(distributionType.orNull)
            .packagerName(packagerName.orNull)
            .announcerName(announcerName.orNull)
            .assemblerType(assemblerType.orNull)
            .assemblerName(distributionName.orNull)
            .outputDirectory(outputDirectory.get().asFile.toPath())
            .overwrite(overwrite.get())
            .snapshot(snapshot.get())
            .build()
            .generate()

        if (output) {
            logger.info('Template generated at {}', output.toAbsolutePath())
        }
    }
}
