/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Tap
import org.jreleaser.model.Active
import org.jreleaser.model.AppImage
import org.jreleaser.model.Asdf
import org.jreleaser.model.Brew
import org.jreleaser.model.Chocolatey
import org.jreleaser.model.Gofish
import org.jreleaser.model.Jbang
import org.jreleaser.model.Macports
import org.jreleaser.model.Repository
import org.jreleaser.model.RepositoryTap
import org.jreleaser.model.Scoop
import org.jreleaser.model.Snap
import org.jreleaser.model.Spec

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
    final Property<String> owner
    final Property<String> name
    final Property<String> tagName
    final Property<String> branch
    final Property<String> username
    final Property<String> token
    final Property<String> commitMessage

    @Inject
    TapImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())
        owner = objects.property(String).convention(Providers.notDefined())
        name = objects.property(String).convention(Providers.notDefined())
        tagName = objects.property(String).convention(Providers.notDefined())
        branch = objects.property(String).convention(Providers.notDefined())
        username = objects.property(String).convention(Providers.notDefined())
        token = objects.property(String).convention(Providers.notDefined())
        commitMessage = objects.property(String).convention(Providers.notDefined())
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
            owner.present ||
            name.present ||
            tagName.present ||
            branch.present ||
            username.present ||
            token.present ||
            commitMessage.present
    }

    private void convert(RepositoryTap into) {
        if (active.present) into.active = active.get()
        if (owner.present) into.owner = owner.get()
        if (name.present) into.name = name.get()
        if (tagName.present) into.tagName = tagName.get()
        if (branch.present) into.branch = branch.get()
        if (username.present) into.username = username.get()
        if (token.present) into.token = token.get()
        if (commitMessage.present) into.commitMessage = commitMessage.get()
    }

    AppImage.AppImageRepository toAppImageRepository() {
        AppImage.AppImageRepository tap = new AppImage.AppImageRepository()
        convert(tap)
        tap
    }

    Asdf.AsdfRepository toAsdfRepository() {
        Asdf.AsdfRepository tap = new Asdf.AsdfRepository()
        convert(tap)
        tap
    }

    Brew.HomebrewTap toHomebrewTap() {
        Brew.HomebrewTap tap = new Brew.HomebrewTap()
        convert(tap)
        tap
    }

    Macports.MacportsRepository toMacportsRepository() {
        Macports.MacportsRepository tap = new Macports.MacportsRepository()
        convert(tap)
        tap
    }

    Gofish.GofishRepository toGofishRepository() {
        Gofish.GofishRepository tap = new Gofish.GofishRepository()
        convert(tap)
        tap
    }

    Spec.SpecRepository toSpecRepository() {
        Spec.SpecRepository tap = new Spec.SpecRepository()
        convert(tap)
        tap
    }

    Snap.SnapTap toSnapTap() {
        Snap.SnapTap tap = new Snap.SnapTap()
        convert(tap)
        tap
    }

    Repository toRepository() {
        Repository tap = new Repository()
        convert(tap)
        tap
    }

    Scoop.ScoopBucket toScoopBucket() {
        Scoop.ScoopBucket tap = new Scoop.ScoopBucket()
        convert(tap)
        tap
    }

    Chocolatey.ChocolateyBucket toChocolateyBucket() {
        Chocolatey.ChocolateyBucket tap = new Chocolatey.ChocolateyBucket()
        convert(tap)
        tap
    }

    Jbang.JbangCatalog toJbangCatalog() {
        Jbang.JbangCatalog tap = new Jbang.JbangCatalog()
        convert(tap)
        tap
    }
}
