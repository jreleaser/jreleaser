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
package org.jreleaser.gradle.plugin.internal.dsl.catalog.swid

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.catalog.swid.Entity
import org.jreleaser.gradle.plugin.dsl.catalog.swid.SwidTag
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.11.0
 */
@CompileStatic
class SwidTagImpl implements SwidTag {
    String name
    final Property<Active> active
    final Property<String> tagRef
    final Property<String> path
    final Property<String> tagId
    final Property<Integer> tagVersion
    final Property<String> lang
    final Property<Boolean> corpus
    final Property<Boolean> patch

    private final NamedDomainObjectContainer<EntityImpl> entities

    @Inject
    SwidTagImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        tagRef = objects.property(String).convention(Providers.<String> notDefined())
        path = objects.property(String).convention(Providers.<String> notDefined())
        tagId = objects.property(String).convention(Providers.<String> notDefined())
        tagVersion = objects.property(Integer).convention(Providers.<Integer> notDefined())
        lang = objects.property(String).convention(Providers.<String> notDefined())
        corpus = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        patch = objects.property(Boolean).convention(Providers.<Boolean> notDefined())

        entities = objects.domainObjectContainer(EntityImpl, new NamedDomainObjectFactory<EntityImpl>() {
            @Override
            EntityImpl create(String name) {
                EntityImpl entity = objects.newInstance(EntityImpl, objects)
                entity
            }
        })
    }

    @Internal
    boolean isSet() {
        tagRef.present ||
            path.present ||
            tagId.present ||
            tagVersion.present ||
            lang.present ||
            corpus.present ||
            patch.present ||
            !entities.isEmpty()
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void entity(Action<? super Entity> action) {
        action.execute(entities.maybeCreate("entity-${entities.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void entity(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Entity) Closure<Void> action) {
        ConfigureUtil.configure(action, entities.maybeCreate("entity-${entities.size()}".toString()))
    }

    org.jreleaser.model.internal.catalog.swid.SwidTag toModel() {
        org.jreleaser.model.internal.catalog.swid.SwidTag tag = new org.jreleaser.model.internal.catalog.swid.SwidTag()
        tag.name = name
        if (active.present) tag.active = active.get()
        if (tagRef.present) tag.name = tagRef.get()
        if (path.present) tag.path = path.get()
        if (tagId.present) tag.tagId = tagId.get()
        if (tagVersion.present) tag.tagVersion = tagVersion.get()
        if (lang.present) tag.lang = lang.get()
        if (corpus.present) tag.corpus = corpus.get()
        if (patch.present) tag.path = patch.get()
        for (EntityImpl entity : entities) {
            tag.addEntity(entity.toModel())
        }
        tag
    }
}
