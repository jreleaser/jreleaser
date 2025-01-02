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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.engine.context.ModelValidator
import org.jreleaser.engine.templates.TemplateEvaluator
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
 * @since 1.5.0
 */
@CompileStatic
abstract class JReleaserTemplateEvalTask extends AbstractPlatformAwareJReleaserTask {
    static final String NAME = 'jreleaserTemplateEval'

    @Input
    final Property<Boolean> announce

    @Input
    final Property<Boolean> assembly

    @Input
    final Property<Boolean> changelog

    @Input
    final Property<Boolean> download

    @Optional
    @InputFile
    final RegularFileProperty inputFile

    @Optional
    @InputDirectory
    final DirectoryProperty inputDirectory

    @OutputDirectory
    final DirectoryProperty targetDirectory

    @Input
    final Property<Boolean> overwrite

    @Inject
    JReleaserTemplateEvalTask(ObjectFactory objects) {
        super(objects)
        assembly = objects.property(Boolean).convention(false)
        announce = objects.property(Boolean).convention(false)
        changelog = objects.property(Boolean).convention(false)
        download = objects.property(Boolean).convention(false)
        overwrite = objects.property(Boolean).convention(false)

        inputFile = objects.fileProperty()
        inputDirectory = objects.directoryProperty()
        targetDirectory = objects.directoryProperty()

        command = JReleaserCommand.CONFIG
    }

    @Option(option = 'announce', description = 'Eval model in announce configuration (OPTIONAL).')
    void setAnnounce(boolean announce) {
        this.announce.set(announce)
    }

    @Option(option = 'assembly', description = 'Eval model in assembly configuration (OPTIONAL).')
    void setAssembly(boolean assembly) {
        this.assembly.set(assembly)
    }

    @Option(option = 'changelog', description = 'Eval model in changelog configuration (OPTIONAL).')
    void setChangelog(boolean changelog) {
        this.changelog.set(changelog)
    }

    @Option(option = 'download', description = 'Eval model in download configuration (OPTIONAL).')
    void setDownload(boolean download) {
        this.download.set(download)
    }

    @Option(option = 'input-file', description = 'An input template file.')
    void setInputFile(String inputFile) {
        this.inputFile.set(new File(inputFile))
    }

    @Option(option = 'input-directory', description = 'A directory with input templates.')
    void setInputDirectory(String inputDirectory) {
        this.inputDirectory.set(new File(inputDirectory))
    }

    @Option(option = 'target-directory', description = 'Directory where evaluated template(s) will be placed.')
    void setTargetDirectory(String targetDirectory) {
        this.targetDirectory.set(new File(targetDirectory))
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

        if (inputFile.present) {
            TemplateEvaluator.generateTemplate(context, inputFile.get().asFile.toPath(),
                context.relativizeToBasedir(targetDirectory.get().asFile.toPath()), overwrite.getOrElse(false))
        } else if (null != inputDirectory) {
            TemplateEvaluator.generateTemplates(context, inputDirectory.get().asFile.toPath(),
                context.relativizeToBasedir(targetDirectory.get().asFile.toPath()), overwrite.getOrElse(false))
        }

        context.report()
    }
}
