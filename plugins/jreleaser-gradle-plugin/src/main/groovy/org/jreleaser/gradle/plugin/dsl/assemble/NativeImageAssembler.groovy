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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.catalog.swid.SwidTagAware
import org.jreleaser.gradle.plugin.dsl.common.Activatable
import org.jreleaser.gradle.plugin.dsl.common.ArchiveOptions
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.model.Archive

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
interface NativeImageAssembler extends JavaAssembler, SwidTagAware {
    Property<String> getImageName()

    Property<String> getImageNameTransform()

    ListProperty<String> getArgs()

    SetProperty<String> getComponents()

    Property<Archive.Format> getArchiveFormat()

    void setArchiveFormat(String str)

    void arg(String arg)

    void component(String component)

    void graal(Action<? super Artifact> action)

    void upx(Action<? super Upx> action)

    void linux(Action<? super Linux> action)

    void windows(Action<? super Windows> action)

    void osx(Action<? super Osx> action)

    void graalJdk(Action<? super Artifact> action)

    void options(Action<? super ArchiveOptions> action)

    void graal(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    void upx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action)

    void linux(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Linux) Closure<Void> action)

    void windows(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Windows) Closure<Void> action)

    void osx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Osx) Closure<Void> action)

    void graalJdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArchiveOptions) Closure<Void> action)

    interface Upx extends Activatable {
        Property<String> getVersion()

        ListProperty<String> getArgs()

        void arg(String arg)
    }

    interface Linux {
        ListProperty<String> getArgs()

        void arg(String arg)
    }

    interface Windows {
        ListProperty<String> getArgs()

        void arg(String arg)
    }

    interface Osx {
        ListProperty<String> getArgs()

        void arg(String arg)
    }
}