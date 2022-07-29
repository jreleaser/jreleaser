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
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Assembler
import org.jreleaser.gradle.plugin.dsl.FileSet
import org.jreleaser.gradle.plugin.dsl.Platform
import org.jreleaser.model.Active
import org.jreleaser.model.Stereotype
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractAssembler implements Assembler {
    final Property<Boolean> exported
    final Property<Active> active
    final Property<Stereotype> stereotype
    final MapProperty<String, Object> extraProperties
    final NamedDomainObjectContainer<FileSetImpl> fileSets

    @Inject
    AbstractAssembler(ObjectFactory objects) {
        exported = objects.property(Boolean).convention(Providers.notDefined())
        active = objects.property(Active).convention(Providers.notDefined())
        stereotype = objects.property(Stereotype).convention(Providers.notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())

        fileSets = objects.domainObjectContainer(FileSetImpl, new NamedDomainObjectFactory<FileSetImpl>() {
            @Override
            FileSetImpl create(String name) {
                FileSetImpl fs = objects.newInstance(FileSetImpl, objects)
                fs.name = name
                fs
            }
        })
    }

    @Internal
    boolean isSet() {
        exported.present ||
            active.present ||
            extraProperties.present ||
            !fileSets.isEmpty()
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void setStereotype(String str) {
        if (isNotBlank(str)) {
            stereotype.set(Stereotype.of(str.trim()))
        }
    }

    @Override
    void fileSet(Action<? super FileSet> action) {
        action.execute(fileSets.maybeCreate("fileSet-${fileSets.size()}".toString()))
    }

    @Override
    void platform(Action<? super Platform> action) {
        action.execute(platform)
    }

    @Override
    void fileSet(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = FileSet) Closure<Void> action) {
        ConfigureUtil.configure(action, fileSets.maybeCreate("fileSet-${fileSets.size()}".toString()))
    }

    @Override
    void platform(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Platform) Closure<Void> action) {
        ConfigureUtil.configure(action, platform)
    }

    protected <A extends org.jreleaser.model.Assembler> void fillProperties(A assembler) {
        assembler.exported = exported.getOrElse(true)
        if (active.present) assembler.active = active.get()
        if (stereotype.present) assembler.stereotype = stereotype.get()
        if (extraProperties.present) assembler.extraProperties.putAll(extraProperties.get())
        for (FileSetImpl fileSet : fileSets) {
            assembler.addFileSet(fileSet.toModel())
        }
    }
}
