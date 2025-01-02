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
 * @since 1.6.0
 */
public abstract class AbstractJibConfiguration<S extends AbstractJibConfiguration<S>> extends AbstractActivatable<S>
    implements JibConfiguration, ExtraProperties {
    private static final long serialVersionUID = 1610025836172412213L;

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Map<String, String> environment = new LinkedHashMap<>();
    private final Map<String, String> labels = new LinkedHashMap<>();
    private final Set<String> imageNames = new LinkedHashSet<>();
    private final Set<String> volumes = new LinkedHashSet<>();
    private final Set<String> exposedPorts = new LinkedHashSet<>();
    private final Set<Registry> registries = new LinkedHashSet<>();
    private final List<String> skipTemplates = new ArrayList<>();

    private String templateDirectory;
    private String baseImage;
    private String creationTime;
    private String workingDirectory;
    private String user;
    private org.jreleaser.model.api.packagers.JibConfiguration.Format format;

    @Override
    public void merge(S source) {
        super.merge(source);
        this.templateDirectory = merge(this.templateDirectory, source.getTemplateDirectory());
        setSkipTemplates(merge(this.skipTemplates, source.getSkipTemplates()));
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
        this.baseImage = merge(this.baseImage, source.getBaseImage());
        this.creationTime = merge(this.creationTime, source.getCreationTime());
        this.workingDirectory = merge(this.workingDirectory, source.getWorkingDirectory());
        this.user = merge(this.user, source.getUser());
        this.format = merge(this.format, source.getFormat());
        setImageNames(merge(this.imageNames, source.getImageNames()));
        setVolumes(merge(this.volumes, source.getVolumes()));
        setExposedPorts(merge(this.exposedPorts, source.getExposedPorts()));
        setEnvironment(merge(this.environment, source.getEnvironment()));
        setLabels(merge(this.labels, source.getLabels()));
        setRegistries(merge(this.registries, source.getRegistries()));
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
    public String getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public org.jreleaser.model.api.packagers.JibConfiguration.Format getFormat() {
        return format;
    }

    @Override
    public void setFormat(org.jreleaser.model.api.packagers.JibConfiguration.Format format) {
        this.format = format;
    }

    @Override
    public void setFormat(String format) {
        this.format = org.jreleaser.model.api.packagers.JibConfiguration.Format.of(format);
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
    public Map<String, String> getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(Map<String, String> environment) {
        this.environment.clear();
        this.environment.putAll(environment);
    }

    @Override
    public void addEnvironment(Map<String, String> environment) {
        this.environment.putAll(environment);
    }

    @Override
    public void addEnvironment(String key, String value) {
        if (isNotBlank(value)) {
            this.environment.put(key, value);
        }
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
    public Set<String> getVolumes() {
        return volumes;
    }

    @Override
    public void setVolumes(Set<String> volumes) {
        if (null != volumes) {
            this.volumes.clear();
            this.volumes.addAll(volumes);
        }
    }

    @Override
    public void addVolumes(Set<String> volumes) {
        if (null != volumes) {
            this.volumes.addAll(volumes);
        }
    }

    @Override
    public void addVolume(String volume) {
        if (null != volume) {
            this.volumes.add(volume);
        }
    }

    @Override
    public Set<String> getExposedPorts() {
        return exposedPorts;
    }

    @Override
    public void setExposedPorts(Set<String> exposedPorts) {
        if (null != exposedPorts) {
            this.exposedPorts.clear();
            this.exposedPorts.addAll(exposedPorts);
        }
    }

    @Override
    public void addExposedPorts(Set<String> exposedPorts) {
        if (null != exposedPorts) {
            this.exposedPorts.addAll(exposedPorts);
        }
    }

    @Override
    public void addExposedPort(String exposedPort) {
        if (null != exposedPort) {
            this.exposedPorts.add(exposedPort);
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
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", getActive());
        props.put("templateDirectory", templateDirectory);
        props.put("skipTemplates", skipTemplates);
        props.put("baseImage", baseImage);
        props.put("imageNames", imageNames);
        props.put("creationTime", creationTime);
        props.put("format", format);
        props.put("workingDirectory", workingDirectory);
        props.put("user", user);
        props.put("environment", environment);
        props.put("labels", labels);
        props.put("volumes", volumes);
        props.put("exposedPorts", exposedPorts);
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
