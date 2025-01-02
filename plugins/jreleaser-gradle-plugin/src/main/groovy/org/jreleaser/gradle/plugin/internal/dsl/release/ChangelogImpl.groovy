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
package org.jreleaser.gradle.plugin.internal.dsl.release

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.release.Changelog
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
    final Property<Boolean> skipMergeCommits
    final Property<org.jreleaser.model.Changelog.Sort> sort
    final RegularFileProperty external
    final Property<Active> formatted
    final Property<String> format
    final Property<String> categoryTitleFormat
    final Property<String> contributorsTitleFormat
    final Property<String> preset
    final Property<String> content
    final RegularFileProperty contentTemplate
    final SetProperty<String> includeLabels
    final SetProperty<String> excludeLabels
    final HideImpl hide
    final ContributorsImpl contributors
    final AppendImpl append
    final MapProperty<String, Object> extraProperties

    private final List<CategoryImpl> categories = []
    private final List<LabelerImpl> labelers = []
    private final Set<ReplacerImpl> replacers = []
    private final ObjectFactory objects

    @Inject
    ChangelogImpl(ObjectFactory objects) {
        this.objects = objects
        enabled = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        links = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        hideUncategorized = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        skipMergeCommits = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        sort = objects.property(org.jreleaser.model.Changelog.Sort).convention(Providers.<org.jreleaser.model.Changelog.Sort> notDefined())
        external = objects.fileProperty().convention(Providers.notDefined())
        formatted = objects.property(Active).convention(Providers.<Active> notDefined())
        format = objects.property(String).convention(Providers.<String> notDefined())
        categoryTitleFormat = objects.property(String).convention(Providers.<String> notDefined())
        contributorsTitleFormat = objects.property(String).convention(Providers.<String> notDefined())
        preset = objects.property(String).convention(Providers.<String> notDefined())
        content = objects.property(String).convention(Providers.<String> notDefined())
        contentTemplate = objects.fileProperty().convention(Providers.notDefined())
        includeLabels = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        excludeLabels = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        hide = objects.newInstance(HideImpl, objects)
        contributors = objects.newInstance(ContributorsImpl, objects)
        append = objects.newInstance(AppendImpl, objects)
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Override
    void setFormatted(String str) {
        if (isNotBlank(str)) {
            formatted.set(Active.of(str.trim()))
        }
    }

    @Override
    void setExternal(String external) {
        this.external.set(new File(external))
    }

    @Override
    void setContentTemplate(String contentTemplate) {
        this.contentTemplate.set(new File(contentTemplate))
    }

    @Internal
    boolean isSet() {
        links.present ||
            skipMergeCommits.present ||
            hideUncategorized.present ||
            external.present ||
            sort.present ||
            formatted.present ||
            format.present ||
            categoryTitleFormat.present ||
            contributorsTitleFormat.present ||
            preset.present ||
            content.present ||
            contentTemplate.present ||
            includeLabels.present ||
            excludeLabels.present ||
            !categories.isEmpty() ||
            !labelers.isEmpty() ||
            !replacers.isEmpty() ||
            contributors.isSet() ||
            hide.isSet() ||
            append.isSet() ||
            extraProperties.present
    }

    @Override
    void setSort(String sort) {
        this.sort.set(org.jreleaser.model.Changelog.Sort.valueOf(sort.toUpperCase(Locale.ENGLISH)))
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
    void hide(Action<? super Hide> action) {
        action.execute(hide)
    }

    @Override
    void contributors(Action<? super Contributors> action) {
        action.execute(contributors)
    }

    @Override
    void append(Action<? super Append> action) {
        action.execute(append)
    }

    @Override
    @CompileDynamic
    void category(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Category) Closure<Void> action) {
        CategoryImpl category = objects.newInstance(CategoryImpl, objects)
        ConfigureUtil.configure(action, category)
        categories.add(category)
    }

    @Override
    @CompileDynamic
    void labeler(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Labeler) Closure<Void> action) {
        LabelerImpl labeler = objects.newInstance(LabelerImpl, objects)
        ConfigureUtil.configure(action, labeler)
        labelers.add(labeler)
    }

    @Override
    @CompileDynamic
    void replacer(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Replacer) Closure<Void> action) {
        ReplacerImpl replacer = objects.newInstance(ReplacerImpl, objects)
        ConfigureUtil.configure(action, replacer)
        replacers.add(replacer)
    }

    @Override
    @CompileDynamic
    void hide(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Hide) Closure<Void> action) {
        ConfigureUtil.configure(action, hide)
    }

    @Override
    @CompileDynamic
    void contributors(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Contributors) Closure<Void> action) {
        ConfigureUtil.configure(action, contributors)
    }

    @Override
    @CompileDynamic
    void append(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Append) Closure<Void> action) {
        ConfigureUtil.configure(action, append)
    }

    org.jreleaser.model.internal.release.Changelog toModel() {
        org.jreleaser.model.internal.release.Changelog changelog = new org.jreleaser.model.internal.release.Changelog()
        if (enabled.present) {
            changelog.enabled = enabled.get()
        } else {
            changelog.enabled = true
        }

        if (!changelog.enabled) return changelog

        if (links.present) changelog.links = links.get()
        if (skipMergeCommits.present) changelog.skipMergeCommits = skipMergeCommits.get()
        if (hideUncategorized.present) hide.uncategorized.set(hideUncategorized.get())
        if (sort.present) changelog.sort = sort.get()
        if (external.present) changelog.external = external.getAsFile().get().toPath()
        if (formatted.present) changelog.formatted = formatted.get()
        if (format.present) changelog.format = format.get()
        if (categoryTitleFormat.present) changelog.categoryTitleFormat = categoryTitleFormat.get()
        if (contributorsTitleFormat.present) changelog.contributorsTitleFormat = contributorsTitleFormat.get()
        if (preset.present) changelog.preset = preset.get()
        if (content.present) changelog.content = content.get()
        if (contentTemplate.present) {
            changelog.contentTemplate = contentTemplate.asFile.get().absolutePath
        }
        changelog.includeLabels = (Set<String>) includeLabels.getOrElse([] as Set)
        changelog.excludeLabels = (Set<String>) excludeLabels.getOrElse([] as Set)
        changelog.setCategories(categories.collect([]) { CategoryImpl category ->
            category.toModel()
        } as Set<org.jreleaser.model.internal.release.Changelog.Category>)
        changelog.setLabelers(labelers.collect([] as Set) { LabelerImpl labeler ->
            labeler.toModel()
        } as Set<org.jreleaser.model.internal.release.Changelog.Labeler>)
        changelog.setReplacers(replacers.collect([] as List) { ReplacerImpl replacer ->
            replacer.toModel()
        } as List<org.jreleaser.model.internal.release.Changelog.Replacer>)
        changelog.hide = hide.toModel()
        changelog.contributors = contributors.toModel()
        changelog.append = append.toModel()
        if (extraProperties.present) changelog.extraProperties.putAll(extraProperties.get())
        changelog
    }

    @CompileStatic
    static class AppendImpl implements Append {
        final Property<Boolean> enabled
        final Property<String> title
        final RegularFileProperty target
        final Property<String> content
        final RegularFileProperty contentTemplate

        @Inject
        AppendImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            title = objects.property(String).convention(Providers.<String> notDefined())
            contentTemplate = objects.fileProperty().convention(Providers.notDefined())
            content = objects.property(String).convention(Providers.<String> notDefined())
            target = objects.fileProperty().convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            enabled.present ||
                title.present ||
                target.present ||
                content.present ||
                contentTemplate.present
        }

        @Override
        void setTarget(String target) {
            this.target.set(new File(target))
        }

        @Override
        void setContentTemplate(String contentTemplate) {
            this.contentTemplate.set(new File(contentTemplate))
        }

        org.jreleaser.model.internal.release.Changelog.Append toModel() {
            org.jreleaser.model.internal.release.Changelog.Append append = new org.jreleaser.model.internal.release.Changelog.Append()
            append.enabled = enabled.orNull
            append.title = title.orNull
            if (target.present) {
                append.target = target.asFile.get().absolutePath
            }
            append.content = content.orNull
            if (contentTemplate.present) {
                append.contentTemplate = contentTemplate.asFile.get().absolutePath
            }
            append
        }
    }

    @CompileStatic
    static class CategoryImpl implements Category {
        final Property<String> key
        final Property<String> title
        final SetProperty<String> labels
        final Property<String> format
        final Property<Integer> order

        @Inject
        CategoryImpl(ObjectFactory objects) {
            key = objects.property(String).convention(Providers.<String> notDefined())
            title = objects.property(String).convention(Providers.<String> notDefined())
            labels = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            format = objects.property(String).convention(Providers.<String> notDefined())
            order = objects.property(Integer).convention(Providers.<Integer> notDefined())
        }

        org.jreleaser.model.internal.release.Changelog.Category toModel() {
            org.jreleaser.model.internal.release.Changelog.Category category = new org.jreleaser.model.internal.release.Changelog.Category()
            category.key = key.orNull
            category.title = title.orNull
            category.labels = (Set<String>) labels.getOrElse([] as Set)
            category.format = format.orNull
            category.order = order.orNull
            category
        }
    }

    @CompileStatic
    static class LabelerImpl implements Labeler {
        final Property<String> label
        final Property<String> title
        final Property<String> body
        final Property<String> contributor
        final Property<Integer> order

        @Inject
        LabelerImpl(ObjectFactory objects) {
            label = objects.property(String).convention(Providers.<String> notDefined())
            title = objects.property(String).convention(Providers.<String> notDefined())
            body = objects.property(String).convention(Providers.<String> notDefined())
            contributor = objects.property(String).convention(Providers.<String> notDefined())
            order = objects.property(Integer).convention(Providers.<Integer> notDefined())
        }

        org.jreleaser.model.internal.release.Changelog.Labeler toModel() {
            org.jreleaser.model.internal.release.Changelog.Labeler labeler = new org.jreleaser.model.internal.release.Changelog.Labeler()
            labeler.label = label.orNull
            labeler.title = title.orNull
            labeler.body = body.orNull
            labeler.contributor = contributor.orNull
            labeler.order = order.orNull
            labeler
        }
    }

    @CompileStatic
    static class ReplacerImpl implements Replacer {
        final Property<String> search
        final Property<String> replace

        @Inject
        ReplacerImpl(ObjectFactory objects) {
            search = objects.property(String).convention(Providers.<String> notDefined())
            replace = objects.property(String).convention(Providers.<String> notDefined())
        }

        org.jreleaser.model.internal.release.Changelog.Replacer toModel() {
            org.jreleaser.model.internal.release.Changelog.Replacer replacer = new org.jreleaser.model.internal.release.Changelog.Replacer()
            replacer.search = search.orNull
            replacer.replace = replace.getOrElse('')
            replacer
        }
    }

    @CompileStatic
    static class ContributorsImpl implements Contributors {
        final Property<Boolean> enabled
        final Property<String> format

        @Inject
        ContributorsImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            format = objects.property(String).convention(Providers.<String> notDefined())
        }

        @Internal
        boolean isSet() {
            enabled.present ||
                format.present
        }

        org.jreleaser.model.internal.release.Changelog.Contributors toModel() {
            org.jreleaser.model.internal.release.Changelog.Contributors contributors = new org.jreleaser.model.internal.release.Changelog.Contributors()
            if (enabled.present) contributors.enabled = enabled.get()
            if (format.present) contributors.format = format.get()
            contributors
        }
    }

    @CompileStatic
    static class HideImpl implements Hide {
        final Property<Boolean> uncategorized
        final SetProperty<String> categories
        final SetProperty<String> contributors

        @Inject
        HideImpl(ObjectFactory objects) {
            uncategorized = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            categories = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            contributors = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        }

        @Internal
        boolean isSet() {
            uncategorized.present ||
                categories.present ||
                contributors.present
        }

        @Override
        void category(String category) {
            if (isNotBlank(category)) {
                categories.add(category.trim())
            }
        }

        @Override
        void contributor(String contributor) {
            if (isNotBlank(contributor)) {
                contributors.add(contributor.trim())
            }
        }

        org.jreleaser.model.internal.release.Changelog.Hide toModel() {
            org.jreleaser.model.internal.release.Changelog.Hide hide = new org.jreleaser.model.internal.release.Changelog.Hide()
            if (uncategorized.present) hide.uncategorized = uncategorized.get()
            hide.categories = (Set<String>) categories.getOrElse([] as Set)
            hide.contributors = (Set<String>) contributors.getOrElse([] as Set)
            hide
        }
    }
}
