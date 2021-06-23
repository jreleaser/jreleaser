/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.jreleaser.gradle.plugin.dsl.Environment

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

    void setVariables(String variables) {
        this.variables.set(new File(variables))
    }

    org.jreleaser.model.Environment toModel(Project project) {
        org.jreleaser.model.Environment environment = new org.jreleaser.model.Environment()
        environment.variablesSource = new org.jreleaser.model.Environment.MapVariablesSource(project.properties)
        if (variables.present) environment.variables = variables.asFile.get().absolutePath
        if (properties.present) environment.properties.putAll(properties.get())
        environment
    }
}
