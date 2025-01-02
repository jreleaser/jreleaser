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
package org.jreleaser.gradle.plugin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Provider
import org.jreleaser.gradle.plugin.internal.JReleaserExtensionImpl
import org.jreleaser.gradle.plugin.internal.JReleaserLoggerAdapter
import org.jreleaser.gradle.plugin.internal.JReleaserProjectConfigurer
import org.jreleaser.gradle.plugin.tasks.JReleaseAutoConfigReleaseTask
import org.jreleaser.gradle.plugin.tasks.JReleaserAnnounceTask
import org.jreleaser.gradle.plugin.tasks.JReleaserAssembleTask
import org.jreleaser.gradle.plugin.tasks.JReleaserCatalogTask
import org.jreleaser.gradle.plugin.tasks.JReleaserChangelogTask
import org.jreleaser.gradle.plugin.tasks.JReleaserChecksumTask
import org.jreleaser.gradle.plugin.tasks.JReleaserConfigTask
import org.jreleaser.gradle.plugin.tasks.JReleaserDeployTask
import org.jreleaser.gradle.plugin.tasks.JReleaserDownloadTask
import org.jreleaser.gradle.plugin.tasks.JReleaserEnvTask
import org.jreleaser.gradle.plugin.tasks.JReleaserFullReleaseTask
import org.jreleaser.gradle.plugin.tasks.JReleaserInitTask
import org.jreleaser.gradle.plugin.tasks.JReleaserJsonSchemaTask
import org.jreleaser.gradle.plugin.tasks.JReleaserPackageTask
import org.jreleaser.gradle.plugin.tasks.JReleaserPrepareTask
import org.jreleaser.gradle.plugin.tasks.JReleaserPublishTask
import org.jreleaser.gradle.plugin.tasks.JReleaserReleaseTask
import org.jreleaser.gradle.plugin.tasks.JReleaserSignTask
import org.jreleaser.gradle.plugin.tasks.JReleaserTemplateEvalTask
import org.jreleaser.gradle.plugin.tasks.JReleaserTemplateGenerateTask
import org.jreleaser.gradle.plugin.tasks.JReleaserUploadTask
import org.kordamp.gradle.util.AnsiConsole

import static org.jreleaser.model.JReleaserOutput.JRELEASER_QUIET
import static org.jreleaser.util.IoUtils.newPrintWriter

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JReleaserPlugin implements Plugin<Project> {
    private static final String JRELEASER_GROUP = 'JReleaser'

    @Override
    void apply(Project project) {
        if (project.gradle.startParameter.logLevel != LogLevel.QUIET) {
            project.gradle.sharedServices
                .registerIfAbsent('jreleaser-banner', Banner, { spec -> })
                .get().display(project)
        } else {
            System.setProperty(JRELEASER_QUIET, 'true')
        }

        Provider<String> nameProvider = project.provider({ -> project.name })
        Provider<String> descriptionProvider = project.provider({ -> project.description })
        Provider<String> versionProvider = project.provider({ -> String.valueOf(project.version) })
        project.extensions.create(JReleaserExtension, 'jreleaser', JReleaserExtensionImpl,
            project.objects, project.layout, nameProvider, descriptionProvider, versionProvider)

        registerTasks(project)

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project p) {
                JReleaserExtension extension = project.extensions.findByType(JReleaserExtension)
                if (!extension.enabled.get()) return

                if (hasKordampBasePluginApplied(p)) {
                    registerAllProjectsEvaluatedListener(p)
                } else {
                    configureJReleaser(p)
                }
            }
        })
    }

    private void configureJReleaser(Project project) {
        JReleaserProjectConfigurer.configure(project)
    }

    private boolean hasKordampBasePluginApplied(Project project) {
        project.rootProject.plugins.findPlugin('org.kordamp.gradle.base')
    }

    @CompileDynamic
    private void registerAllProjectsEvaluatedListener(Project project) {
        Class c = Class.forName('org.jreleaser.gradle.plugin.internal.JReleaserAllProjectsEvaluatedListener')
        def listener = c.getConstructor().newInstance()
        listener.runnable = { ->
            Class.forName('org.jreleaser.gradle.plugin.internal.KordampJReleaserAdapter')
                .adapt(project)
            configureJReleaser(project)
        }

        Class m = Class.forName('org.kordamp.gradle.listener.ProjectEvaluationListenerManager')
        m.addAllProjectsEvaluatedListener(project, listener)
    }

    private void registerTasks(Project project) {
        project.tasks.register(JReleaserEnvTask.NAME, JReleaserEnvTask,
            new Action<JReleaserEnvTask>() {
                @Override
                void execute(JReleaserEnvTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Display environment variable names'
                    t.basedir.set(project.layout.projectDirectory)
                    t.jlogger.set(new JReleaserLoggerAdapter(new AnsiConsole(project, 'JRELEASER'), LogLevel.INFO,
                        newPrintWriter(new ByteArrayOutputStream())))
                }
            })

        project.tasks.register(JReleaserConfigTask.NAME, JReleaserConfigTask,
            new Action<JReleaserConfigTask>() {
                @Override
                void execute(JReleaserConfigTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Outputs current JReleaser configuration'
                }
            })

        project.tasks.register(JReleaserTemplateGenerateTask.NAME, JReleaserTemplateGenerateTask,
            new Action<JReleaserTemplateGenerateTask>() {
                @Override
                void execute(JReleaserTemplateGenerateTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Generates templates for a specific packager/announcer'
                    t.outputDirectory.set(project.layout
                        .projectDirectory
                        .dir('src/jreleaser'))
                }
            })

        project.tasks.register(JReleaserTemplateEvalTask.NAME, JReleaserTemplateEvalTask,
            new Action<JReleaserTemplateEvalTask>() {
                @Override
                void execute(JReleaserTemplateEvalTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Evaluate a template or templates'
                }
            })

        project.tasks.register(JReleaserDownloadTask.NAME, JReleaserDownloadTask,
            new Action<JReleaserDownloadTask>() {
                @Override
                void execute(JReleaserDownloadTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Downloads all artifacts'
                }
            })

        project.tasks.register(JReleaserAssembleTask.NAME, JReleaserAssembleTask,
            new Action<JReleaserAssembleTask>() {
                @Override
                void execute(JReleaserAssembleTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Assemble all distributions'
                }
            })

        project.tasks.register(JReleaserChangelogTask.NAME, JReleaserChangelogTask,
            new Action<JReleaserChangelogTask>() {
                @Override
                void execute(JReleaserChangelogTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Calculate changelogs'
                }
            })

        project.tasks.register(JReleaserChecksumTask.NAME, JReleaserChecksumTask,
            new Action<JReleaserChecksumTask>() {
                @Override
                void execute(JReleaserChecksumTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Calculate checksums'
                }
            })

        project.tasks.register(JReleaserSignTask.NAME, JReleaserSignTask,
            new Action<JReleaserSignTask>() {
                @Override
                void execute(JReleaserSignTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Signs a release'
                }
            })

        project.tasks.register(JReleaserDeployTask.NAME, JReleaserDeployTask,
            new Action<JReleaserDeployTask>() {
                @Override
                void execute(JReleaserDeployTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Deploys all artifacts'
                }
            })

        project.tasks.register(JReleaserCatalogTask.NAME, JReleaserCatalogTask,
            new Action<JReleaserCatalogTask>() {
                @Override
                void execute(JReleaserCatalogTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Catalogs all artifacts and files'
                }
            })

        project.tasks.register(JReleaserUploadTask.NAME, JReleaserUploadTask,
            new Action<JReleaserUploadTask>() {
                @Override
                void execute(JReleaserUploadTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Uploads all artifacts'
                }
            })

        project.tasks.register(JReleaserReleaseTask.NAME, JReleaserReleaseTask,
            new Action<JReleaserReleaseTask>() {
                @Override
                void execute(JReleaserReleaseTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Creates or updates a release'
                }
            })

        project.tasks.register(JReleaseAutoConfigReleaseTask.NAME, JReleaseAutoConfigReleaseTask,
            new Action<JReleaseAutoConfigReleaseTask>() {
                @Override
                void execute(JReleaseAutoConfigReleaseTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Creates or updates a release with auto-config enabled'
                }
            })

        project.tasks.register(JReleaserPrepareTask.NAME, JReleaserPrepareTask,
            new Action<JReleaserPrepareTask>() {
                @Override
                void execute(JReleaserPrepareTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Prepares all distributions'
                }
            })

        project.tasks.register(JReleaserPackageTask.NAME, JReleaserPackageTask,
            new Action<JReleaserPackageTask>() {
                @Override
                void execute(JReleaserPackageTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Packages all distributions'
                }
            })

        project.tasks.register(JReleaserPublishTask.NAME, JReleaserPublishTask,
            new Action<JReleaserPublishTask>() {
                @Override
                void execute(JReleaserPublishTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Publishes all distributions'
                }
            })

        project.tasks.register(JReleaserAnnounceTask.NAME, JReleaserAnnounceTask,
            new Action<JReleaserAnnounceTask>() {
                @Override
                void execute(JReleaserAnnounceTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Announces a release'
                }
            })

        project.tasks.register(JReleaserFullReleaseTask.NAME, JReleaserFullReleaseTask,
            new Action<JReleaserFullReleaseTask>() {
                @Override
                void execute(JReleaserFullReleaseTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Invokes release, publish, and announce'
                }
            })

        project.tasks.register(JReleaserInitTask.NAME, JReleaserInitTask,
            new Action<JReleaserInitTask>() {
                @Override
                void execute(JReleaserInitTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Create a jreleaser config file'
                    t.outputDirectory.set(project.layout.projectDirectory)
                }
            })

        project.tasks.register(JReleaserJsonSchemaTask.NAME, JReleaserJsonSchemaTask,
            new Action<JReleaserJsonSchemaTask>() {
                @Override
                void execute(JReleaserJsonSchemaTask t) {
                    t.group = JRELEASER_GROUP
                    t.description = 'Generate JSON schema'
                }
            })
    }
}
