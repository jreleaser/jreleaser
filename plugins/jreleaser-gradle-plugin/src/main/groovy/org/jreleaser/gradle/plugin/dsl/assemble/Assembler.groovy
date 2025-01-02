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
package org.jreleaser.gradle.plugin.dsl.assemble

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.catalog.swid.SwidTag
import org.jreleaser.gradle.plugin.dsl.common.Activatable
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.dsl.common.ExtraProperties
import org.jreleaser.gradle.plugin.dsl.common.FileSet
import org.jreleaser.gradle.plugin.dsl.common.Glob
import org.jreleaser.gradle.plugin.dsl.platform.Platform
import org.jreleaser.model.Stereotype

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
interface Assembler extends Activatable, ExtraProperties {
    Property<Boolean> getExported()

    Property<Stereotype> getStereotype()

    DirectoryProperty getTemplateDirectory()

    void setTemplateDirectory(String templateDirectory)

    SetProperty<String> getSkipTemplates()

    void skipTemplate(String template)

    Platform getPlatform()

    void setStereotype(String str)

    void artifact(Action<? super Artifact> action)

    void files(Action<? super Glob> action)

    void fileSet(Action<? super FileSet> action)

    void platform(Action<? super Platform> action)

    void artifact(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    void files(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Glob) Closure<Void> action)

    void fileSet(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = FileSet) Closure<Void> action)

    void platform(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Platform) Closure<Void> action)
}