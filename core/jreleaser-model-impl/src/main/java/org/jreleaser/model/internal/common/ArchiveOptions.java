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
package org.jreleaser.model.internal.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.util.FileUtils;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.util.TimeUtils.TIMESTAMP_FORMATTER;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public class ArchiveOptions extends AbstractModelObject<ArchiveOptions> implements Domain {
    private static final long serialVersionUID = -2360394060904246488L;

    private ZonedDateTime timestamp;
    private org.jreleaser.model.api.common.ArchiveOptions.TarMode longFileMode = org.jreleaser.model.api.common.ArchiveOptions.TarMode.ERROR;
    private org.jreleaser.model.api.common.ArchiveOptions.TarMode bigNumberMode = org.jreleaser.model.api.common.ArchiveOptions.TarMode.ERROR;

    @JsonIgnore
    private final org.jreleaser.model.api.common.ArchiveOptions immutable = new org.jreleaser.model.api.common.ArchiveOptions() {
        private static final long serialVersionUID = -4559383384992086381L;

        @Override
        public ZonedDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public TarMode getLongFileMode() {
            return longFileMode;
        }

        @Override
        public TarMode getBigNumberMode() {
            return bigNumberMode;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ArchiveOptions.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.common.ArchiveOptions asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ArchiveOptions source) {
        this.timestamp = merge(this.timestamp, source.timestamp);
        this.longFileMode = merge(this.longFileMode, source.longFileMode);
        this.bigNumberMode = merge(this.bigNumberMode, source.bigNumberMode);
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = ZonedDateTime.parse(timestamp, TIMESTAMP_FORMATTER);
    }

    public org.jreleaser.model.api.common.ArchiveOptions.TarMode getLongFileMode() {
        return longFileMode;
    }

    public void setLongFileMode(org.jreleaser.model.api.common.ArchiveOptions.TarMode longFileMode) {
        this.longFileMode = longFileMode;
    }

    public void setLongFileMode(String longFileMode) {
        setLongFileMode(org.jreleaser.model.api.common.ArchiveOptions.TarMode.of(longFileMode));
    }

    public org.jreleaser.model.api.common.ArchiveOptions.TarMode getBigNumberMode() {
        return bigNumberMode;
    }

    public void setBigNumberMode(org.jreleaser.model.api.common.ArchiveOptions.TarMode bigNumberMode) {
        this.bigNumberMode = bigNumberMode;
    }

    public void setBigNumberMode(String bigNumberMode) {
        setBigNumberMode(org.jreleaser.model.api.common.ArchiveOptions.TarMode.of(bigNumberMode));
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", timestamp);
        map.put("longFileMode", longFileMode);
        map.put("bigNumberMode", bigNumberMode);
        return map;
    }

    public FileUtils.ArchiveOptions toOptions() {
        return new FileUtils.ArchiveOptions()
            .withTimestamp(timestamp)
            .withLongFileMode(FileUtils.ArchiveOptions.TarMode.of(longFileMode.name()))
            .withBigNumberMode(FileUtils.ArchiveOptions.TarMode.of(bigNumberMode.name()));
    }
}