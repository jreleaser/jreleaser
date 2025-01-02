/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.options.Option
import org.jreleaser.engine.context.ContextCreator
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.gradle.plugin.internal.JReleaserExtensionImpl
import org.jreleaser.gradle.plugin.internal.JReleaserLoggerService
import org.jreleaser.gradle.plugin.internal.JReleaserProjectConfigurer
import org.jreleaser.logging.JReleaserLogger
import org.jreleaser.model.JReleaserVersion
import org.jreleaser.model.api.JReleaserCommand
import org.jreleaser.model.internal.JReleaserContext
import org.jreleaser.model.internal.JReleaserModel
import org.jreleaser.util.Env
import org.jreleaser.util.PlatformUtils
import org.jreleaser.util.StringUtils

import javax.inject.Inject

import static java.util.stream.Collectors.toList
import static org.jreleaser.model.api.JReleaserContext.Mode.FULL
import static org.jreleaser.model.internal.JReleaserContext.Configurer
import static org.jreleaser.util.StringUtils.isBlank
import static org.jreleaser.util.StringUtils.isNotBlank

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
    final Property<Boolean> strict

    @InputFiles
    final DirectoryProperty outputDirectory

    @Internal
    final Property<JReleaserLoggerService> jlogger

    @Internal
    org.jreleaser.model.api.JReleaserContext.Mode mode

    @Internal
    JReleaserCommand command

    @Inject
    AbstractJReleaserTask(ObjectFactory objects) {
        jlogger = objects.property(JReleaserLoggerService)
        mode = FULL
        dryrun = objects.property(Boolean)
        gitRootSearch = objects.property(Boolean)
        strict = objects.property(Boolean)
        outputDirectory = objects.directoryProperty()
    }

    @Option(option = 'dryrun', description = 'Skip remote operations (OPTIONAL).')
    void setDryrun(boolean dryrun) {
        this.dryrun.set(dryrun)
    }

    @Option(option = 'git-root-search', description = 'Searches for the Git root (OPTIONAL).')
    void setGitRootSearch(boolean gitRootSearch) {
        this.gitRootSearch.set(gitRootSearch)
    }

    @Option(option = 'strict', description = 'Enable strict mode (OPTIONAL).')
    void setStrict(boolean strict) {
        this.strict.set(strict)
    }

    protected JReleaserModel createModel() {
        JReleaserExtensionImpl extension = (JReleaserExtensionImpl) project.extensions.findByType(JReleaserExtension)
        JReleaserModel model = extension.toModel(project, jlogger.get().logger)
        JReleaserProjectConfigurer.configureModel(project, model)
        model
    }

    protected JReleaserContext createContext() {
        JReleaserLogger logger = jlogger.get().logger
        PlatformUtils.resolveCurrentPlatform(logger)

        logger.info('JReleaser {}', JReleaserVersion.getPlainVersion())
        JReleaserVersion.banner(logger.getTracer())
        logger.increaseIndent()
        logger.info('- basedir set to {}', project.projectDir.toPath().toAbsolutePath())
        logger.info('- outputdir set to {}', outputDirectory.get().asFile.toPath().toAbsolutePath())
        logger.decreaseIndent()

        return ContextCreator.create(
            logger,
            resolveConfigurer(project.extensions.findByType(JReleaserExtension)),
            mode,
            command,
            createModel(),
            project.projectDir.toPath(),
            outputDirectory.get().asFile.toPath(),
            dryrun.getOrElse(false),
            gitRootSearch.getOrElse(false),
            strict.getOrElse(false),
            collectSelectedPlatforms(),
            collectRejectedPlatforms())
    }

    protected boolean resolveBoolean(String key, Boolean value, Boolean defaultValue) {
        if (null != value) return value
        String resolvedValue = Env.resolve(key, '')
        return isNotBlank(resolvedValue) ? Boolean.parseBoolean(resolvedValue) : defaultValue
    }

    protected List<String> resolveCollection(String key, List<String> values) {
        if (!values.isEmpty()) return values
        String resolvedValue = Env.resolve(key, '')
        if (isBlank(resolvedValue)) return Collections.emptyList()
        return Arrays.stream(resolvedValue.trim().split(','))
            .map({ s -> s.trim() })
            .filter({ s -> isNotBlank(s) })
            .collect(toList())
    }

    protected List<String> collectSelectedPlatforms() {
        []
    }

    protected List<String> collectRejectedPlatforms() {
        []
    }

    protected Configurer resolveConfigurer(JReleaserExtension extension) {
        if (!extension.configFile.present) return Configurer.GRADLE

        File configFile = extension.configFile.get().asFile
        switch (StringUtils.getFilenameExtension(configFile.name)) {
            case 'yml':
            case 'yaml':
                return Configurer.CLI_YAML
            case 'toml':
                return Configurer.CLI_TOML
            case 'json':
                return Configurer.CLI_JSON
        }
        // should not happen!
        throw new IllegalArgumentException('Invalid configuration format: ' + configFile.name)
    }
}
