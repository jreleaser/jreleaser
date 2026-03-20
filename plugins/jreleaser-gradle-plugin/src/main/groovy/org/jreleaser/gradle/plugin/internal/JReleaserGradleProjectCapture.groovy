/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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

import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.provider.Provider
import org.jreleaser.logging.JReleaserLogger

/**
 * Captures relevant information from a Gradle Project
 * to be used in JReleaser plugin configuration and tasks.
 *
 * @author Markus Hoffrogge
 * @since 1.24.0
 */
class JReleaserGradleProjectCapture implements Serializable {
    final String group
    final String version
    final String name
    final String description
    final boolean multiProject
    final String javaApplicationMainClass
    final String javaApplicationMainModule

    private final Map<String, Serializable> properties

    // List of Gradle Project properties that are deprecated in Gradle 9
    // and should be ignored to avoid deprecation warnings.
    // Refer to deprecated Convention APIs:
    //   org.gradle.api.plugins.internal.NaggingJavaPluginConvention
    //   org.gradle.api.plugins.BasePluginConvention
    //   org.gradle.api.plugins.JavaPluginConvention
    private static final List<String> GRADLE_DEPRECATED_PROJECT_PROPS = [
        'archivesBaseName',
        'archivesName',
        'autoTargetJvmDisabled',
        'convention',
        'distsDirectory',
        'distsDirName',
        'docsDir',
        'docsDirName',
        'libsDirectory',
        'libsDirName',
        'manifest',
        'project',
        'reportsDir',
        'sourceSets',
        'sourceCompatibility',
        'targetCompatibility',
        'testReportDir',
        'testReportDirName',
        'testResultsDir',
        'testResultsDirName'
    ]

    static JReleaserGradleProjectCapture of(Project gradleProject, JReleaserLogger logger) {
        return new JReleaserGradleProjectCapture(gradleProject, logger)
    }

    private JReleaserGradleProjectCapture(Project gradleProject, JReleaserLogger logger) {
        this.name = gradleProject.name
        this.group = gradleProject.group?.toString()
        this.version = gradleProject.version?.toString()
        this.description = gradleProject.description
        this.properties = captureGradleProjectSerializableProperties(gradleProject, logger)
        this.multiProject = !gradleProject.rootProject.childProjects.isEmpty()
        JavaApplication javaApplication = (JavaApplication) gradleProject.extensions.findByType(JavaApplication)
        this.javaApplicationMainClass = javaApplication?.mainClass?.orNull
        this.javaApplicationMainModule = javaApplication?.mainModule?.orNull
    }

    private static Map <String, Serializable> captureGradleProjectSerializableProperties(
        Project gradleProject, JReleaserLogger logger) {
        Set<String> propertyKeys = collectGradleProjectPropertyKeys(gradleProject)
        Map <String, Serializable> projectProperties = [:]
        propertyKeys.each { key ->
            // Avoid useless Gradle deprecation log
            if (GRADLE_DEPRECATED_PROJECT_PROPS.contains(key)) {
                logger.debug("GradleProjectCapture: IGNORE deprecated Gradle project property '${key}'.")
                return // continue
            }
            Object value = gradleProject.findProperty(key)
            if (value instanceof Provider) {
                Provider p = (Provider) value
                value = p.present ? p.get() : null
            }
            if (value instanceof File) {
                value = ((File) value).absolutePath
            }
            if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
                logger.debug("GradleProjectCapture: CAPTURE project property '${key}'.")
                projectProperties.put(key, value instanceof Serializable ? (Serializable) value : value.toString())
            } else if (value) {
                logger.debug("GradleProjectCapture: SKIP complex project property '${key}' of type '${value.getClass().name}'.")
            } else {
                logger.debug("GradleProjectCapture: SKIP null or empty project property '${key}'.")
            }
        }
        return projectProperties
    }

    private static Set<String> collectGradleProjectPropertyKeys(Project gradleProject) {
        Set<String> propertyKeys = gradleProject.properties.keySet()
        ExtraPropertiesExtension extraPropertiesExtension = gradleProject.extensions.extraProperties
        Set<String> extraPropertyKeys = extraPropertiesExtension?.properties?.keySet()
        if (extraPropertyKeys) {
            propertyKeys += extraPropertyKeys
            propertyKeys = propertyKeys.toSet()
        }
        return propertyKeys
    }

    Map<String, Serializable> getProperties() {
        return properties.asImmutable()
    }

    Serializable findProperty(String propertyName) {
        return hasProperty(propertyName) ? property(propertyName) : null;
    }

    boolean hasProperty(String propertyName) {
        return properties.containsKey(propertyName)
    }

    Serializable property(String propertyName) {
        return properties[propertyName]
    }
}
