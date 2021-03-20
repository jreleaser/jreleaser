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
import org.gradle.api.tasks.options.OptionValues
import org.jreleaser.gradle.plugin.internal.JReleaserLoggerAdapter
import org.jreleaser.model.Distribution
import org.jreleaser.templates.TemplateGenerator

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserTemplateGeneratorTask extends DefaultTask {
    @Internal
    final Property<Distribution.DistributionType> distributionType

    @Input
    final Property<String> distributionName

    @Input
    final Property<String> toolName

    @OutputDirectory
    final DirectoryProperty outputDirectory

    @Input
    final Property<Boolean> overwrite

    @Inject
    JReleaserTemplateGeneratorTask(ObjectFactory objects) {
        distributionType = objects.property(Distribution.DistributionType).convention(Distribution.DistributionType.BINARY)
        distributionName = objects.property(String)
        toolName = objects.property(String)
        overwrite = objects.property(Boolean).convention(false)

        outputDirectory = objects.directoryProperty()
    }

    @Option(option = 'overwrite', description = 'Overwrite existing files (OPTIONAL).')
    void setOverwrite(boolean overwrite) {
        this.overwrite.set(overwrite)
    }

    @Option(option = 'distribution-name', description = 'The name of the distribution (REQUIRED).')
    void setDistributionName(String distributionName) {
        this.distributionName.set(distributionName)
    }

    @Option(option = 'tool-name', description = 'The name of the tool (REQUIRED).')
    void setToolName(String toolName) {
        this.toolName.set(toolName)
    }

    @Option(option = 'distribution-type', description = 'The type of the distribution (REQUIRED).')
    void setAction(Distribution.DistributionType distributionType) {
        this.distributionType.set(distributionType)
    }

    @OptionValues('distribution-type')
    List<Distribution.DistributionType> getDistributionTypes() {
        return new ArrayList<Distribution.DistributionType>(Arrays.asList(Distribution.DistributionType.values()))
    }

    @TaskAction
    void generateTemplate() {
        boolean result = TemplateGenerator.builder()
            .logger(new JReleaserLoggerAdapter(project))
            .distributionName(distributionName.get())
            .distributionType(distributionType.get())
            .toolName(toolName.get())
            .outputDirectory(outputDirectory.get().asFile.toPath())
            .overwrite(overwrite.get())
            .build()
            .generate()

        if (result) {
            println('Template generated at ' +
                outputDirectory.get().asFile.toPath()
                    .resolve(distributionName.get())
                    .resolve(toolName.get()))
        }
    }
}
