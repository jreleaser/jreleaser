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
package org.kordamp.jreleaser.gradle.plugin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.kordamp.jreleaser.gradle.plugin.internal.JReleaserExtensionImpl
import org.kordamp.jreleaser.gradle.plugin.internal.JReleaserProjectConfigurer

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JReleaserPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        Banner.display(project)

        Provider<String> nameProvider = project.provider({ -> project.name })
        Provider<String> descriptionProvider = project.provider({ -> project.description })
        Provider<String> versionProvider = project.provider({ -> String.valueOf(project.version) })
        Provider<Directory> distributionsDirProvider = project.provider({ ->
            project.layout.projectDirectory.dir('src/distributions')
        })
        project.extensions.create(JReleaserExtension, 'jreleaser', JReleaserExtensionImpl,
            project.objects, nameProvider, descriptionProvider, versionProvider, distributionsDirProvider)

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
        Class c = Class.forName('org.kordamp.jreleaser.gradle.plugin.internal.JReleaserAllProjectsEvaluatedListener')
        def listener = c.getConstructor().newInstance()
        listener.runnable = { ->
            Class.forName('org.kordamp.jreleaser.gradle.plugin.internal.KordampJReleaserAdapter')
                .adapt(project)
            configureJReleaser(project)
        }

        Class m = Class.forName('org.kordamp.gradle.listener.ProjectEvaluationListenerManager')
        m.addAllProjectsEvaluatedListener(project, listener)
    }
}
