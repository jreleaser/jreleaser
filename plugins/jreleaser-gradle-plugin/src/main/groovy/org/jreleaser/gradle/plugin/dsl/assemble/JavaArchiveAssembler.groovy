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
import org.jreleaser.gradle.plugin.dsl.catalog.swid.SwidTagAware
import org.jreleaser.gradle.plugin.dsl.common.ArchiveOptions
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.dsl.common.EnvironmentVariables
import org.jreleaser.gradle.plugin.dsl.common.Executable
import org.jreleaser.gradle.plugin.dsl.common.Glob
import org.jreleaser.gradle.plugin.dsl.common.JvmOptions
import org.jreleaser.model.Archive.Format

/**
 *
 * @author Andres Almiray
 * @since 1.4.0
 */
@CompileStatic
interface JavaArchiveAssembler extends Assembler, SwidTagAware {
    Property<String> getArchiveName()

    SetProperty<Format> getFormats()

    void format(String format)

    Java getJava()

    Executable getExecutable()

    void java(Action<? super Java> action)

    void mainJar(Action<? super Artifact> action)

    void executable(Action<? super Executable> action)

    void jars(Action<? super Glob> action)

    void options(Action<? super ArchiveOptions> action)

    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Java) Closure<Void> action)

    void mainJar(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    void executable(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Executable) Closure<Void> action)

    void jars(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Glob) Closure<Void> action)

    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArchiveOptions) Closure<Void> action)

    interface Java {
        Property<String> getMainModule()

        Property<String> getMainClass()

        @Deprecated
        SetProperty<String> getOptions()

        JvmOptions getJvmOptions()

        EnvironmentVariables getEnvironmentVariables()

        void jvmOptions(Action<? super JvmOptions> action)

        void environmentVariables(Action<? super EnvironmentVariables> action)

        void jvmOptions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JvmOptions) Closure<Void> action)

        void environmentVariables(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = EnvironmentVariables) Closure<Void> action)
    }
}