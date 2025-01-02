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
import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Executable extends AbstractModelObject<Executable> implements Domain {
    private static final long serialVersionUID = -2611682172967876842L;

    private String name;
    private String unixExtension;
    private String windowsExtension = "bat";

    @JsonIgnore
    private final org.jreleaser.model.api.common.Executable immutable = new org.jreleaser.model.api.common.Executable() {
        private static final long serialVersionUID = 5589040357178439205L;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getUnixExtension() {
            return unixExtension;
        }

        @Override
        public String getWindowsExtension() {
            return windowsExtension;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Executable.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.common.Executable asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Executable source) {
        this.name = this.merge(this.name, source.name);
        this.unixExtension = this.merge(this.unixExtension, source.unixExtension);
        this.windowsExtension = this.merge(this.windowsExtension, source.windowsExtension);
    }

    public String resolveExecutable(String platform) {
        if (PlatformUtils.isWindows(platform)) {
            return name + resolveWindowsExtension();
        }

        return name + resolveUnixExtension();
    }

    public String resolveUnixExtension() {
        return isNotBlank(unixExtension) ? "." + unixExtension : "";
    }

    public String resolveWindowsExtension() {
        return "." + windowsExtension;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnixExtension() {
        return unixExtension;
    }

    public void setUnixExtension(String unixExtension) {
        this.unixExtension = unixExtension;
    }

    public String getWindowsExtension() {
        return windowsExtension;
    }

    public void setWindowsExtension(String windowsExtension) {
        this.windowsExtension = windowsExtension;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("unixExtension", unixExtension);
        map.put("windowsExtension", windowsExtension);
        return map;
    }
}
