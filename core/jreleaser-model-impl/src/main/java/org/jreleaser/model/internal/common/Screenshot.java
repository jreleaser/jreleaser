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
import com.github.mustachejava.TemplateFunction;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class Screenshot extends AbstractModelObject<Screenshot> implements Domain, ExtraProperties {
    private static final long serialVersionUID = 7278270297286736205L;

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private org.jreleaser.model.Screenshot.Type type = org.jreleaser.model.Screenshot.Type.SOURCE;
    private Boolean primary;
    private String url;
    private String caption;
    private Integer width;
    private Integer height;

    @JsonIgnore
    private final org.jreleaser.model.api.common.Screenshot immutable = new org.jreleaser.model.api.common.Screenshot() {
        private static final long serialVersionUID = 3229726441750227017L;

        @Override
        public org.jreleaser.model.Screenshot.Type getType() {
            return type;
        }

        @Override
        public boolean isPrimary() {
            return Screenshot.this.isPrimary();
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getCaption() {
            return caption;
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
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Screenshot.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return Screenshot.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public org.jreleaser.model.api.common.Screenshot asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Screenshot source) {
        this.type = merge(this.type, source.type);
        this.primary = merge(this.primary, source.primary);
        this.url = merge(this.url, source.url);
        this.caption = merge(this.caption, source.caption);
        this.width = merge(this.width, source.width);
        this.height = merge(this.height, source.height);
        setExtraProperties(merge(this.extraProperties, source.extraProperties));
    }

    public org.jreleaser.model.Screenshot.Type getType() {
        return type;
    }

    public void setType(org.jreleaser.model.Screenshot.Type type) {
        this.type = type;
    }

    public void setType(String str) {
        setType(org.jreleaser.model.Screenshot.Type.of(str));
    }

    public boolean isPrimary() {
        return null != primary && primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
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

    @Override
    public String prefix() {
        return "screenshot";
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
        map.put("type", type);
        map.put("url", url);
        if (isPrimary()) map.put("primary", isPrimary());
        map.put("caption", caption);
        map.put("width", width);
        map.put("height", height);
        map.put("extraProperties", getExtraProperties());
        return map;
    }

    public ScreenshotTemplate asScreenshotTemplate() {
        return new ScreenshotTemplate(this);
    }

    public static final class ScreenshotTemplate {
        private final String type;
        private final boolean primary;
        private final String url;
        private final String caption;
        private final Integer width;
        private final Integer height;

        public ScreenshotTemplate(Screenshot source) {
            this.type = source.type.formatted();
            this.primary = source.isPrimary();
            this.url = source.url;
            this.caption = source.caption;
            this.width = source.width;
            this.height = source.height;
        }

        public String getType() {
            return type;
        }

        public TemplateFunction getUrl() {
            return s -> url;
        }

        public boolean isPrimary() {
            return primary;
        }

        public String getCaption() {
            return caption;
        }

        public Integer getWidth() {
            return width;
        }

        public Integer getHeight() {
            return height;
        }
    }
}
