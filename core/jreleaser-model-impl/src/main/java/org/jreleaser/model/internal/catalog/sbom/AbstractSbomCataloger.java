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
package org.jreleaser.model.internal.catalog.sbom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.ExtraProperties;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public abstract class AbstractSbomCataloger<S extends AbstractSbomCataloger<S, A>, A extends org.jreleaser.model.api.catalog.sbom.SbomCataloger> extends AbstractActivatable<S> implements SbomCataloger<A>, ExtraProperties {
    private static final long serialVersionUID = -8115903657059268124L;

    @JsonIgnore
    private final String type;
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private final Pack pack = new Pack();
    protected Boolean distributions;
    protected Boolean files;

    protected AbstractSbomCataloger(String type) {
        this.type = type;
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.distributions = merge(this.distributions, source.distributions);
        this.files = merge(this.files, source.files);
        setPack(source.getPack());
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
    }

    @Override
    protected boolean isSet() {
        return super.isSet() ||
            null != distributions ||
            null != files ||
            pack.isSet() ||
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
    public boolean isDistributions() {
        return null != distributions && distributions;
    }

    @Override
    public void setDistributions(Boolean distributions) {
        this.distributions = distributions;
    }

    @Override
    public boolean isDistributionsSet() {
        return null != distributions;
    }

    @Override
    public boolean isFiles() {
        return null != files && files;
    }

    @Override
    public void setFiles(Boolean files) {
        this.files = files;
    }

    @Override
    public boolean isFilesSet() {
        return null != files;
    }

    @Override
    public Pack getPack() {
        return pack;
    }

    @Override
    public void setPack(Pack pack) {
        this.pack.merge(pack);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", getActive());
        props.put("distributions", isDistributions());
        props.put("files", isFiles());
        props.put("pack", pack.asMap(full));
        asMap(full, props);
        props.put("extraProperties", getExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(this.getType(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);
}
