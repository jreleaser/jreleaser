/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.model.internal.download;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.project.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class AbstractDownloader<A extends org.jreleaser.model.api.download.Downloader, S extends AbstractDownloader<A, S>> extends AbstractModelObject<S> implements Downloader<A> {
    private static final long serialVersionUID = 1444643272006843006L;

    @JsonIgnore
    private final String type;
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final List<Asset> assets = new ArrayList<>();
    @JsonIgnore
    private String name;
    @JsonIgnore
    private boolean enabled;
    private Active active;
    private Integer connectTimeout;
    private Integer readTimeout;

    protected AbstractDownloader(String type) {
        this.type = type;
    }

    @Override
    public void merge(S source) {
        this.name = merge(this.name, source.getName());
        this.active = merge(this.active, source.getActive());
        this.enabled = merge(this.enabled, source.isEnabled());
        this.connectTimeout = merge(this.connectTimeout, source.getConnectTimeout());
        this.readTimeout = merge(this.readTimeout, source.getReadTimeout());
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
        setAssets(merge(this.assets, source.getAssets()));
    }

    @Override
    public String getPrefix() {
        return getType();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        enabled = null != active && active.check(project);
        return enabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        setActive(Active.of(str));
    }

    @Override
    public boolean isActiveSet() {
        return null != active;
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
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Integer getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
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
    public List<Asset> getAssets() {
        return assets;
    }

    @Override
    public void setAssets(List<Asset> assets) {
        this.assets.clear();
        this.assets.addAll(assets);
    }

    @Override
    public void addAsset(Asset asset) {
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
        asMap(full, props);
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

    protected abstract void asMap(boolean full, Map<String, Object> props);
}
