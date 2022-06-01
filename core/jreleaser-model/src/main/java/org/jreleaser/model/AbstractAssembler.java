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
import org.jreleaser.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.MustacheUtils.applyTemplates;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
abstract class AbstractAssembler<S extends AbstractAssembler<S>> extends AbstractModelObject<S> implements Assembler {
    @JsonIgnore
    protected final Set<Artifact> outputs = new LinkedHashSet<>();
    protected final Map<String, Object> extraProperties = new LinkedHashMap<>();
    protected final List<FileSet> fileSets = new ArrayList<>();
    protected final Platform platform = new Platform();
    @JsonIgnore
    protected final String type;
    @JsonIgnore
    protected String name;
    @JsonIgnore
    protected boolean enabled;
    protected Active active;
    protected Boolean exported;

    protected AbstractAssembler(String type) {
        this.type = type;
    }

    @Override
    public void merge(S assembler) {
        this.active = merge(this.active, assembler.active);
        this.enabled = merge(this.enabled, assembler.enabled);
        this.exported = merge(this.exported, assembler.exported);
        this.name = merge(this.name, assembler.name);
        this.platform.merge(assembler.platform);
        setOutputs(merge(this.outputs, assembler.outputs));
        setFileSets(merge(this.fileSets, assembler.fileSets));
        setExtraProperties(merge(this.extraProperties, assembler.extraProperties));
    }

    @Override
    public Map<String, Object> props() {
        Map<String, Object> props = new LinkedHashMap<>();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(Constants.KEY_DISTRIBUTION_NAME, name);
        return props;
    }

    @Override
    public String getType() {
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
            active = Active.NEVER;
        }
        enabled = active.check(project);
        return enabled;
    }

    @Override
    public Platform getPlatform() {
        return platform;
    }

    @Override
    public void setPlatform(Platform platform) {
        this.platform.merge(platform);
    }

    @Override
    public boolean isExported() {
        return exported == null || exported;
    }

    @Override
    public void setExported(boolean exported) {
        this.exported = exported;
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
        this.active = Active.of(str);
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    @Override
    public Set<Artifact> getOutputs() {
        return Artifact.sortArtifacts(outputs);
    }

    @Override
    public void setOutputs(Set<Artifact> output) {
        this.outputs.clear();
        this.outputs.addAll(output);
    }

    @Override
    public void addOutput(Artifact artifact) {
        if (null != artifact) {
            this.outputs.add(artifact);
        }
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
    public String getPrefix() {
        return getType();
    }

    @Override
    public List<FileSet> getFileSets() {
        return fileSets;
    }

    @Override
    public void setFileSets(List<FileSet> fileSets) {
        this.fileSets.clear();
        this.fileSets.addAll(fileSets);
    }

    @Override
    public void addFileSets(List<FileSet> files) {
        this.fileSets.addAll(files);
    }

    @Override
    public void addFileSet(FileSet file) {
        if (null != file) {
            this.fileSets.add(file);
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("exported", isExported());
        props.put("active", active);
        if (full || platform.isSet()) props.put("platform", platform.asMap(full));
        asMap(full, props);
        Map<String, Map<String, Object>> mappedFileSets = new LinkedHashMap<>();
        for (int i = 0; i < fileSets.size(); i++) {
            mappedFileSets.put("fileSet " + i, fileSets.get(i).asMap(full));
        }
        props.put("fileSets", mappedFileSets);
        props.put("extraProperties", getResolvedExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getName(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);
}
