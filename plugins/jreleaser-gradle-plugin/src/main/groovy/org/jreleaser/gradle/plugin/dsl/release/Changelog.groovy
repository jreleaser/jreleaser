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
package org.jreleaser.gradle.plugin.dsl.release

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.common.ExtraProperties
import org.jreleaser.model.Active

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Changelog extends ExtraProperties {
    Property<org.jreleaser.model.Changelog.Sort> getSort()

    Property<Boolean> getEnabled()

    Property<Boolean> getLinks()

    RegularFileProperty getExternal()

    void setExternal(String external)

    void setSort(String sort)

    Property<Active> getFormatted()

    void setFormatted(String str)

    Property<String> getFormat()

    Property<String> getCategoryTitleFormat()

    Property<String> getContributorsTitleFormat()

    Property<String> getContent()

    Property<String> getPreset()

    Property<Boolean> getSkipMergeCommits()

    RegularFileProperty getContentTemplate()

    void setContentTemplate(String contentTemplate)

    SetProperty<String> getIncludeLabels()

    SetProperty<String> getExcludeLabels()

    void includeLabel(String label)

    void excludeLabel(String label)

    void category(Action<? super Category> action)

    void labeler(Action<? super Labeler> action)

    void replacer(Action<? super Replacer> action)

    Hide getHide()

    Contributors getContributors()

    Append getAppend()

    void hide(Action<? super Hide> action)

    void contributors(Action<? super Contributors> action)

    void append(Action<? super Append> action)

    interface Append {
        Property<Boolean> getEnabled()

        Property<String> getTitle()

        RegularFileProperty getTarget()

        Property<String> getContent()

        RegularFileProperty getContentTemplate()

        void setTarget(String target)

        void setContentTemplate(String contentTemplate)
    }

    interface Category {
        Property<String> getKey()

        Property<String> getTitle()

        SetProperty<String> getLabels()

        Property<String> getFormat()

        Property<Integer> getOrder()
    }

    interface Labeler {
        Property<String> getLabel()

        Property<String> getTitle()

        Property<String> getBody()

        Property<String> getContributor()

        Property<Integer> getOrder()
    }

    interface Replacer {
        Property<String> getSearch()

        Property<String> getReplace()
    }

    interface Contributors {
        Property<Boolean> getEnabled()

        Property<String> getFormat()
    }

    interface Hide {
        Property<Boolean> getUncategorized()

        SetProperty<String> getCategories()

        SetProperty<String> getContributors()

        void category(String category)

        void contributor(String contributor)
    }
}
