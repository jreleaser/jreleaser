/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Changelog
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ChangelogImpl implements Changelog {
    final Property<Boolean> enabled
    final Property<Boolean> links
    final Property<Boolean> hideUncategorized
    final Property<org.jreleaser.model.Changelog.Sort> sort
    final RegularFileProperty external
    final Property<Active> formatted
    final Property<String> change
    final Property<String> content
    final RegularFileProperty contentTemplate
    final SetProperty<String> includeLabels
    final SetProperty<String> excludeLabels
    final SetProperty<String> hiddenCategories

    private final List<CategoryImpl> categories = []
    private final Set<LabelerImpl> labelers = []
    private final Set<ReplacerImpl> replacers = []
    private final ObjectFactory objects

    @Inject
    ChangelogImpl(ObjectFactory objects) {
        this.objects = objects
        enabled = objects.property(Boolean).convention(Providers.notDefined())
        links = objects.property(Boolean).convention(Providers.notDefined())
        hideUncategorized = objects.property(Boolean).convention(Providers.notDefined())
        sort = objects.property(org.jreleaser.model.Changelog.Sort).convention(Providers.notDefined())
        external = objects.fileProperty().convention(Providers.notDefined())
        formatted = objects.property(Active).convention(Providers.notDefined())
        change = objects.property(String).convention(Providers.notDefined())
        content = objects.property(String).convention(Providers.notDefined())
        contentTemplate = objects.fileProperty().convention(Providers.notDefined())
        includeLabels = objects.setProperty(String).convention(Providers.notDefined())
        excludeLabels = objects.setProperty(String).convention(Providers.notDefined())
        hiddenCategories = objects.setProperty(String).convention(Providers.notDefined())
    }

    @Override
    void setFormatted(String str) {
        if (isNotBlank(str)) {
            formatted.set(Active.of(str.trim()))
        }
    }

    @Internal
    boolean isSet() {
        enabled.present ||
            links.present ||
            hideUncategorized.present ||
            external.present ||
            sort.present ||
            formatted.present ||
            change.present ||
            content.present ||
            contentTemplate.present ||
            includeLabels.present ||
            excludeLabels.present ||
            hiddenCategories.present ||
            !categories.isEmpty() ||
            !labelers.isEmpty() ||
            !replacers.isEmpty()
    }

    @Override
    void setSort(String sort) {
        this.sort.set(org.jreleaser.model.Changelog.Sort.valueOf(sort.toUpperCase()))
    }

    @Override
    void includeLabel(String label) {
        if (isNotBlank(label)) {
            includeLabels.add(label.trim())
        }
    }

    @Override
    void excludeLabel(String label) {
        if (isNotBlank(label)) {
            excludeLabels.add(label.trim())
        }
    }

    @Override
    void hideCategory(String category) {
        if (isNotBlank(category)) {
            hiddenCategories.add(category.trim())
        }
    }

    @Override
    void category(Action<? super Category> action) {
        CategoryImpl category = objects.newInstance(CategoryImpl, objects)
        action.execute(category)
        categories.add(category)
    }

    @Override
    void labeler(Action<? super Labeler> action) {
        LabelerImpl labeler = objects.newInstance(LabelerImpl, objects)
        action.execute(labeler)
        labelers.add(labeler)
    }

    @Override
    void replacer(Action<? super Replacer> action) {
        ReplacerImpl replacer = objects.newInstance(ReplacerImpl, objects)
        action.execute(replacer)
        replacers.add(replacer)
    }

    @Override
    void category(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Category) Closure<Void> action) {
        CategoryImpl category = objects.newInstance(CategoryImpl, objects)
        ConfigureUtil.configure(action, category)
        categories.add(category)
    }

    @Override
    void labeler(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Labeler) Closure<Void> action) {
        LabelerImpl labeler = objects.newInstance(LabelerImpl, objects)
        ConfigureUtil.configure(action, labeler)
        labelers.add(labeler)
    }

    @Override
    void replacer(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Replacer) Closure<Void> action) {
        ReplacerImpl replacer = objects.newInstance(ReplacerImpl, objects)
        ConfigureUtil.configure(action, replacer)
        replacers.add(replacer)
    }

    org.jreleaser.model.Changelog toModel() {
        org.jreleaser.model.Changelog changelog = new org.jreleaser.model.Changelog()
        if (enabled.present) {
            changelog.enabled = enabled.get()
        } else {
            changelog.enabled = isSet()
        }
        if (links.present) changelog.links = links.get()
        if (hideUncategorized.present) changelog.hideUncategorized = hideUncategorized.get()
        if (sort.present) changelog.sort = sort.get()
        if (external.present) changelog.external = external.getAsFile().get().toPath()
        if (formatted.present) changelog.formatted = formatted.get()
        if (change.present) changelog.change = change.get()
        if (content.present) changelog.content = content.get()
        if (contentTemplate.present) {
            changelog.contentTemplate = contentTemplate.asFile.get().absolutePath
        }
        changelog.includeLabels = (Set<String>) includeLabels.getOrElse([] as Set)
        changelog.excludeLabels = (Set<String>) excludeLabels.getOrElse([] as Set)
        changelog.hiddenCategories = (Set<String>) hiddenCategories.getOrElse([] as Set)
        changelog.setCategories(categories.collect([]) { CategoryImpl category ->
            category.toModel()
        } as List<org.jreleaser.model.Changelog.Category>)
        changelog.setLabelers(labelers.collect([] as Set) { LabelerImpl labeler ->
            labeler.toModel()
        } as Set<org.jreleaser.model.Changelog.Labeler>)
        changelog.setReplacers(replacers.collect([] as Set) { ReplacerImpl replacer ->
            replacer.toModel()
        } as Set<org.jreleaser.model.Changelog.Replacer>)
        changelog
    }

    @CompileStatic
    static class CategoryImpl implements Category {
        final Property<String> title
        final SetProperty<String> labels

        @Inject
        CategoryImpl(ObjectFactory objects) {
            title = objects.property(String).convention(Providers.notDefined())
            labels = objects.setProperty(String).convention(Providers.notDefined())
        }

        org.jreleaser.model.Changelog.Category toModel() {
            org.jreleaser.model.Changelog.Category category = new org.jreleaser.model.Changelog.Category()
            category.title = title.orNull
            category.labels = (Set<String>) labels.getOrElse([] as Set)
            category
        }
    }

    @CompileStatic
    static class LabelerImpl implements Labeler {
        final Property<String> label
        final Property<String> title
        final Property<String> body

        @Inject
        LabelerImpl(ObjectFactory objects) {
            label = objects.property(String).convention(Providers.notDefined())
            title = objects.property(String).convention(Providers.notDefined())
            body = objects.property(String).convention(Providers.notDefined())
        }

        org.jreleaser.model.Changelog.Labeler toModel() {
            org.jreleaser.model.Changelog.Labeler labeler = new org.jreleaser.model.Changelog.Labeler()
            labeler.label = label.orNull
            labeler.title = title.orNull
            labeler.body = body.orNull
            labeler
        }
    }

    @CompileStatic
    static class ReplacerImpl implements Replacer {
        final Property<String> search
        final Property<String> replace

        @Inject
        ReplacerImpl(ObjectFactory objects) {
            search = objects.property(String).convention(Providers.notDefined())
            replace = objects.property(String).convention(Providers.notDefined())
        }

        org.jreleaser.model.Changelog.Replacer toModel() {
            org.jreleaser.model.Changelog.Replacer replacer = new org.jreleaser.model.Changelog.Replacer()
            replacer.search = search.orNull
            replacer.replace = replace.getOrElse('')
            replacer
        }
    }
}
