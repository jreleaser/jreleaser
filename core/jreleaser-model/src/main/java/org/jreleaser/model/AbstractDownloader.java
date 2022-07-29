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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
abstract class AbstractDownloader<S extends AbstractDownloader<S>> extends AbstractModelObject<S> implements Downloader {
    @JsonIgnore
    protected final String type;
    protected final Map<String, Object> extraProperties = new LinkedHashMap<>();
    protected final List<Asset> assets = new ArrayList<>();
    @JsonIgnore
    protected String name;
    @JsonIgnore
    protected boolean enabled;
    protected Active active;
    protected Integer connectTimeout;
    protected Integer readTimeout;

    protected AbstractDownloader(String type) {
        this.type = type;
    }

    @Override
    public void freeze() {
        super.freeze();
        assets.forEach(Asset::freeze);
    }

    @Override
    public void merge(S downloader) {
        freezeCheck();
        this.name = merge(this.name, downloader.name);
        this.active = merge(this.active, downloader.active);
        this.enabled = merge(this.enabled, downloader.enabled);
        this.connectTimeout = merge(this.connectTimeout, downloader.connectTimeout);
        this.readTimeout = merge(this.readTimeout, downloader.readTimeout);
        setExtraProperties(merge(this.extraProperties, downloader.extraProperties));
        setAssets(merge(this.assets, downloader.assets));
    }

    @Override
    public String getPrefix() {
        return type;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            active = Active.ALWAYS;
        }
        enabled = active.check(project);
        return enabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        freezeCheck();
        this.name = name;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        freezeCheck();
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        setActive(Active.of(str));
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(Integer connectTimeout) {
        freezeCheck();
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Integer getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void setReadTimeout(Integer readTimeout) {
        freezeCheck();
        this.readTimeout = readTimeout;
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
    public List<Asset> getAssets() {
        return freezeWrap(assets);
    }

    @Override
    public void setAssets(List<Asset> assets) {
        freezeCheck();
        this.assets.clear();
        this.assets.addAll(assets);
    }

    @Override
    public void addAsset(Asset asset) {
        freezeCheck();
        if (null != asset) {
            this.assets.add(asset);
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", active);
        props.put("connectTimeout", connectTimeout);
        props.put("readTimeout", readTimeout);
        asMap(props, full);
        Map<String, Map<String, Object>> mappedAssets = new LinkedHashMap<>();
        int i = 0;
        for (Asset asset : getAssets()) {
            mappedAssets.put("asset " + (i++), asset.asMap(full));
        }
        props.put("assets", mappedAssets);
        props.put("extraProperties", getResolvedExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(this.getName(), props);
        return map;
    }

    protected abstract void asMap(Map<String, Object> props, boolean full);
}
