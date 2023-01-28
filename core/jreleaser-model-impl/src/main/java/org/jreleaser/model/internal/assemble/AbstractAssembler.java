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
package org.jreleaser.model.internal.assemble;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.platform.Platform;
import org.jreleaser.mustache.TemplateContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_STEREOTYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class AbstractAssembler<S extends AbstractAssembler<S, A>, A extends org.jreleaser.model.api.assemble.Assembler> extends AbstractActivatable<S> implements Assembler<A> {
    private static final long serialVersionUID = 8376910418156286094L;

    @JsonIgnore
    private final Set<Artifact> outputs = new LinkedHashSet<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final List<FileSet> fileSets = new ArrayList<>();
    private final Platform platform = new Platform();
    @JsonIgnore
    private final String type;
    @JsonIgnore
    private String name;

    private Boolean exported;
    private Stereotype stereotype;

    protected AbstractAssembler(String type) {
        this.type = type;
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.exported = merge(this.exported, source.isExported());
        this.name = merge(this.name, source.getName());
        this.platform.merge(source.getPlatform());
        this.stereotype = merge(this.stereotype, source.getStereotype());
        setOutputs(merge(this.outputs, source.getOutputs()));
        setFileSets(merge(this.fileSets, source.getFileSets()));
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
    }

    @Override
    public TemplateContext props() {
        TemplateContext props = new TemplateContext();
        applyTemplates(props, getResolvedExtraProperties());
        props.set(KEY_DISTRIBUTION_NAME, name);
        props.set(KEY_DISTRIBUTION_STEREOTYPE, getStereotype());
        return props;
    }

    @Override
    public Stereotype getStereotype() {
        return stereotype;
    }

    @Override
    public void setStereotype(Stereotype stereotype) {
        this.stereotype = stereotype;
    }

    @Override
    public void setStereotype(String str) {
        setStereotype(Stereotype.of(str));
    }

    @Override
    public String getType() {
        return type;
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
        return null == exported || exported;
    }

    @Override
    public void setExported(Boolean exported) {
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
        props.put("active", getActive());
        props.put("stereotype", stereotype);
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
