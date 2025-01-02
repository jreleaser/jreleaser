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
package org.jreleaser.model.internal.announce;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractActivatable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractAnnouncer<S extends AbstractAnnouncer<S, A>, A extends org.jreleaser.model.api.announce.Announcer> extends AbstractActivatable<S> implements Announcer<A> {
    private static final long serialVersionUID = 9045634651862485708L;

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    @JsonIgnore
    private String name;
    private Integer connectTimeout;
    private Integer readTimeout;

    protected AbstractAnnouncer(String name) {
        this.name = name;
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.connectTimeout = merge(this.connectTimeout, source.getConnectTimeout());
        this.readTimeout = merge(this.readTimeout, source.getReadTimeout());
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected boolean isSet() {
        return super.isSet() ||
            null != connectTimeout ||
            null != readTimeout ||
            !extraProperties.isEmpty();
    }

    @Override
    public String prefix() {
        return getName();
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    @Override
    public String getName() {
        return name;
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
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", getActive());
        props.put("connectTimeout", connectTimeout);
        props.put("readTimeout", readTimeout);
        asMap(full, props);
        props.put("extraProperties", getExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getName(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);
}
