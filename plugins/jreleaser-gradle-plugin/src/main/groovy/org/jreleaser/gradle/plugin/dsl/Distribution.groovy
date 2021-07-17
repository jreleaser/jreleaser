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
package org.jreleaser.gradle.plugin.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.jreleaser.model.Active
import org.jreleaser.model.Distribution.DistributionType

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Distribution extends ExtraProperties {
    Property<Active> getActive()

    void setActive(String str)

    Property<DistributionType> getDistributionType()

    Property<String> getExecutable()

    ListProperty<String> getTags()

    void setDistributionType(String distributionType)

    void addTag(String tag)

    Brew getBrew()

    Chocolatey getChocolatey()

    Docker getDocker()

    Jbang getJbang()

    Scoop getScoop()

    Sdkman getSdkman()

    Snap getSnap()

    Java getJava()

    void artifact(Action<? super Artifact> action)

    void java(Action<? super Java> action)

    void brew(Action<? super Brew> action)

    void chocolatey(Action<? super Chocolatey> action)

    void docker(Action<? super Docker> action)

    void jbang(Action<? super Jbang> action)

    void scoop(Action<? super Scoop> action)

    void sdkman(Action<? super Sdkman> action)

    void snap(Action<? super Snap> action)

    void artifact(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Java) Closure<Void> action)

    void brew(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Brew) Closure<Void> action)

    void chocolatey(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Chocolatey) Closure<Void> action)

    void docker(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Docker) Closure<Void> action)

    void jbang(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jbang) Closure<Void> action)

    void scoop(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Scoop) Closure<Void> action)

    void sdkman(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Sdkman) Closure<Void> action)

    void snap(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Snap) Closure<Void> action)
}