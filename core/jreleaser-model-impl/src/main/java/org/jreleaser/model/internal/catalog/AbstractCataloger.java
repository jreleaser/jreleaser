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
package org.jreleaser.model.internal.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.ExtraProperties;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public abstract class AbstractCataloger<S extends AbstractCataloger<S, A>, A extends org.jreleaser.model.api.catalog.Cataloger> extends AbstractActivatable<S> implements Cataloger<A>, ExtraProperties {
    private static final long serialVersionUID = -6677881013912645741L;

    @JsonIgnore
    private final String type;
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    protected AbstractCataloger(String type) {
        this.type = type;
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
    }

    @Override
    protected boolean isSet() {
        return super.isSet() ||
            !extraProperties.isEmpty();
    }

    @Override
    public String prefix() {
        return getType();
    }

    @Override
    public boolean isSnapshotSupported() {
        return false;
    }

    @Override
    public String getType() {
        return type;
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
        asMap(full, props);
        props.put("extraProperties", getExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(this.getType(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);
}
