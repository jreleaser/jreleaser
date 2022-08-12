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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class Screenshot extends AbstractModelObject<Screenshot> implements Domain {
    private Type type = Type.SOURCE;
    private Boolean primary;
    private String url;
    private String caption;
    private Integer width;
    private Integer height;

    @Override
    public void merge(Screenshot registry) {
        freezeCheck();
        this.type = merge(this.type, registry.type);
        this.primary = merge(this.primary, registry.primary);
        this.url = merge(this.url, registry.url);
        this.caption = merge(this.caption, registry.caption);
        this.width = merge(this.width, registry.width);
        this.height = merge(this.height, registry.height);
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
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", type);
        map.put("url", url);
        if (isPrimary()) map.put("primary", isPrimary());
        map.put("caption", caption);
        map.put("width", width);
        map.put("height", height);
        return map;
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
}
