/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Packagers {
    AppImage getAppImage()

    Asdf getAsdf()

    Brew getBrew()

    Chocolatey getChocolatey()

    Docker getDocker()

    Gofish getGofish()

    Jbang getJbang()

    Macports getMacports()

    Scoop getScoop()

    Sdkman getSdkman()

    Snap getSnap()

    Spec getSpec()

    void appImage(Action<? super AppImage> action)

    void asdf(Action<? super Asdf> action)

    void brew(Action<? super Brew> action)

    void chocolatey(Action<? super Chocolatey> action)

    void docker(Action<? super Docker> action)

    void gofish(Action<? super Gofish> action)

    void jbang(Action<? super Jbang> action)

    void macports(Action<? super Macports> action)

    void scoop(Action<? super Scoop> action)

    void sdkman(Action<? super Sdkman> action)

    void snap(Action<? super Snap> action)

    void spec(Action<? super Spec> action)

    void appImage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = AppImage) Closure<Void> action)

    void asdf(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Asdf) Closure<Void> action)

    void brew(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Brew) Closure<Void> action)

    void chocolatey(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Chocolatey) Closure<Void> action)

    void docker(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Docker) Closure<Void> action)

    void gofish(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Gofish) Closure<Void> action)

    void jbang(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jbang) Closure<Void> action)

    void macports(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Macports) Closure<Void> action)

    void scoop(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Scoop) Closure<Void> action)

    void sdkman(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Sdkman) Closure<Void> action)

    void snap(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Snap) Closure<Void> action)

    void spec(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Spec) Closure<Void> action)
}