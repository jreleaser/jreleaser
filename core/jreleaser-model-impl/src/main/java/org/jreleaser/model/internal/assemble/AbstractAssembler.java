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
package org.jreleaser.model.internal.assemble;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.catalog.swid.SwidTag;
import org.jreleaser.model.internal.catalog.swid.SwidTagAware;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;
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
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class AbstractAssembler<S extends AbstractAssembler<S, A>, A extends org.jreleaser.model.api.assemble.Assembler> extends AbstractActivatable<S> implements Assembler<A> {
    private static final long serialVersionUID = -7134019171123897997L;

    @JsonIgnore
    private final Set<Artifact> outputs = new LinkedHashSet<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Set<Artifact> artifacts = new LinkedHashSet<>();
    private final List<Glob> files = new ArrayList<>();
    private final List<FileSet> fileSets = new ArrayList<>();
    private final Platform platform = new Platform();
    private final Set<String> skipTemplates = new LinkedHashSet<>();
    private final SwidTag swid = new SwidTag();
    @JsonIgnore
    private final String type;
    @JsonIgnore
    private String name;

    private String templateDirectory;
    protected Boolean exported;
    private Stereotype stereotype;

    protected AbstractAssembler(String type) {
        this.type = type;
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.exported = merge(this.exported, source.exported);
        this.name = merge(this.name, source.getName());
        this.platform.merge(source.getPlatform());
        this.stereotype = merge(this.stereotype, source.getStereotype());
        this.templateDirectory = merge(this.templateDirectory, source.getTemplateDirectory());
        setSwid(source.getSwid());
        setSkipTemplates(merge(this.skipTemplates, source.getSkipTemplates()));
        setOutputs(merge(this.outputs, source.getOutputs()));
        setArtifacts(merge(this.artifacts, source.getArtifacts()));
        setFileSets(merge(this.fileSets, source.getFileSets()));
        setFiles(merge(this.files, source.getFiles()));
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
    }

    @Override
    public TemplateContext props() {
        TemplateContext props = new TemplateContext();
        applyTemplates(props, resolvedExtraProperties());
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
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    @Override
    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    @Override
    public Set<String> getSkipTemplates() {
        return skipTemplates;
    }

    @Override
    public void setSkipTemplates(Set<String> skipTemplates) {
        this.skipTemplates.clear();
        this.skipTemplates.addAll(skipTemplates);
    }

    @Override
    public void addSkipTemplates(Set<String> templates) {
        this.skipTemplates.addAll(templates);
    }

    @Override
    public void addSkipTemplate(String template) {
        if (isNotBlank(template)) {
            this.skipTemplates.add(template.trim());
        }
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
    public String prefix() {
        return getType();
    }

    @Override
    public Set<Artifact> getArtifacts() {
        return Artifact.sortArtifacts(artifacts);
    }

    @Override
    public void setArtifacts(Set<Artifact> artifacts) {
        this.artifacts.clear();
        this.artifacts.addAll(artifacts);
    }

    @Override
    public void addArtifacts(Set<Artifact> artifacts) {
        this.artifacts.addAll(artifacts);
    }

    @Override
    public void addArtifact(Artifact artifact) {
        if (null != artifact) {
            this.artifacts.add(artifact);
        }
    }

    @Override
    public List<Glob> getFiles() {
        return files;
    }

    @Override
    public void setFiles(List<Glob> files) {
        this.files.clear();
        this.files.addAll(files);
    }

    @Override
    public void addFiles(List<Glob> files) {
        this.files.addAll(files);
    }

    @Override
    public void addFile(Glob file) {
        if (null != file) {
            this.files.add(file);
        }
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
    public SwidTag getSwid() {
        return swid;
    }

    @Override
    public void setSwid(SwidTag swid) {
        this.swid.merge(swid);
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
        props.put("templateDirectory", templateDirectory);
        props.put("skipTemplates", skipTemplates);
        if (this instanceof SwidTagAware) {
            props.put("swid", swid.asMap(full));
        }
        Map<String, Map<String, Object>> mappedArtifacts = new LinkedHashMap<>();
        int i = 0;
        for (Artifact artifact : artifacts) {
            mappedArtifacts.put("artifact " + (i++), artifact.asMap(full));
        }
        props.put("artifacts", mappedArtifacts);
        Map<String, Map<String, Object>> mappedFiles = new LinkedHashMap<>();
        for (i = 0; i < files.size(); i++) {
            mappedFiles.put("file " + i, files.get(i).asMap(full));
        }
        props.put("files", mappedFiles);
        Map<String, Map<String, Object>> mappedFileSets = new LinkedHashMap<>();
        for (i = 0; i < fileSets.size(); i++) {
            mappedFileSets.put("fileSet " + i, fileSets.get(i).asMap(full));
        }
        props.put("fileSets", mappedFileSets);
        props.put("extraProperties", getExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getName(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);
}
