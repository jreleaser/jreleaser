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
package org.jreleaser.gradle.plugin.dsl.files

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.jreleaser.gradle.plugin.dsl.common.Activatable
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.dsl.common.Glob

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Files extends Activatable {
    void artifact(Action<? super Artifact> action)

    void glob(Action<? super Glob> action)

    void artifact(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    void glob(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Glob) Closure<Void> action)
}