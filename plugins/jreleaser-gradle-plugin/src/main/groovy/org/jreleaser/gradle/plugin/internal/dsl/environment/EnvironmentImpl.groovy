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
package org.jreleaser.gradle.plugin.internal.dsl.environment

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.jreleaser.gradle.plugin.dsl.environment.Environment

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class EnvironmentImpl implements Environment {
    final RegularFileProperty variables
    final MapProperty<String, Object> properties

    @Inject
    EnvironmentImpl(ObjectFactory objects) {
        variables = objects.fileProperty().convention(Providers.notDefined())
        properties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Override
    void setVariables(String variables) {
        this.variables.set(new File(variables))
    }

    org.jreleaser.model.internal.environment.Environment toModel(Project project) {
        org.jreleaser.model.internal.environment.Environment environment = new org.jreleaser.model.internal.environment.Environment()
        environment.propertiesSource = new org.jreleaser.model.internal.environment.Environment.MapPropertiesSource(
            filterProperties(project.properties))
        if (variables.present) environment.variables = variables.asFile.get().absolutePath
        if (properties.present) environment.properties.putAll(properties.get())
        environment
    }

    private Map<String, ?> filterProperties(Map<String, ?> inputs) {
        Map<String, ?> outputs = [:]

        inputs.each { key, value ->
            if (key.startsWith('systemProp') || key.startsWith('VISITED_org_kordamp_gradle')) return

            def val = value
            if (value instanceof Provider) {
                val = ((Provider) value).get()
            }

            if (value instanceof CharSequence ||
                value instanceof Number ||
                value instanceof Boolean ||
                value instanceof File) {
                outputs.put(key, val)
            }
        }

        outputs
    }
}
