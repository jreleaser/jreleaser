/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.model;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public class Platform implements Domain {
    private final Map<String, String> replacements = new LinkedHashMap<>();

    void setAll(Platform platform) {
        setReplacements(platform.replacements);
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

    public Platform merge(Platform other) {
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
