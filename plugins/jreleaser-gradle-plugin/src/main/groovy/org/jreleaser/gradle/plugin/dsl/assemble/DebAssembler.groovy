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
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 *
 * @author Andres Almiray
 * @since 1.16.0
 */
@CompileStatic
interface DebAssembler extends Assembler {
    Property<String> getExecutable()

    Property<String> getInstallationPath()

    Property<String> getArchitecture()

    Property<String> getAssemblerRef()

    Control getControl()

    void control(Action<? super Control> action)

    void control(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Control) Closure<Void> action)

    @CompileStatic
    interface Control {
        Property<String> getPackageName()

        Property<String> getPackageVersion()

        Property<Integer> getPackageRevision()

        Property<String> getProvides()

        Property<String> getMaintainer()

        Property<org.jreleaser.model.api.assemble.DebAssembler.Section> getSection()

        Property<org.jreleaser.model.api.assemble.DebAssembler.Priority> getPriority()

        Property<Boolean> getEssential()

        Property<String> getDescription()

        Property<String> getHomepage()

        Property<String> getBuiltUsing()

        SetProperty<String> getDepends()

        SetProperty<String> getPreDepends()

        SetProperty<String> getRecommends()

        SetProperty<String> getSuggests()

        SetProperty<String> getEnhances()

        SetProperty<String> getBreaks()

        SetProperty<String> getConflicts()

        void setSection(String section)

        void setPriority(String priority)
    }
}