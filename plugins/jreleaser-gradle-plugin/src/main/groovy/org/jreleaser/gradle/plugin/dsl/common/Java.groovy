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
package org.jreleaser.gradle.plugin.dsl.common

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Java extends ExtraProperties {
    Property<String> getVersion()

    Property<String> getGroupId()

    Property<String> getArtifactId()

    Property<String> getMainModule()

    Property<String> getMainClass()

    Property<Boolean> getMultiProject()

    @Deprecated
    SetProperty<String> getOptions()

    JvmOptions getJvmOptions()

    EnvironmentVariables getEnvironmentVariables()

    void jvmOptions(Action<? super JvmOptions> action)

    void environmentVariables(Action<? super EnvironmentVariables> action)

    void jvmOptions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JvmOptions) Closure<Void> action)

    void environmentVariables(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = EnvironmentVariables) Closure<Void> action)
}