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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
interface NativeImage extends JavaAssembler {
    Property<String> getImageName()

    Property<String> getImageNameTransform()

    ListProperty<String> getArgs()

    Property<org.jreleaser.model.Archive.Format> getArchiveFormat()

    void setArchiveFormat(String str)

    void addArg(String arg)

    void graal(Action<? super Artifact> action)

    void upx(Action<? super Upx> action)

    void graalJdk(Action<? super Artifact> action)

    void graal(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    void upx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action)

    void graalJdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    interface Upx extends Activatable {
        Property<String> getVersion()

        ListProperty<String> getArgs()

        void addArg(String arg)
    }
}