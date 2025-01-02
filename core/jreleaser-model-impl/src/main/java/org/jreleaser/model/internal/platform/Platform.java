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
package org.jreleaser.model.internal.platform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public final class Platform extends AbstractModelObject<Platform> implements Domain {
    private static final long serialVersionUID = -5939953296773672903L;

    private final Map<String, String> replacements = new LinkedHashMap<>();

    @JsonIgnore
    private final org.jreleaser.model.api.platform.Platform immutable = new org.jreleaser.model.api.platform.Platform() {
        private static final long serialVersionUID = 7959953437571066163L;

        @Override
        public Map<String, String> getReplacements() {
            return unmodifiableMap(replacements);
        }

        @Override
        public String applyReplacements(String platform) {
            return Platform.this.applyReplacements(platform);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Platform.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.platform.Platform asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Platform source) {
        setReplacements(merge(this.replacements, source.replacements));
    }

    public boolean isSet() {
        return !replacements.isEmpty();
    }

    public Map<String, String> getReplacements() {
        return replacements;
    }

    public void setReplacements(Map<String, String> replacements) {
        this.replacements.putAll(replacements);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("replacements", replacements);
        return map;
    }

    public String applyReplacements(String platform) {
        if (isBlank(platform)) return platform;

        for (Map.Entry<String, String> e : replacements.entrySet()) {
            if (e.getKey().equals(platform)) {
                return e.getValue();
            }
        }

        String[] parts = platform.split("-");
        for (int i = 0; i < parts.length; i++) {
            for (Map.Entry<String, String> e : replacements.entrySet()) {
                if (e.getKey().equals(parts[i])) {
                    parts[i] = e.getValue();
                }
            }
        }

        return String.join("-", parts);
    }

    public Platform mergeValues(Platform other) {
        Platform merged = new Platform();

        Map<String, String> full = new LinkedHashMap<>();
        Map<String, String> partial = new LinkedHashMap<>();

        for (Map.Entry<String, String> e : replacements.entrySet()) {
            if (e.getKey().contains("-")) {
                full.put(e.getKey(), e.getValue());
            } else {
                partial.put(e.getKey(), e.getValue());
            }
        }

        for (Map.Entry<String, String> e : other.replacements.entrySet()) {
            if (e.getKey().contains("-")) {
                full.put(e.getKey(), e.getValue());
            } else {
                partial.put(e.getKey(), e.getValue());
            }
        }

        merged.replacements.putAll(full);
        merged.replacements.putAll(partial);

        return merged;
    }
}
