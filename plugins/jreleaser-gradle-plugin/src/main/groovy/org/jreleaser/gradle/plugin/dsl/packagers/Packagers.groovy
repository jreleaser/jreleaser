/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.dsl.packagers

import groovy.transform.CompileStatic
import org.gradle.api.Action

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Packagers {
    AppImagePackager getAppImage()

    AsdfPackager getAsdf()

    BrewPackager getBrew()

    ChocolateyPackager getChocolatey()

    DockerPackager getDocker()

    FlatpakPackager getFlatpak()

    GofishPackager getGofish()

    JbangPackager getJbang()

    JibPackager getJib()

    MacportsPackager getMacports()

    ScoopPackager getScoop()

    SdkmanPackager getSdkman()

    SnapPackager getSnap()

    SpecPackager getSpec()

    WingetPackager getWinget()

    void appImage(Action<? super AppImagePackager> action)

    void asdf(Action<? super AsdfPackager> action)

    void brew(Action<? super BrewPackager> action)

    void chocolatey(Action<? super ChocolateyPackager> action)

    void docker(Action<? super DockerPackager> action)

    void flatpak(Action<? super FlatpakPackager> action)

    void gofish(Action<? super GofishPackager> action)

    void jbang(Action<? super JbangPackager> action)

    void jib(Action<? super JibPackager> action)

    void macports(Action<? super MacportsPackager> action)

    void scoop(Action<? super ScoopPackager> action)

    void sdkman(Action<? super SdkmanPackager> action)

    void snap(Action<? super SnapPackager> action)

    void spec(Action<? super SpecPackager> action)

    void winget(Action<? super WingetPackager> action)
}