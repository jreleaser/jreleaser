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
package org.jreleaser.model.internal.packagers;

import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.ExtraProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class AbstractDockerConfiguration<S extends AbstractDockerConfiguration<S>> extends AbstractActivatable<S>
    implements DockerConfiguration, ExtraProperties {
    private static final long serialVersionUID = 9145623803421730899L;

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Map<String, String> labels = new LinkedHashMap<>();
    private final Set<String> imageNames = new LinkedHashSet<>();
    private final List<String> buildArgs = new ArrayList<>();
    private final List<String> preCommands = new ArrayList<>();
    private final List<String> postCommands = new ArrayList<>();
    private final Set<Registry> registries = new LinkedHashSet<>();
    private final List<String> skipTemplates = new ArrayList<>();
    private final Buildx buildx = new Buildx();

    private String templateDirectory;
    protected Boolean useLocalArtifact;
    private String baseImage;

    @Override
    public void merge(S source) {
        super.merge(source);
        this.templateDirectory = merge(this.templateDirectory, source.getTemplateDirectory());
        setSkipTemplates(merge(this.skipTemplates, source.getSkipTemplates()));
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
        this.baseImage = merge(this.baseImage, source.getBaseImage());
        this.useLocalArtifact = merge(this.useLocalArtifact, source.useLocalArtifact);
        setImageNames(merge(this.imageNames, source.getImageNames()));
        setBuildArgs(merge(this.buildArgs, source.getBuildArgs()));
        setPreCommands(merge(this.preCommands, source.getPreCommands()));
        setPostCommands(merge(this.postCommands, source.getPostCommands()));
        setLabels(merge(this.labels, source.getLabels()));
        setRegistries(merge(this.registries, source.getRegistries()));
        setBuildx(source.getBuildx());
    }

    @Override
    public String prefix() {
        return TYPE;
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
    public List<String> getSkipTemplates() {
        return skipTemplates;
    }

    @Override
    public void setSkipTemplates(List<String> skipTemplates) {
        this.skipTemplates.clear();
        this.skipTemplates.addAll(skipTemplates);
    }

    @Override
    public void addSkipTemplates(List<String> templates) {
        this.skipTemplates.addAll(templates);
    }

    @Override
    public void addSkipTemplate(String template) {
        if (isNotBlank(template)) {
            this.skipTemplates.add(template.trim());
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
    public String getBaseImage() {
        return baseImage;
    }

    @Override
    public void setBaseImage(String baseImage) {
        this.baseImage = baseImage;
    }

    @Override
    public Map<String, String> getLabels() {
        return labels;
    }

    @Override
    public void setLabels(Map<String, String> labels) {
        this.labels.clear();
        this.labels.putAll(labels);
    }

    @Override
    public void addLabels(Map<String, String> labels) {
        this.labels.putAll(labels);
    }

    @Override
    public void addLabel(String key, String value) {
        if (isNotBlank(value)) {
            this.labels.put(key, value);
        }
    }

    @Override
    public Set<String> getImageNames() {
        return imageNames;
    }

    @Override
    public void setImageNames(Set<String> imageNames) {
        if (null != imageNames) {
            this.imageNames.clear();
            this.imageNames.addAll(imageNames);
        }
    }

    @Override
    public void addImageName(String imageName) {
        if (isNotBlank(imageName)) {
            this.imageNames.add(imageName);
        }
    }

    @Override
    public List<String> getBuildArgs() {
        return buildArgs;
    }

    @Override
    public void setBuildArgs(List<String> buildArgs) {
        if (null != buildArgs) {
            this.buildArgs.clear();
            this.buildArgs.addAll(buildArgs);
        }
    }

    @Override
    public void addBuildArg(String buildArg) {
        if (isNotBlank(buildArg)) {
            this.buildArgs.add(buildArg);
        }
    }

    @Override
    public List<String> getPreCommands() {
        return preCommands;
    }

    @Override
    public void setPreCommands(List<String> preCommands) {
        if (null != preCommands) {
            this.preCommands.clear();
            this.preCommands.addAll(preCommands);
        }
    }

    @Override
    public List<String> getPostCommands() {
        return postCommands;
    }

    @Override
    public void setPostCommands(List<String> postCommands) {
        if (null != postCommands) {
            this.postCommands.clear();
            this.postCommands.addAll(postCommands);
        }
    }

    @Override
    public Set<Registry> getRegistries() {
        return registries;
    }

    @Override
    public void setRegistries(Set<? extends Registry> registries) {
        if (null != registries) {
            this.registries.clear();
            this.registries.addAll(registries);
        }
    }

    @Override
    public void addRegistry(Registry registry) {
        if (null != registry) {
            this.registries.add(registry);
        }
    }

    @Override
    public boolean isUseLocalArtifact() {
        return null == useLocalArtifact || useLocalArtifact;
    }

    @Override
    public void setUseLocalArtifact(Boolean useLocalArtifact) {
        this.useLocalArtifact = useLocalArtifact;
    }

    @Override
    public boolean isUseLocalArtifactSet() {
        return null != useLocalArtifact;
    }

    @Override
    public Buildx getBuildx() {
        return buildx;
    }

    @Override
    public void setBuildx(Buildx buildx) {
        this.buildx.merge(buildx);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", getActive());
        props.put("templateDirectory", templateDirectory);
        props.put("skipTemplates", skipTemplates);
        props.put("useLocalArtifact", isUseLocalArtifact());
        props.put("baseImage", baseImage);
        props.put("imageNames", imageNames);
        props.put("buildArgs", buildArgs);
        props.put("labels", labels);
        props.put("preCommands", preCommands);
        props.put("postCommands", postCommands);
        if (buildx.isEnabled() || full) props.put("buildx", buildx.asMap(full));
        asMap(full, props);

        Map<String, Map<String, Object>> m = new LinkedHashMap<>();
        int i = 0;
        for (Registry registry : this.registries) {
            m.put("registry " + (i++), registry.asMap(full));
        }
        props.put("registries", m);

        props.put("extraProperties", getExtraProperties());

        return props;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);
}
