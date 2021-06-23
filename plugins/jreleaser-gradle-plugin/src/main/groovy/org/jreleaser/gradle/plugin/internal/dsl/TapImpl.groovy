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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Tap
import org.jreleaser.model.ChocolateyBucket
import org.jreleaser.model.HomebrewTap
import org.jreleaser.model.JbangCatalog
import org.jreleaser.model.ScoopBucket
import org.jreleaser.model.SnapTap

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class TapImpl implements Tap {
    final Property<String> owner
    final Property<String> name
    final Property<String> username
    final Property<String> token

    @Inject
    TapImpl(ObjectFactory objects) {
        owner = objects.property(String).convention(Providers.notDefined())
        name = objects.property(String).convention(Providers.notDefined())
        username = objects.property(String).convention(Providers.notDefined())
        token = objects.property(String).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        owner.present ||
            name.present ||
            username.present ||
            token.present
    }

    HomebrewTap toHomebrewTap() {
        HomebrewTap tap = new HomebrewTap()
        if (owner.present) tap.owner = owner.get()
        if (name.present) tap.name = name.get()
        if (username.present) tap.name = username.get()
        if (token.present) tap.token = token.get()
        tap
    }

    SnapTap toSnapTap() {
        SnapTap tap = new SnapTap()
        if (owner.present) tap.owner = owner.get()
        if (name.present) tap.name = name.get()
        if (username.present) tap.name = username.get()
        if (token.present) tap.token = token.get()
        tap
    }

    ScoopBucket toScoopBucket() {
        ScoopBucket tap = new ScoopBucket()
        if (owner.present) tap.owner = owner.get()
        if (name.present) tap.name = name.get()
        if (username.present) tap.name = username.get()
        if (token.present) tap.token = token.get()
        tap
    }

    ChocolateyBucket toChocolateyBucket() {
        ChocolateyBucket tap = new ChocolateyBucket()
        if (owner.present) tap.owner = owner.get()
        if (name.present) tap.name = name.get()
        if (username.present) tap.name = username.get()
        if (token.present) tap.token = token.get()
        tap
    }

    JbangCatalog toJbangCatalog() {
        JbangCatalog catalog = new JbangCatalog()
        if (owner.present) catalog.owner = owner.get()
        if (name.present) catalog.name = name.get()
        if (username.present) catalog.name = username.get()
        if (token.present) catalog.token = token.get()
        catalog
    }
}
