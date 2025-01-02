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

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class Icon extends AbstractModelObject<Icon> implements Domain, ExtraProperties {
    private static final long serialVersionUID = -5336402014020023227L;

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private String url;
    private Integer width;
    private Integer height;
    private Boolean primary;

    @JsonIgnore
    private final org.jreleaser.model.api.common.Icon immutable = new org.jreleaser.model.api.common.Icon() {
        private static final long serialVersionUID = 371202655417131552L;

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public Integer getWidth() {
            return width;
        }

        @Override
        public Integer getHeight() {
            return height;
        }

        @Override
        public boolean isPrimary() {
            return Icon.this.isPrimary();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Icon.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return Icon.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public org.jreleaser.model.api.common.Icon asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Icon source) {
        this.url = merge(this.url, source.url);
        this.width = merge(this.width, source.width);
        this.height = merge(this.height, source.height);
        this.primary = merge(this.primary, source.primary);
        setExtraProperties(merge(this.extraProperties, source.extraProperties));
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public boolean isPrimary() {
        return null != primary && primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    @Override
    public String prefix() {
        return "icon";
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("url", url);
        map.put("width", width);
        map.put("height", height);
        if (isPrimary()) map.put("primary", true);
        map.put("extraProperties", getExtraProperties());
        return map;
    }
}
