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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Brew
import org.jreleaser.gradle.plugin.dsl.Cask
import org.jreleaser.gradle.plugin.dsl.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.Tap
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class BrewImpl extends AbstractRepositoryTool implements Brew {
    final Property<String> formulaName
    final CommitAuthorImpl commitAuthor
    final TapImpl tap
    final CaskImpl cask
    final MapProperty<String, String> dependencies
    final ListProperty<String> livecheck

    @Inject
    BrewImpl(ObjectFactory objects) {
        super(objects)
        formulaName = objects.property(String).convention(Providers.notDefined())
        tap = objects.newInstance(TapImpl, objects)
        cask = objects.newInstance(CaskImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
        dependencies = objects.mapProperty(String, String).convention(Providers.notDefined())
        livecheck = objects.listProperty(String).convention(Providers.notDefined())
    }

    @Override
    void addDependency(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            dependencies.put(key.trim(), value.trim())
        }
    }

    @Override
    void addDependency(String key) {
        if (isNotBlank(key)) {
            dependencies.put(key.trim(), 'null')
        }
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            formulaName.present ||
            dependencies.present ||
            tap.isSet() ||
            commitAuthor.isSet() ||
            livecheck.present ||
            cask.isSet()
    }

    @Override
    void tap(Action<? super Tap> action) {
        action.execute(tap)
    }

    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    @Override
    void cask(Action<? super Cask> action) {
        action.execute(cask)
    }

    @Override
    void tap(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        ConfigureUtil.configure(action, tap)
    }

    @Override
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    @Override
    void cask(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Cask) Closure<Void> action) {
        ConfigureUtil.configure(action, cask)
    }

    org.jreleaser.model.Brew toModel() {
        org.jreleaser.model.Brew tool = new org.jreleaser.model.Brew()
        fillToolProperties(tool)
        if (formulaName.present) tool.formulaName = formulaName.get()
        if (tap.isSet()) tool.tap = tap.toHomebrewTap()
        if (commitAuthor.isSet()) tool.commitAuthor = commitAuthor.toModel()
        if (dependencies.present) tool.dependencies = dependencies.get()
        if (livecheck.present) tool.livecheck = (livecheck.get() as List<String>)
        if (cask.isSet()) tool.cask = cask.toModel()
        tool
    }
}
