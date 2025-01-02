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
package org.jreleaser.gradle.plugin.internal.dsl.common

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.ArchiveOptions

import javax.inject.Inject
import java.time.ZonedDateTime

import static org.jreleaser.util.StringUtils.isNotBlank
import static org.jreleaser.util.TimeUtils.TIMESTAMP_FORMATTER

/**
 *
 * @author Andres Almiray
 * @since 1.6.0
 */
@CompileStatic
class ArchiveOptionsImpl implements ArchiveOptions {
    final Property<ZonedDateTime> timestamp
    final Property<org.jreleaser.model.api.common.ArchiveOptions.TarMode> longFileMode
    final Property<org.jreleaser.model.api.common.ArchiveOptions.TarMode> bigNumberMode

    @Inject
    ArchiveOptionsImpl(ObjectFactory objects) {
        timestamp = objects.property(ZonedDateTime).convention(Providers.<ZonedDateTime> notDefined())
        longFileMode = objects.property(org.jreleaser.model.api.common.ArchiveOptions.TarMode).convention(Providers.<org.jreleaser.model.api.common.ArchiveOptions.TarMode> notDefined())
        bigNumberMode = objects.property(org.jreleaser.model.api.common.ArchiveOptions.TarMode).convention(Providers.<org.jreleaser.model.api.common.ArchiveOptions.TarMode> notDefined())
    }

    @Internal
    boolean isSet() {
        timestamp.present ||
            longFileMode.present ||
            bigNumberMode.present
    }

    @Override
    void setTimestamp(String str) {
        if (isNotBlank(str)) {
            timestamp.set(ZonedDateTime.parse(str, TIMESTAMP_FORMATTER))
        }
    }

    @Override
    void setLongFileMode(String str) {
        if (isNotBlank(str)) {
            longFileMode.set(org.jreleaser.model.api.common.ArchiveOptions.TarMode.of(str.trim()))
        }
    }

    @Override
    void setBigNumberMode(String str) {
        if (isNotBlank(str)) {
            bigNumberMode.set(org.jreleaser.model.api.common.ArchiveOptions.TarMode.of(str.trim()))
        }
    }

    org.jreleaser.model.internal.common.ArchiveOptions toModel() {
        org.jreleaser.model.internal.common.ArchiveOptions o = new org.jreleaser.model.internal.common.ArchiveOptions()
        if (timestamp.present) o.timestamp = timestamp.get()
        if (longFileMode.present) o.longFileMode = longFileMode.get()
        if (bigNumberMode.present) o.bigNumberMode = bigNumberMode.get()
        o
    }
}
