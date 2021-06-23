/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class AbstractDockerConfiguration implements DockerConfiguration {
    protected final Map<String, Object> extraProperties = new LinkedHashMap<>();
    protected final Map<String, String> labels = new LinkedHashMap<>();
    protected final Set<String> imageNames = new LinkedHashSet<>();
    protected final List<String> buildArgs = new ArrayList<>();
    protected final List<String> preCommands = new ArrayList<>();
    protected final List<String> postCommands = new ArrayList<>();
    protected final Set<Registry> registries = new LinkedHashSet<>();
    protected boolean enabled;
    protected Active active;
    protected String templateDirectory;

    protected String baseImage;

    void setAll(AbstractDockerConfiguration docker) {
        this.active = docker.active;
        this.enabled = docker.enabled;
        this.templateDirectory = docker.templateDirectory;
        setExtraProperties(docker.extraProperties);
        this.baseImage = docker.baseImage;
        setImageNames(docker.imageNames);
        setBuildArgs(docker.buildArgs);
        setPreCommands(docker.preCommands);
        setPostCommands(docker.postCommands);
        setLabels(docker.labels);
        setRegistries(docker.registries);
    }

    @Override
    public String getPrefix() {
        return NAME;
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

    public boolean resolveEnabled(Project project, Distribution distribution) {
        if (null == active) {
            active = Active.NEVER;
        }
        enabled = active.check(project);
        return enabled;
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
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    @Override
    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
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
        if (imageNames != null) {
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
        if (buildArgs != null) {
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
        if (preCommands != null) {
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
        if (postCommands != null) {
            this.postCommands.clear();
            this.postCommands.addAll(postCommands);
        }
    }

    @Override
    public Set<Registry> getRegistries() {
        return registries;
    }

    @Override
    public void setRegistries(Set<Registry> registries) {
        if (registries != null) {
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
        props.put("active", active);
        props.put("templateDirectory", templateDirectory);
        props.put("baseImage", baseImage);
        props.put("imageNames", imageNames);
        props.put("buildArgs", buildArgs);
        props.put("labels", labels);
        props.put("preCommands", preCommands);
        props.put("postCommands", postCommands);
        asMap(full, props);

        List<Map<String, Object>> repos = this.registries
            .stream()
            .map(r -> r.asMap(full))
            .collect(Collectors.toList());
        if (!repos.isEmpty()) props.put("registries", repos);
        props.put("extraProperties", getResolvedExtraProperties());

        return props;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);
}
