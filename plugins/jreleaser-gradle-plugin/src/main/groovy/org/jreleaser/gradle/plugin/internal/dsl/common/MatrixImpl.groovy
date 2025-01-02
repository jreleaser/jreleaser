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
package org.jreleaser.gradle.plugin.internal.dsl.common

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.Matrix

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.16.0
 */
@CompileStatic
class MatrixImpl implements Matrix {
    final MapProperty<String, List<String>> vars
    final ListProperty<Map<String, String>> rows

    @Inject
    @CompileDynamic
    MatrixImpl(ObjectFactory objects) {
        vars = objects.mapProperty(String, List<String>).convention(Providers.notDefined())
        rows = objects.listProperty(Map<String, String>).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        vars.present || rows.present
    }

    @Override
    void variable(String key, List<String> values) {
        if (isNotBlank(key) && values) {
            vars.put(key.trim(), values)
        }
    }

    @Override
    void row(Map<String, String> values) {
        if (values) rows.add(values)
    }

    org.jreleaser.model.internal.common.Matrix toModel() {
        org.jreleaser.model.internal.common.Matrix matrix = new org.jreleaser.model.internal.common.Matrix()
        matrix.setVars(vars.getOrElse([:]))
        matrix.setRows(rows.getOrElse([]))
        matrix
    }
}
