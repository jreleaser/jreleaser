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
package org.jreleaser.gradle.plugin.internal.dsl.project


import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.jreleaser.gradle.plugin.dsl.common.Java
import org.jreleaser.gradle.plugin.dsl.project.Languages
import org.jreleaser.gradle.plugin.internal.dsl.common.JavaImpl

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.16.0
 */
@CompileStatic
class LanguagesImpl implements Languages {
    final JavaImpl java

    @Inject
    LanguagesImpl(ObjectFactory objects) {
        java = objects.newInstance(JavaImpl, objects)
    }

    @Override
    void java(Action<? super Java> action) {
        action.execute(java)
    }

    org.jreleaser.model.internal.project.Languages toModel() {
        org.jreleaser.model.internal.project.Languages languages = new org.jreleaser.model.internal.project.Languages()
        languages.java = java.toModel()
        languages
    }
}
