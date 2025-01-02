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
package org.jreleaser.gradle.plugin.dsl.catalog.swid

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Activatable

/**
 *
 * @author Andres Almiray
 * @since 1.11.0
 */
@CompileStatic
interface SwidTag extends Activatable {
    Property<String> getTagRef()

    Property<String> getPath()

    Property<String> getTagId()

    Property<Integer> getTagVersion()

    Property<String> getLang()

    Property<Boolean> getCorpus()

    Property<Boolean> getPatch()

    void entity(Action<? super Entity> action)

    void entity(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Entity) Closure<Void> action)
}