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
package org.jreleaser.gradle.plugin.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension
import org.kordamp.gradle.plugin.base.model.License

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class KordampJReleaserAdapter {
    static void adapt(Project project) {
        JReleaserExtension jreleaser = project.extensions.findByType(JReleaserExtension)
        ProjectConfigurationExtension config = project.extensions.findByType(ProjectConfigurationExtension)

        if (!jreleaser.project.description.present) {
            jreleaser.project.description.set(config.info.description)
        }
        if (!jreleaser.project.links.homepage.present) {
            jreleaser.project.links.homepage.set(config.info.links.website)
        }
        if (!jreleaser.project.authors.present) {
            jreleaser.project.authors.set(config.info.authors)
        }
        if (!jreleaser.project.tags.present) {
            jreleaser.project.tags.set(config.info.tags)
        }
        if (!jreleaser.project.license.present) {
            List<License> licenses = config.licensing.allLicenses()
            if (licenses.size() > 0) {
                License license = licenses.find {
                    it.primary
                } ?: licenses[0]
                jreleaser.project.license.set(license.name)
            }
        }
        if (!jreleaser.project.inceptionYear.present) {
            jreleaser.project.inceptionYear.set(config.info.inceptionYear)
        }
    }
}
