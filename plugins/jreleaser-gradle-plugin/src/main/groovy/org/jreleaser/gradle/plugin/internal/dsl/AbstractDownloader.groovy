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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Downloader
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
abstract class AbstractDownloader implements Downloader {
    final Property<Active> active
    final Property<Integer> connectTimeout
    final Property<Integer> readTimeout
    final MapProperty<String, Object> extraProperties

    @Inject
    AbstractDownloader(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())
        connectTimeout = objects.property(Integer).convention(Providers.notDefined())
        readTimeout = objects.property(Integer).convention(Providers.notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        active.present ||
            connectTimeout.present ||
            readTimeout.present ||
            extraProperties.present
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void unpack(Action<? super Unpack> action) {
        action.execute(unpack)
    }

    @Override
    void unpack(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Unpack) Closure<Void> action) {
        ConfigureUtil.configure(action, unpack)
    }

    protected <U extends org.jreleaser.model.Downloader> void fillProperties(U downloader) {
        if (active.present) downloader.active = active.get()
        if (connectTimeout.present) downloader.connectTimeout = connectTimeout.get()
        if (readTimeout.present) downloader.readTimeout = readTimeout.get()
        if (extraProperties.present) downloader.extraProperties.putAll(extraProperties.get())
    }

    @CompileStatic
    static class UnpackImpl implements Unpack {
        final Property<Boolean> enabled
        final Property<Boolean> skipRootEntry

        @Inject
        UnpackImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(Providers.notDefined())
            skipRootEntry = objects.property(Boolean).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            enabled.present ||
                skipRootEntry.present
        }

        org.jreleaser.model.Downloader.Unpack toModel() {
            org.jreleaser.model.Downloader.Unpack unpack = new org.jreleaser.model.Downloader.Unpack()
            if (enabled.present) unpack.enabled = enabled.get()
            if (skipRootEntry.present) unpack.skipRootEntry = skipRootEntry.get()
            unpack
        }
    }
}
