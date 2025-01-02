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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.mustache.TemplateContext;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.13.0
 */
public final class EnvironmentVariables extends AbstractModelObject<EnvironmentVariables> implements Domain {
    private static final long serialVersionUID = -4764413898945637009L;

    private final Map<String, String> universal = new LinkedHashMap<>();
    private final Map<String, String> unix = new LinkedHashMap<>();
    private final Map<String, String> linux = new LinkedHashMap<>();
    private final Map<String, String> osx = new LinkedHashMap<>();
    private final Map<String, String> windows = new LinkedHashMap<>();

    @JsonIgnore
    private final org.jreleaser.model.api.common.EnvironmentVariables immutable = new org.jreleaser.model.api.common.EnvironmentVariables() {
        private static final long serialVersionUID = -8852099385145553688L;

        @Override
        public Map<String, String> getUniversal() {
            return unmodifiableMap(universal);
        }

        @Override
        public Map<String, String> getUnix() {
            return unmodifiableMap(unix);
        }

        @Override
        public Map<String, String> getLinux() {
            return unmodifiableMap(linux);
        }

        @Override
        public Map<String, String> getOsx() {
            return unmodifiableMap(osx);
        }

        @Override
        public Map<String, String> getWindows() {
            return unmodifiableMap(windows);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(EnvironmentVariables.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.common.EnvironmentVariables asImmutable() {
        return immutable;
    }

    @Override
    public void merge(EnvironmentVariables source) {
        setUniversal(merge(this.universal, source.universal));
        setUnix(merge(this.unix, source.unix));
        setLinux(merge(this.linux, source.linux));
        setOsx(merge(this.osx, source.osx));
        setWindows(merge(this.windows, source.windows));
    }

    public Map<String, String> getUniversal() {
        return universal;
    }

    public void setUniversal(Map<String, String> universal) {
        this.universal.clear();
        this.universal.putAll(universal);
    }

    public Map<String, String> getUnix() {
        return unix;
    }

    public void setUnix(Map<String, String> unix) {
        this.unix.clear();
        this.unix.putAll(unix);
    }

    public Map<String, String> getLinux() {
        return linux;
    }

    public void setLinux(Map<String, String> linux) {
        this.linux.clear();
        this.linux.putAll(linux);
    }

    public Map<String, String> getOsx() {
        return osx;
    }

    public void setOsx(Map<String, String> osx) {
        this.osx.clear();
        this.osx.putAll(osx);
    }

    public Map<String, String> getWindows() {
        return windows;
    }

    public void setWindows(Map<String, String> windows) {
        this.windows.clear();
        this.windows.putAll(windows);
    }

    public boolean isSet() {
        return
            !universal.isEmpty() ||
                !unix.isEmpty() ||
                !linux.isEmpty() ||
                !osx.isEmpty() ||
                !windows.isEmpty();
    }

    public Map<String, String> getResolvedUniversal(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        return universal.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, e -> resolveTemplate(e.getValue(), props)));
    }

    public Map<String, String> getResolvedUnix(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        return unix.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, e -> resolveTemplate(e.getValue(), props)));
    }

    public Map<String, String> getResolvedLinux(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        return linux.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, e -> resolveTemplate(e.getValue(), props)));
    }

    public Map<String, String> getResolvedOsx(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        return osx.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, e -> resolveTemplate(e.getValue(), props)));
    }

    public Map<String, String> getResolvedWindows(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        return windows.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, e -> resolveTemplate(e.getValue(), props)));
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("universal", universal);
        map.put("unix", unix);
        map.put("linux", linux);
        map.put("osx", osx);
        map.put("windows", windows);
        return map;
    }
}
