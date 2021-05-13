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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.model.Active

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Changelog {
    Property<org.jreleaser.model.Changelog.Sort> getSort()

    Property<Boolean> getEnabled()

    Property<Boolean> getLinks()

    Property<Boolean> getHideUncategorized()

    RegularFileProperty getExternal()

    void setSort(String sort)

    Property<Active> getFormatted()

    void setFormatted(String str)

    Property<String> getChange()

    Property<String> getContent()

    RegularFileProperty getContentTemplate()

    SetProperty<String> getIncludeLabels()

    SetProperty<String> getExcludeLabels()

    void includeLabel(String label)

    void excludeLabel(String label)

    void category(Action<? super Category> action)

    void labeler(Action<? super Labeler> action)

    void replacer(Action<? super Replacer> action)

    void category(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Category) Closure<Void> action)

    void labeler(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Labeler) Closure<Void> action)

    void replacer(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Replacer) Closure<Void> action)

    interface Category {
        Property<String> getTitle()

        SetProperty<String> getLabels()
    }

    interface Labeler {
        Property<String> getLabel()

        Property<String> getTitle()

        Property<String> getBody()
    }

    interface Replacer {
        Property<String> getSearch()

        Property<String> getReplace()
    }
}