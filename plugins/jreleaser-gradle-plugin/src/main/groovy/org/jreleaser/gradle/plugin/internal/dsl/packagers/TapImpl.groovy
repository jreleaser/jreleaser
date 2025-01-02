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
package org.jreleaser.gradle.plugin.internal.dsl.packagers

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.packagers.Tap
import org.jreleaser.model.Active
import org.jreleaser.model.internal.announce.ArticleAnnouncer
import org.jreleaser.model.internal.packagers.AbstractRepositoryTap
import org.jreleaser.model.internal.packagers.AppImagePackager
import org.jreleaser.model.internal.packagers.AsdfPackager
import org.jreleaser.model.internal.packagers.BrewPackager
import org.jreleaser.model.internal.packagers.ChocolateyPackager
import org.jreleaser.model.internal.packagers.FlatpakPackager
import org.jreleaser.model.internal.packagers.GofishPackager
import org.jreleaser.model.internal.packagers.JbangPackager
import org.jreleaser.model.internal.packagers.MacportsPackager
import org.jreleaser.model.internal.packagers.ScoopPackager
import org.jreleaser.model.internal.packagers.SnapPackager
import org.jreleaser.model.internal.packagers.SpecPackager
import org.jreleaser.model.internal.packagers.WingetPackager

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class TapImpl implements Tap {
    final Property<Active> active
    final Property<String> repoOwner
    final Property<String> name
    final Property<String> tagName
    final Property<String> branch
    final Property<String> branchPush
    final Property<String> username
    final Property<String> token
    final Property<String> commitMessage
    final MapProperty<String, Object> extraProperties

    @Inject
    TapImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        repoOwner = objects.property(String).convention(Providers.<String> notDefined())
        name = objects.property(String).convention(Providers.<String> notDefined())
        tagName = objects.property(String).convention(Providers.<String> notDefined())
        branch = objects.property(String).convention(Providers.<String> notDefined())
        branchPush = objects.property(String).convention(Providers.<String> notDefined())
        username = objects.property(String).convention(Providers.<String> notDefined())
        token = objects.property(String).convention(Providers.<String> notDefined())
        commitMessage = objects.property(String).convention(Providers.<String> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Internal
    boolean isSet() {
        active.present ||
            repoOwner.present ||
            name.present ||
            tagName.present ||
            branch.present ||
            branchPush.present ||
            username.present ||
            token.present ||
            commitMessage.present ||
            extraProperties.present
    }

    private <T extends AbstractRepositoryTap> T convert(T into) {
        if (active.present) into.active = active.get()
        if (repoOwner.present) into.owner = repoOwner.get()
        if (name.present) into.name = name.get()
        if (tagName.present) into.tagName = tagName.get()
        if (branch.present) into.branch = branch.get()
        if (branchPush.present) into.branchPush = branchPush.get()
        if (username.present) into.username = username.get()
        if (token.present) into.token = token.get()
        if (commitMessage.present) into.commitMessage = commitMessage.get()
        if (extraProperties.present) into.extraProperties.putAll(extraProperties.get())
        into
    }

    AppImagePackager.AppImageRepository toAppImageRepository() {
        convert(new AppImagePackager.AppImageRepository())
    }

    AsdfPackager.AsdfRepository toAsdfRepository() {
        convert(new AsdfPackager.AsdfRepository())
    }

    BrewPackager.HomebrewRepository toHomebrewRepository() {
        convert(new BrewPackager.HomebrewRepository())
    }

    MacportsPackager.MacportsRepository toMacportsRepository() {
        convert(new MacportsPackager.MacportsRepository())
    }

    FlatpakPackager.FlatpakRepository toFlatpakRepository() {
        convert(new FlatpakPackager.FlatpakRepository())
    }

    GofishPackager.GofishRepository toGofishRepository() {
        convert(new GofishPackager.GofishRepository())
    }

    SpecPackager.SpecRepository toSpecRepository() {
        convert(new SpecPackager.SpecRepository())
    }

    SnapPackager.SnapRepository toSnapRepository() {
        convert(new SnapPackager.SnapRepository())
    }

    ArticleAnnouncer.Repository toRepository() {
        convert(new ArticleAnnouncer.Repository())
    }

    ScoopPackager.ScoopRepository toScoopRepository() {
        convert(new ScoopPackager.ScoopRepository())
    }

    ChocolateyPackager.ChocolateyRepository toChocolateyRepository() {
        convert(new ChocolateyPackager.ChocolateyRepository())
    }

    JbangPackager.JbangRepository toJbangRepository() {
        convert(new JbangPackager.JbangRepository())
    }

    WingetPackager.WingetRepository toWingetRepository() {
        convert(new WingetPackager.WingetRepository())
    }
}
