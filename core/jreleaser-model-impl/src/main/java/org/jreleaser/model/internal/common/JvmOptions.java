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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.System.lineSeparator;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.13.0
 */
public final class JvmOptions extends AbstractModelObject<JvmOptions> implements Domain {
    //private static final long serialVersionUID = 4713757280623221679L;

    private final List<String> universal = new ArrayList<>();
    private final List<String> unix = new ArrayList<>();
    private final List<String> linux = new ArrayList<>();
    private final List<String> osx = new ArrayList<>();
    private final List<String> windows = new ArrayList<>();

    @JsonIgnore
    private final org.jreleaser.model.api.common.JvmOptions immutable = new org.jreleaser.model.api.common.JvmOptions() {
        private static final long serialVersionUID = -7553541254718019915L;

        @Override
        public List<String> getUniversal() {
            return unmodifiableList(universal);
        }

        @Override
        public List<String> getUnix() {
            return unmodifiableList(unix);
        }

        @Override
        public List<String> getLinux() {
            return unmodifiableList(linux);
        }

        @Override
        public List<String> getOsx() {
            return unmodifiableList(osx);
        }

        @Override
        public List<String> getWindows() {
            return unmodifiableList(windows);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(JvmOptions.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.common.JvmOptions asImmutable() {
        return immutable;
    }

    @Override
    public void merge(JvmOptions source) {
        setUniversal(merge(this.universal, source.universal));
        setUnix(merge(this.unix, source.unix));
        setLinux(merge(this.linux, source.linux));
        setOsx(merge(this.osx, source.osx));
        setWindows(merge(this.windows, source.windows));
    }

    public void merge(Set<String> source) {
        this.universal.addAll(source);
    }

    public List<String> getUniversal() {
        return universal;
    }

    public void setUniversal(List<String> universal) {
        this.universal.clear();
        this.universal.addAll(universal);
    }

    public List<String> getUnix() {
        return unix;
    }

    public void setUnix(List<String> unix) {
        this.unix.clear();
        this.unix.addAll(unix);
    }

    public List<String> getLinux() {
        return linux;
    }

    public void setLinux(List<String> linux) {
        this.linux.clear();
        this.linux.addAll(linux);
    }

    public List<String> getOsx() {
        return osx;
    }

    public void setOsx(List<String> osx) {
        this.osx.clear();
        this.osx.addAll(osx);
    }

    public List<String> getWindows() {
        return windows;
    }

    public void setWindows(List<String> windows) {
        this.windows.clear();
        this.windows.addAll(windows);
    }

    public boolean isSet() {
        return
            !universal.isEmpty() ||
                !unix.isEmpty() ||
                !linux.isEmpty() ||
                !osx.isEmpty() ||
                !windows.isEmpty();
    }

    public List<String> getResolvedUniversal(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        return universal.stream()
            .map(option -> resolveTemplate(option, props))
            .map(option -> option.replace(lineSeparator(), ""))
            .collect(toList());
    }

    public List<String> getResolvedUnix(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        return unix.stream()
            .map(option -> resolveTemplate(option, props))
            .collect(toList());
    }

    public List<String> getResolvedLinux(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        return linux.stream()
            .map(option -> resolveTemplate(option, props))
            .collect(toList());
    }

    public List<String> getResolvedOsx(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        return osx.stream()
            .map(option -> resolveTemplate(option, props))
            .collect(toList());
    }

    public List<String> getResolvedWindows(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        return windows.stream()
            .map(option -> resolveTemplate(option, props))
            .collect(toList());
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
