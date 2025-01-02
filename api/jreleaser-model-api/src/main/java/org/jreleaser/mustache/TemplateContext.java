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
package org.jreleaser.mustache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class TemplateContext {
    private final Map<String, Object> properties = new LinkedHashMap<>();

    public static TemplateContext empty() {
        return new TemplateContext();
    }

    public static TemplateContext from(Map<String, Object> props) {
        if (props != null) {
            return new TemplateContext(props);
        }
        return new TemplateContext();
    }

    public TemplateContext() {
    }

    public TemplateContext(Map<String, Object> props) {
        this.properties.putAll(props);
    }

    public TemplateContext(TemplateContext other) {
        setAll(other);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public Set<String> keys() {
        return properties.keySet();
    }

    public Set<Map.Entry<String, Object>> entries() {
        return properties.entrySet();
    }

    public boolean contains(String key) {
        return properties.containsKey(key);
    }

    public <V> V get(String key) {
        return (V) properties.get(key);
    }

    public TemplateContext setAll(TemplateContext other) {
        if (null != other) properties.putAll(other.properties);
        return this;
    }

    public <V> TemplateContext setAll(Map<String, V> props) {
        if (null == props || props.isEmpty()) return this;
        props.forEach(this::set);
        return this;
    }

    public <V> V set(String key, V value) {
        V previousValue = (V) properties.get(key);
        if (null != value) properties.put(key, value);
        return previousValue;
    }

    public <V> V set(String key, V value, V defaultValue) {
        V previousValue = (V) properties.get(key);
        if (null != value && null != defaultValue) properties.put(key, value);
        return previousValue;
    }

    public <V> V remove(String key) {
        return (V) properties.remove(key);
    }

    public Map<String, Object> asMap() {
        return properties;
    }
}
