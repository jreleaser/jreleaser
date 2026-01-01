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
import org.jreleaser.gradle.plugin.dsl.common.Matrix
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

    @Deprecated
    Property<Archive.Format> getArchiveFormat()

    Property<Boolean> getApplyDefaultMatrix()

    void setArchiveFormat(String str)

    void arg(String arg)

    void component(String component)

    void graal(Action<? super Artifact> action)

    void upx(Action<? super Upx> action)

    void archiving(Action<? super Archiving> action)

    @Deprecated
    void linux(Action<? super LinuxX86> action)

    @Deprecated
    void windows(Action<? super WindowsX86> action)

    @Deprecated
    void osx(Action<? super MacosX86> action)

    void linuxX86(Action<? super LinuxX86> action)

    void windowsX86(Action<? super WindowsX86> action)

    void macosX86(Action<? super MacosX86> action)

    void linuxArm(Action<? super LinuxArm> action)

    void macosArm(Action<? super MacosArm> action)

    void graalJdk(Action<? super Artifact> action)

    void matrix(Action<? super Matrix> action)

    void graalJdkPattern(Action<? super Artifact> action)

    void options(Action<? super ArchiveOptions> action)

    interface Archiving {
        Property<Boolean> getEnabled()

        Property<Archive.Format> getFormat()

        void setFormat(String str)
    }

    interface Upx extends Activatable {
        Property<String> getVersion()

        ListProperty<String> getArgs()

        void arg(String arg)
    }

    interface PlatformCustomizer {
        ListProperty<String> getArgs()

        void arg(String arg)
    }

    @Deprecated
    interface Linux extends PlatformCustomizer {
    }

    @Deprecated
    interface Windows extends PlatformCustomizer {
    }

    @Deprecated
    interface Osx extends PlatformCustomizer {
    }

    interface LinuxX86 extends Linux {
    }

    interface WindowsX86 extends Windows {
    }

    interface MacosX86 extends Osx {
    }

    interface LinuxArm extends PlatformCustomizer {
    }

    interface MacosArm extends PlatformCustomizer {
    }
}