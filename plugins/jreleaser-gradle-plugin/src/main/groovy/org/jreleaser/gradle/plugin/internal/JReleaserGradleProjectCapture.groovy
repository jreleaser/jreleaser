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
    final boolean isMultiProject;
    final String javaApplicationMainClass
    final String javaApplicationMainModule

    private final Map<String, Object> properties

    static JReleaserGradleProjectCapture of(Project gradleProject) {
        return new JReleaserGradleProjectCapture(gradleProject)
    }

    private JReleaserGradleProjectCapture(Project gradleProject) {
        this.name = gradleProject.name
        this.group = gradleProject.group?.toString()
        this.version = gradleProject.version?.toString()
        this.description = gradleProject.description
        this.properties = captureGradleProjectSerializableProperties(gradleProject)
        this.isMultiProject = !gradleProject.rootProject.childProjects.isEmpty()
        JavaApplication javaApplication = (JavaApplication) gradleProject.extensions.findByType(JavaApplication)
        this.javaApplicationMainClass = javaApplication?.mainClass?.orNull
        this.javaApplicationMainModule = javaApplication?.mainModule?.orNull
    }

    private static Map <String, Object> captureGradleProjectSerializableProperties(Project gradleProject) {
        Set<String> propertyKeys = collectGradleProjectPropertyKeys(gradleProject)
        Map <String, Object> projectProperties = [:]
        propertyKeys.each { key ->
            Object value = gradleProject.findProperty(key)
            if (value instanceof Provider) {
                Provider p = (Provider) value
                value = p.present ? p.get() : null
            }
            if (value instanceof File) {
                projectProperties.put(key, ((File) value).absolutePath)
            } else if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
                projectProperties.put(key, value)
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

    Map<String, Object> getProperties() {
        return properties.asImmutable()
    }

    Object findProperty(String propertyName) {
        return hasProperty(propertyName) ? property(propertyName) : null;
    }

    boolean hasProperty(String propertyName) {
        return properties.containsKey(propertyName)
    }

    Object property(String propertyName) {
        return properties[propertyName]
    }

}
