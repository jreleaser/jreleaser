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
package org.jreleaser.model.internal.extensions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.EnabledAware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class Extension extends AbstractModelObject<Extension> implements Domain, EnabledAware {
    private static final long serialVersionUID = 8235578876272898842L;

    private final List<Provider> providers = new ArrayList<>();
    private Boolean enabled;
    private String name;
    private String gav;
    private String directory;

    @JsonIgnore
    private final org.jreleaser.model.api.extensions.Extension immutable = new org.jreleaser.model.api.extensions.Extension() {
        private static final long serialVersionUID = -8554317090414988356L;

        private List<? extends org.jreleaser.model.api.extensions.Extension.Provider> providers;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getGav() {
            return gav;
        }

        @Override
        public String getDirectory() {
            return directory;
        }

        @Override
        public List<? extends org.jreleaser.model.api.extensions.Extension.Provider> getProviders() {
            if (null == providers) {
                providers = Extension.this.providers.stream()
                    .map(Extension.Provider::asImmutable)
                    .collect(toList());
            }
            return providers;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Extension.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return Extension.this.isEnabled();
        }
    };

    public org.jreleaser.model.api.extensions.Extension asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Extension source) {
        this.enabled = merge(this.enabled, source.enabled);
        this.name = merge(this.name, source.name);
        this.gav = merge(this.gav, source.gav);
        this.directory = merge(this.directory, source.directory);
        setProviders(merge(this.providers, source.providers));
    }

    @Override
    public boolean isEnabled() {
        return null != enabled && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return null != enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGav() {
        return gav;
    }

    public void setGav(String gav) {
        this.gav = gav;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public List<Provider> getProviders() {
        return providers;
    }

    public void setProviders(List<Provider> providers) {
        this.providers.clear();
        this.providers.addAll(providers);
    }

    public void addProvider(Provider provider) {
        if (null != provider) {
            this.providers.add(provider);
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("gav", gav);
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


    public static final class Provider extends AbstractModelObject<Provider> implements Domain {
        private static final long serialVersionUID = -6536770909683740039L;

        private final Map<String, Object> properties = new LinkedHashMap<>();
        private String type;

        @JsonIgnore
        private final org.jreleaser.model.api.extensions.Extension.Provider immutable = new org.jreleaser.model.api.extensions.Extension.Provider() {
            private static final long serialVersionUID = 6167354406466230040L;

            @Override
            public String getType() {
                return type;
            }

            @Override
            public Map<String, Object> getProperties() {
                return unmodifiableMap(properties);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Provider.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.extensions.Extension.Provider asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Provider source) {
            this.type = merge(this.type, source.type);
            setProperties(merge(this.properties, source.properties));
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties.clear();
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
