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
package org.jreleaser.gradle.plugin.internal

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.internal.dsl.DistributionImpl
import org.jreleaser.gradle.plugin.tasks.JReleaserAnnounceTask
import org.jreleaser.gradle.plugin.tasks.JReleaserChecksumTask
import org.jreleaser.gradle.plugin.tasks.JReleaserConfigTask
import org.jreleaser.gradle.plugin.tasks.JReleaserFullReleaseTask
import org.jreleaser.gradle.plugin.tasks.JReleaserPackageTask
import org.jreleaser.gradle.plugin.tasks.JReleaserPrepareTask
import org.jreleaser.gradle.plugin.tasks.JReleaserReleaseTask
import org.jreleaser.gradle.plugin.tasks.JReleaserSignTask
import org.jreleaser.gradle.plugin.tasks.JReleaserTemplateGeneratorTask
import org.jreleaser.gradle.plugin.tasks.JReleaserUploadTask
import org.jreleaser.model.JReleaserModel
import org.jreleaser.model.JReleaserModelValidator

import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JReleaserProjectConfigurer {
    private static final String JRELEASER_GROUP = 'JReleaser'

    static void configure(Project project) {
        JReleaserExtensionImpl extension = (JReleaserExtensionImpl) project.extensions.findByType(JReleaserExtension)

        boolean hasDistributionPlugin = configureDefaultDistribution(project, extension)

        String javaVersion = ''
        if (project.hasProperty('targetCompatibility')) {
            javaVersion = String.valueOf(project.findProperty('targetCompatibility'))
        }
        if (project.hasProperty('compilerRelease')) {
            javaVersion = String.valueOf(project.findProperty('compilerRelease'))
        }
        if (isBlank(javaVersion)) {
            javaVersion = JavaVersion.current().toString()
        }

        JReleaserModel model = extension.toModel()
        if (isBlank(model.project.javaVersion)) model.project.javaVersion = javaVersion
        if (isBlank(model.project.artifactId)) model.project.artifactId = project.name
        if (isBlank(model.project.groupId)) model.project.groupId = project.group.toString()

        JReleaserLoggerAdapter logger = new JReleaserLoggerAdapter(project)
        List<String> errors = JReleaserModelValidator.validate(logger, project.projectDir.toPath(), model)
        if (errors) {
            project.logger.error('== JReleaser ==')
            errors.each { project.logger.error(it) }
            throw new GradleException("JReleaser for project ${project.name} has not been properly configured.")
        }

        project.tasks.register('jreleaserConfig', JReleaserConfigTask,
            new Action<JReleaserConfigTask>() {
                @Override
                void execute(JReleaserConfigTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Outputs current JReleaser configuration'
                    t.jreleaserModel.set(model)
                }
            })

        project.tasks.register('jeleaserTemplate', JReleaserTemplateGeneratorTask,
            new Action<JReleaserTemplateGeneratorTask>() {
                @Override
                void execute(JReleaserTemplateGeneratorTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Generates templates for a specific tool'
                    t.outputDirectory.set(project.layout
                        .projectDirectory
                        .dir('src/distributions'))
                }
            })

        Provider<Directory> outputDirectoryProvider = project.layout
            .buildDirectory
            .dir('jreleaser')

        project.tasks.register('jreleaserChecksum', JReleaserChecksumTask,
            new Action<JReleaserChecksumTask>() {
                @Override
                void execute(JReleaserChecksumTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Calculate checksums'
                    t.jreleaserModel.set(model)
                    t.outputDirectory.set(outputDirectoryProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn('assembleDist')
                    }
                }
            })

        project.tasks.register('jreleaserSign', JReleaserSignTask,
            new Action<JReleaserSignTask>() {
                @Override
                void execute(JReleaserSignTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Signs a release'
                    t.jreleaserModel.set(model)
                    t.outputDirectory.set(outputDirectoryProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn('assembleDist')
                    }
                }
            })

        project.tasks.register('jreleaserRelease', JReleaserReleaseTask,
            new Action<JReleaserReleaseTask>() {
                @Override
                void execute(JReleaserReleaseTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Creates or updates a release'
                    t.jreleaserModel.set(model)
                    t.dryrun.set(extension.dryrun)
                    t.outputDirectory.set(outputDirectoryProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn('assembleDist')
                    }
                }
            })

        TaskProvider<JReleaserPrepareTask> prepareTask = project.tasks.register('jreleaserPrepare', JReleaserPrepareTask,
            new Action<JReleaserPrepareTask>() {
                @Override
                void execute(JReleaserPrepareTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Prepares all distributions'
                    t.jreleaserModel.set(model)
                    t.outputDirectory.set(outputDirectoryProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn('assembleDist')
                    }
                }
            })

        TaskProvider<JReleaserPackageTask> packageTask = project.tasks.register('jreleaserPackage', JReleaserPackageTask,
            new Action<JReleaserPackageTask>() {
                @Override
                void execute(JReleaserPackageTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Packages all distributions'
                    t.dependsOn(prepareTask)
                    t.jreleaserModel.set(model)
                    t.outputDirectory.set(outputDirectoryProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn('assembleDist')
                    }
                }
            })

        project.tasks.register('jreleaserUpload', JReleaserUploadTask,
            new Action<JReleaserUploadTask>() {
                @Override
                void execute(JReleaserUploadTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Uploads all distributions'
                    t.dependsOn(packageTask)
                    t.jreleaserModel.set(model)
                    t.dryrun.set(extension.dryrun)
                    t.outputDirectory.set(outputDirectoryProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn('assembleDist')
                    }
                }
            })

        project.tasks.register('jreleaserAnnounce', JReleaserAnnounceTask,
            new Action<JReleaserAnnounceTask>() {
                @Override
                void execute(JReleaserAnnounceTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Announces a release'
                    t.jreleaserModel.set(model)
                    t.dryrun.set(extension.dryrun)
                    t.outputDirectory.set(outputDirectoryProvider)
                }
            })

        project.tasks.register('jreleaserFullRelease', JReleaserFullReleaseTask,
            new Action<JReleaserFullReleaseTask>() {
                @Override
                void execute(JReleaserFullReleaseTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Invokes JReleaser on all distributions'
                    t.jreleaserModel.set(model)
                    t.dryrun.set(extension.dryrun)
                    t.outputDirectory.set(outputDirectoryProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn('assembleDist')
                    }
                }
            })
    }

    private static boolean configureDefaultDistribution(Project project, JReleaserExtensionImpl extension) {
        boolean hasDistributionPlugin = project.plugins.findPlugin('distribution')

        if (hasDistributionPlugin) {
            Action<DistributionImpl> configurer = new Action<DistributionImpl>() {
                @Override
                void execute(DistributionImpl distribution) {
                    if (distribution.artifacts.size() > 0) return
                    distribution.artifact(new Action<Artifact>() {
                        @Override
                        void execute(Artifact artifact) {
                            artifact.path.set(project.tasks
                                .named('distZip', Zip)
                                .flatMap({ tr -> tr.archiveFile }))
                        }
                    })
                    distribution.artifact(new Action<Artifact>() {
                        @Override
                        void execute(Artifact artifact) {
                            artifact.path.set(project.tasks
                                .named('distTar', Tar)
                                .flatMap({ tr -> tr.archiveFile }))
                        }
                    })
                }
            }

            String distributionName = project.name
            if (extension.distributions.findByName(distributionName)) {
                extension.distributions.named(project.name, DistributionImpl, configurer)
            } else {
                extension.distributions.register(project.name, configurer)
            }
        }

        return hasDistributionPlugin
    }
}
