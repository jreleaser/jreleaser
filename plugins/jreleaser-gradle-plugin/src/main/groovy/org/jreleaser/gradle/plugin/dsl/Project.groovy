/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.jreleaser.model.VersionPattern

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Project extends ExtraProperties {
    Property<String> getName()

    Property<String> getVersion()

    Property<VersionPattern> getVersionPattern()

    void setVersionPattern(String str)

    Property<String> getSnapshotPattern()

    Property<String> getDescription()

    Property<String> getLongDescription()

    Property<String> getWebsite()

    Property<String> getLicense()

    Property<String> getCopyright()

    Property<String> getVendor()

    Property<String> getDocsUrl()

    ListProperty<String> getAuthors()

    ListProperty<String> getTags()

    void addAuthor(String name)

    void addTag(String tag)

    Java getJava()

    void java(Action<? super Java> action)

    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Java) Closure<Void> action)
}