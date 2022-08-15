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

import com.github.mustachejava.TemplateFunction;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class Screenshot extends AbstractModelObject<Screenshot> implements Domain, ExtraProperties {
    protected final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private Type type = Type.SOURCE;
    private Boolean primary;
    private String url;
    private String caption;
    private Integer width;
    private Integer height;

    @Override
    public void merge(Screenshot source) {
        freezeCheck();
        this.type = merge(this.type, source.type);
        this.primary = merge(this.primary, source.primary);
        this.url = merge(this.url, source.url);
        this.caption = merge(this.caption, source.caption);
        this.width = merge(this.width, source.width);
        this.height = merge(this.height, source.height);
        setExtraProperties(merge(this.extraProperties, source.extraProperties));
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        freezeCheck();
        this.type = type;
    }

    public void setType(String str) {
        setType(Type.of(str));
    }

    public boolean isPrimary() {
        return primary != null && primary;
    }

    public void setPrimary(Boolean primary) {
        freezeCheck();
        this.primary = primary;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        freezeCheck();
        this.url = url;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        freezeCheck();
        this.caption = caption;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        freezeCheck();
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        freezeCheck();
        this.height = height;
    }

    @Override
    public String getPrefix() {
        return "screenshot";
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return freezeWrap(extraProperties);
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        freezeCheck();
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        freezeCheck();
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
        map.put("extraProperties", getResolvedExtraProperties());
        return map;
    }

    public ScreenshotTemplate asScreenshotTemplate() {
        return new ScreenshotTemplate(this);
    }

    public enum Type {
        SOURCE,
        THUMBNAIL;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Type of(String str) {
            if (isBlank(str)) return null;
            return Type.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
        }
    }

    public static final class ScreenshotTemplate {
        private final String type;
        private final boolean primary;
        private final String url;
        private final String caption;
        private final Integer width;
        private final Integer height;

        public ScreenshotTemplate(Screenshot source) {
            this.type = source.type.toString();
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
            return (s) -> url;
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
