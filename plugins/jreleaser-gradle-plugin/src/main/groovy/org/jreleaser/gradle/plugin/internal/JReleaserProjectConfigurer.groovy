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
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.crypto.checksum.Checksum
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.internal.dsl.DistributionImpl
import org.jreleaser.gradle.plugin.tasks.JReleaserConfigTask
import org.jreleaser.gradle.plugin.tasks.JReleaserReleaseTask
import org.jreleaser.gradle.plugin.tasks.JReleaserTemplateGeneratorTask
import org.jreleaser.gradle.plugin.tasks.JReleaserToolPackagerTask
import org.jreleaser.gradle.plugin.tasks.JReleaserToolProcessorTask
import org.jreleaser.model.Distribution
import org.jreleaser.model.JReleaserModel
import org.jreleaser.model.JReleaserModelValidator
import org.kordamp.gradle.util.StringUtils

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
        javaVersion += '+'

        JReleaserModel model = extension.toModel()
        model.getProject().setJavaVersion(javaVersion)
        JReleaserLoggerAdapter logger = new JReleaserLoggerAdapter(project.logger)
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

        project.tasks.register('generateJReleaserTemplate', JReleaserTemplateGeneratorTask,
            new Action<JReleaserTemplateGeneratorTask>() {
                @Override
                void execute(JReleaserTemplateGeneratorTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Generates a tool file template'
                    t.outputDirectory.set(project.layout
                        .projectDirectory
                        .dir('src/distributions'))
                }
            })

        Set<TaskProvider<?>> checksumTasks = new LinkedHashSet<>()
        Set<TaskProvider<?>> prepareTasks = new LinkedHashSet<>()
        Set<TaskProvider<?>> packageTasks = new LinkedHashSet<>()
        model.distributions.values().each { distribution ->
            List<TaskProvider<?>> tasks = configureDistribution(project, model, distribution)
            if (tasks) {
                checksumTasks << tasks[0]
                prepareTasks << tasks[1]
                packageTasks << tasks[2]
            }
        }

        if (checksumTasks) {
            project.tasks.register('checksum', DefaultTask,
                new Action<DefaultTask>() {
                    @Override
                    void execute(DefaultTask t) {
                        t.group = JRELEASER_GROUP
                        t.description = 'Generates checksums for all distributions'
                        t.dependsOn(checksumTasks)
                    }
                })
        }

        Set<TaskProvider<?>> jreleaserDeps = new LinkedHashSet<>()
        if (prepareTasks) {
            jreleaserDeps << project.tasks.register('jreleaserPrepare', DefaultTask,
                new Action<DefaultTask>() {
                    @Override
                    void execute(DefaultTask t) {
                        t.group = JRELEASER_GROUP
                        t.description = 'Prepares all distributions'
                        t.dependsOn(prepareTasks)
                        if (hasDistributionPlugin) {
                            t.dependsOn('assembleDist')
                        }
                    }
                })
        }

        if (packageTasks) {
            jreleaserDeps << project.tasks.register('jreleaserPackage', DefaultTask,
                new Action<DefaultTask>() {
                    @Override
                    void execute(DefaultTask t) {
                        t.group = JRELEASER_GROUP
                        t.description = 'Packages all distributions'
                        t.dependsOn(packageTasks)
                    }
                })
        }

        jreleaserDeps << project.tasks.register('createRelease', JReleaserReleaseTask,
            new Action<JReleaserReleaseTask>() {
                @Override
                void execute(JReleaserReleaseTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Creates or updates a release'
                    t.jreleaserModel.set(model)
                }
            })

        if (jreleaserDeps) {
            project.tasks.register('jreleaser', DefaultTask,
                new Action<DefaultTask>() {
                    @Override
                    void execute(DefaultTask t) {
                        t.group = JRELEASER_GROUP
                        t.description = 'Invokes JReleaser on all distributions'
                        t.dependsOn(jreleaserDeps)
                    }
                })
        }
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

    private static List<TaskProvider<?>> configureDistribution(Project project,
                                                               JReleaserModel model,
                                                               Distribution distribution) {
        Set<TaskProvider<JReleaserToolProcessorTask>> prepareTasks = new LinkedHashSet<>()
        Set<TaskProvider<JReleaserToolPackagerTask>> packageTasks = new LinkedHashSet<>()

        String distributionName = distribution.name
        String normalizedDistributionName = StringUtils.getPropertyNameForLowerCaseHyphenSeparatedName(distributionName)
        String capitalizedDistributionName = normalizedDistributionName.capitalize()

        for (String toolName : Distribution.supportedTools()) {
            if (distribution.findTool(toolName)?.enabled) {
                String taskName = "prepare${toolName.capitalize()}${capitalizedDistributionName}".toString()
                TaskProvider<JReleaserToolProcessorTask> prt = createJReleaserToolProcessorTask(project, taskName, distributionName, toolName, model, distribution)
                prepareTasks << prt
                taskName = "package${toolName.capitalize()}${capitalizedDistributionName}".toString()
                packageTasks << createJReleaserToolPackagerTask(project, taskName, distributionName, toolName, model, distribution, prt)
            }
        }

        if (prepareTasks) {
            Provider<Directory> checksumDirectory = project.layout
                .buildDirectory
                .dir('jreleaser/checksums/' + distributionName)

            TaskProvider<Checksum> checksumTask = project.tasks.register("checksum${capitalizedDistributionName}", Checksum,
                new Action<Checksum>() {
                    @Override
                    void execute(Checksum t) {
                        t.group = JRELEASER_GROUP
                        t.description = "Generates checksums for the ${distributionName} distribution"
                        t.files = project.files(distribution.artifacts*.path)
                        t.algorithm = Checksum.Algorithm.SHA256
                        t.outputDir = checksumDirectory.get().asFile
                    }
                })
            prepareTasks.each { TaskProvider<JReleaserToolProcessorTask> tp ->
                tp.configure(new Action<JReleaserToolProcessorTask>() {
                    @Override
                    void execute(JReleaserToolProcessorTask t) {
                        t.dependsOn(checksumTask)
                        t.checksumDirectory.set(checksumDirectory)
                    }
                })
            }
            packageTasks.each { TaskProvider<JReleaserToolPackagerTask> tp ->
                tp.configure(new Action<JReleaserToolPackagerTask>() {
                    @Override
                    void execute(JReleaserToolPackagerTask t) {
                        t.dependsOn(checksumTask)
                        t.checksumDirectory.set(checksumDirectory)
                    }
                })
            }

            TaskProvider<DefaultTask> prepareTask = project.tasks.register("prepare${capitalizedDistributionName}", DefaultTask,
                new Action<DefaultTask>() {
                    @Override
                    void execute(DefaultTask t) {
                        t.group = JRELEASER_GROUP
                        t.description = "Prepares the ${distributionName} distribution"
                        t.dependsOn(prepareTasks)
                    }
                })

            TaskProvider<DefaultTask> packageTask = project.tasks.register("package${capitalizedDistributionName}", DefaultTask,
                new Action<DefaultTask>() {
                    @Override
                    void execute(DefaultTask t) {
                        t.group = JRELEASER_GROUP
                        t.description = "Packages the ${distributionName} distribution"
                        t.dependsOn(packageTasks)
                    }
                })

            // pleasing the static compiler ... *grumble*
            List<TaskProvider<?>> list = []
            list.add(checksumTask)
            list.add(prepareTask)
            list.add(packageTask)
            return list
        }

        return []
    }

    private static TaskProvider<JReleaserToolProcessorTask> createJReleaserToolProcessorTask(Project project,
                                                                                             String taskName,
                                                                                             String distributionName,
                                                                                             String toolName,
                                                                                             JReleaserModel model,
                                                                                             Distribution distribution) {
        project.tasks.register(taskName, JReleaserToolProcessorTask,
            new Action<JReleaserToolProcessorTask>() {
                @Override
                void execute(JReleaserToolProcessorTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = "Prepares distribution ${distributionName} with ${toolName.capitalize()}".toString()
                    t.distributionName.set(distributionName)
                    t.toolName.set(toolName)
                    t.jreleaserModel.set(model)
                    t.artifacts.from(distribution.artifacts*.path)
                    t.outputDirectory.set(project.layout
                        .buildDirectory
                        .dir("jreleaser/${distributionName}/${toolName}".toString()))
                }
            })
    }

    private static TaskProvider<JReleaserToolPackagerTask> createJReleaserToolPackagerTask(Project project,
                                                                                           String taskName,
                                                                                           String distributionName,
                                                                                           String toolName,
                                                                                           JReleaserModel model,
                                                                                           Distribution distribution,
                                                                                           TaskProvider<JReleaserToolProcessorTask> prt) {
        project.tasks.register(taskName, JReleaserToolPackagerTask,
            new Action<JReleaserToolPackagerTask>() {
                @Override
                void execute(JReleaserToolPackagerTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = "Packages distribution ${distributionName} with ${toolName.capitalize()}".toString()
                    t.dependsOn(prt)
                    t.distributionName.set(distributionName)
                    t.toolName.set(toolName)
                    t.jreleaserModel.set(model)
                    t.artifacts.from(distribution.artifacts*.path)
                    t.outputDirectory.set(project.layout
                        .buildDirectory
                        .dir("jreleaser/${distributionName}/${toolName}".toString()))
                }
            })
    }
}
