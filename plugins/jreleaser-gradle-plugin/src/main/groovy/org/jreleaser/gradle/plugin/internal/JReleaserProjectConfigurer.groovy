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
package org.jreleaser.gradle.plugin.internal

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildServiceSpec
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.gradle.plugin.tasks.JReleaseAutoConfigReleaseTask
import org.jreleaser.gradle.plugin.tasks.JReleaserAnnounceTask
import org.jreleaser.gradle.plugin.tasks.JReleaserAssembleTask
import org.jreleaser.gradle.plugin.tasks.JReleaserCatalogTask
import org.jreleaser.gradle.plugin.tasks.JReleaserChangelogTask
import org.jreleaser.gradle.plugin.tasks.JReleaserChecksumTask
import org.jreleaser.gradle.plugin.tasks.JReleaserConfigTask
import org.jreleaser.gradle.plugin.tasks.JReleaserDeployTask
import org.jreleaser.gradle.plugin.tasks.JReleaserDownloadTask
import org.jreleaser.gradle.plugin.tasks.JReleaserFullReleaseTask
import org.jreleaser.gradle.plugin.tasks.JReleaserInitTask
import org.jreleaser.gradle.plugin.tasks.JReleaserPackageTask
import org.jreleaser.gradle.plugin.tasks.JReleaserPrepareTask
import org.jreleaser.gradle.plugin.tasks.JReleaserPublishTask
import org.jreleaser.gradle.plugin.tasks.JReleaserReleaseTask
import org.jreleaser.gradle.plugin.tasks.JReleaserSignTask
import org.jreleaser.gradle.plugin.tasks.JReleaserTemplateEvalTask
import org.jreleaser.gradle.plugin.tasks.JReleaserTemplateGenerateTask
import org.jreleaser.gradle.plugin.tasks.JReleaserUploadTask
import org.jreleaser.model.internal.JReleaserModel
import org.kordamp.gradle.util.AnsiConsole

import static org.kordamp.gradle.util.StringUtils.isBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JReleaserProjectConfigurer {
    static final String ASSEMBLE_DIST_TASK_NAME = 'assembleDist'

    static void configure(Project project) {
        JReleaserExtensionImpl extension = (JReleaserExtensionImpl) project.extensions.findByType(JReleaserExtension)

        boolean hasDistributionPlugin = extension.dependsOnAssemble.getOrElse(true) && configureDefaultDistribution(project, extension)

        Provider<Directory> outputDirectory = project.layout.buildDirectory
            .dir('jreleaser')

        Provider<JReleaserLoggerService> loggerProvider = project.gradle.sharedServices
            .registerIfAbsent('jreleaserLogger', JReleaserLoggerService.class) { BuildServiceSpec<JReleaserLoggerService.Params> spec ->
                spec.parameters.console.set(new AnsiConsole(project, 'JRELEASER'))
                spec.parameters.logLevel.set(project.gradle.startParameter.logLevel)
                spec.parameters.outputDirectory.set(outputDirectory)
            }

        project.tasks.named('clean', new Action<Task>() {
            @Override
            void execute(Task t) {
                t.doFirst(new Action<Task>() {
                    @Override
                    void execute(Task task) {
                        loggerProvider.get().close()
                    }
                })
            }
        })

        project.tasks.named(JReleaserConfigTask.NAME, JReleaserConfigTask,
            new Action<JReleaserConfigTask>() {
                @Override
                void execute(JReleaserConfigTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn(ASSEMBLE_DIST_TASK_NAME)
                    }
                }
            })

        project.tasks.named(JReleaserTemplateGenerateTask.NAME, JReleaserTemplateGenerateTask,
            new Action<JReleaserTemplateGenerateTask>() {
                @Override
                void execute(JReleaserTemplateGenerateTask t) {
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                }
            })

        project.tasks.named(JReleaserTemplateEvalTask.NAME, JReleaserTemplateEvalTask,
            new Action<JReleaserTemplateEvalTask>() {
                @Override
                void execute(JReleaserTemplateEvalTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                }
            })

        project.tasks.named(JReleaserDownloadTask.NAME, JReleaserDownloadTask,
            new Action<JReleaserDownloadTask>() {
                @Override
                void execute(JReleaserDownloadTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                }
            })

        project.tasks.named(JReleaserAssembleTask.NAME, JReleaserAssembleTask,
            new Action<JReleaserAssembleTask>() {
                @Override
                void execute(JReleaserAssembleTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                }
            })

        project.tasks.named(JReleaserChangelogTask.NAME, JReleaserChangelogTask,
            new Action<JReleaserChangelogTask>() {
                @Override
                void execute(JReleaserChangelogTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                }
            })

        project.tasks.named(JReleaserChecksumTask.NAME, JReleaserChecksumTask,
            new Action<JReleaserChecksumTask>() {
                @Override
                void execute(JReleaserChecksumTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn(ASSEMBLE_DIST_TASK_NAME)
                    }
                }
            })

        project.tasks.named(JReleaserSignTask.NAME, JReleaserSignTask,
            new Action<JReleaserSignTask>() {
                @Override
                void execute(JReleaserSignTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn(ASSEMBLE_DIST_TASK_NAME)
                    }
                }
            })

        project.tasks.named(JReleaserDeployTask.NAME, JReleaserDeployTask,
            new Action<JReleaserDeployTask>() {
                @Override
                void execute(JReleaserDeployTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                }
            })

        project.tasks.named(JReleaserCatalogTask.NAME, JReleaserCatalogTask,
            new Action<JReleaserCatalogTask>() {
                @Override
                void execute(JReleaserCatalogTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                }
            })

        project.tasks.named(JReleaserUploadTask.NAME, JReleaserUploadTask,
            new Action<JReleaserUploadTask>() {
                @Override
                void execute(JReleaserUploadTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn(ASSEMBLE_DIST_TASK_NAME)
                    }
                }
            })

        project.tasks.named(JReleaserReleaseTask.NAME, JReleaserReleaseTask,
            new Action<JReleaserReleaseTask>() {
                @Override
                void execute(JReleaserReleaseTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn(ASSEMBLE_DIST_TASK_NAME)
                    }
                }
            })

        project.tasks.named(JReleaseAutoConfigReleaseTask.NAME, JReleaseAutoConfigReleaseTask,
            new Action<JReleaseAutoConfigReleaseTask>() {
                @Override
                void execute(JReleaseAutoConfigReleaseTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                }
            })

        project.tasks.named(JReleaserPrepareTask.NAME, JReleaserPrepareTask,
            new Action<JReleaserPrepareTask>() {
                @Override
                void execute(JReleaserPrepareTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn(ASSEMBLE_DIST_TASK_NAME)
                    }
                }
            })

        project.tasks.named(JReleaserPackageTask.NAME, JReleaserPackageTask,
            new Action<JReleaserPackageTask>() {
                @Override
                void execute(JReleaserPackageTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn(ASSEMBLE_DIST_TASK_NAME)
                    }
                }
            })

        project.tasks.named(JReleaserPublishTask.NAME, JReleaserPublishTask,
            new Action<JReleaserPublishTask>() {
                @Override
                void execute(JReleaserPublishTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn(ASSEMBLE_DIST_TASK_NAME)
                    }
                }
            })

        project.tasks.named(JReleaserAnnounceTask.NAME, JReleaserAnnounceTask,
            new Action<JReleaserAnnounceTask>() {
                @Override
                void execute(JReleaserAnnounceTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn(ASSEMBLE_DIST_TASK_NAME)
                    }
                }
            })

        project.tasks.named(JReleaserFullReleaseTask.NAME, JReleaserFullReleaseTask,
            new Action<JReleaserFullReleaseTask>() {
                @Override
                void execute(JReleaserFullReleaseTask t) {
                    t.outputDirectory.set(outputDirectory)
                    t.dryrun.set(extension.dryrun)
                    t.gitRootSearch.set(extension.gitRootSearch)
                    t.strict.set(extension.strict)
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                    if (hasDistributionPlugin) {
                        t.dependsOn(ASSEMBLE_DIST_TASK_NAME)
                    }
                }
            })

        project.tasks.named(JReleaserInitTask.NAME, JReleaserInitTask,
            new Action<JReleaserInitTask>() {
                @Override
                void execute(JReleaserInitTask t) {
                    t.jlogger.set(loggerProvider)
                    t.usesService(loggerProvider)
                }
            })
    }

    private static boolean configureDefaultDistribution(Project project, JReleaserExtensionImpl extension) {
        return project.plugins.findPlugin('distribution')
    }

    static void configureModel(Project project, JReleaserModel model) {
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

        if (isBlank(model.project.languages.java.version)) model.project.languages.java.version = javaVersion
        if (isBlank(model.project.languages.java.artifactId)) model.project.languages.java.artifactId = project.name
        if (isBlank(model.project.languages.java.groupId)) model.project.languages.java.groupId = project.group.toString()
        if (!model.project.languages.java.multiProjectSet) {
            model.project.languages.java.multiProject = !project.rootProject.childProjects.isEmpty()
        }

        if (isBlank(model.project.languages.java.mainClass)) {
            JavaApplication application = (JavaApplication) project.extensions.findByType(JavaApplication)
            if (application) {
                model.project.languages.java.mainClass = application.mainClass.orNull
                model.project.languages.java.mainModule = application.mainModule.orNull
            }
        }
    }
}
