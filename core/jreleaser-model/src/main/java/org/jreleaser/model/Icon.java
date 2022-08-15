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
public class Icon extends AbstractModelObject<Icon> implements Domain, ExtraProperties {
    protected final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private String url;
    private Integer width;
    private Integer height;
    private Boolean primary;

    @Override
    public void merge(Icon source) {
        freezeCheck();
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
        freezeCheck();
        this.url = url;
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

    public boolean isPrimary() {
        return primary != null && primary;
    }

    public void setPrimary(Boolean primary) {
        freezeCheck();
        this.primary = primary;
    }

    @Override
    public String getPrefix() {
        return "icon";
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
        map.put("url", url);
        map.put("width", width);
        map.put("height", height);
        if (isPrimary()) map.put("primary", isPrimary());
        map.put("extraProperties", getResolvedExtraProperties());
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
