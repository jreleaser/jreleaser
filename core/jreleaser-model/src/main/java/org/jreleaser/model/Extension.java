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
package org.jreleaser.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class Extension extends AbstractModelObject<Extension> implements Domain, EnabledAware {
    private final List<Provider> providers = new ArrayList<>();
    private Boolean enabled;
    private String name;
    private String directory;

    @Override
    public void freeze() {
        super.freeze();
        providers.forEach(ModelObject::freeze);
    }

    @Override
    public void merge(Extension source) {
        freezeCheck();
        this.enabled = merge(this.enabled, source.enabled);
        this.name = merge(this.name, source.name);
        this.directory = merge(this.directory, source.directory);
        setProviders(merge(this.providers, source.providers));
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        freezeCheck();
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    public String getName() {
        freezeCheck();
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        freezeCheck();
        this.directory = directory;
    }

    public List<Provider> getProviders() {
        return freezeWrap(providers);
    }

    public void setProviders(List<Provider> providers) {
        freezeCheck();
        this.providers.clear();
        this.providers.addAll(providers);
    }

    public void addProvider(Provider provider) {
        freezeCheck();
        if (null != provider) {
            this.providers.add(provider);
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("directory", directory);
        Map<String, Map<String, Object>> m = new LinkedHashMap<>();
        for (int i = 0; i < providers.size(); i++) {
            m.put("provider " + i, providers.get(i).asMap(full));
        }
        props.put("providers", m);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(name, props);
        return map;
    }

    public static class Provider extends AbstractModelObject<Provider> implements Domain {
        private final Map<String, Object> properties = new LinkedHashMap<>();
        private String type;

        @Override
        public void merge(Provider source) {
            freezeCheck();
            this.type = merge(this.type, source.type);
            setProperties(merge(this.properties, source.properties));
        }

        public String getType() {
            freezeCheck();
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getProperties() {
            return freezeWrap(properties);
        }

        public void setProperties(Map<String, Object> properties) {
            freezeCheck();
            this.properties.clear();
            this.properties.putAll(properties);
        }

        public void addProperties(Map<String, Object> properties) {
            freezeCheck();
            this.properties.putAll(properties);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", type);
            map.put("properties", getProperties());
            return map;
        }
    }
}
