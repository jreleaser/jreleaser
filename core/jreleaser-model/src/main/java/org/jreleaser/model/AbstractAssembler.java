/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.util.Constants;
import org.jreleaser.util.Version;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.CollectionUtils.safePut;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
abstract class AbstractAssembler implements Assembler {
    protected final Set<Artifact> output = new LinkedHashSet<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Java java = new Java();
    private final String type;
    protected String name;
    protected boolean enabled;
    protected Active active;
    protected String executable;
    protected String templateDirectory;

    protected AbstractAssembler(String type) {
        this.type = type;
    }

    void setAll(AbstractAssembler assembler) {
        this.active = assembler.active;
        this.enabled = assembler.enabled;
        this.name = assembler.name;
        this.executable = assembler.executable;
        this.templateDirectory = assembler.templateDirectory;
        setOutputs(assembler.output);
        setJava(assembler.java);
        setExtraProperties(assembler.extraProperties);
    }

    @Override
    public Map<String, Object> props() {
        Map<String, Object> props = new LinkedHashMap<>(getResolvedExtraProperties());
        props.put(Constants.KEY_DISTRIBUTION_NAME, name);
        props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, executable);
        props.putAll(java.getResolvedExtraProperties());
        safePut(Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID, java.getGroupId(), props, true);
        safePut(Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, java.getArtifactId(), props, true);
        safePut(Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS, java.getMainClass(), props, true);
        if (isNotBlank(java.getVersion())) {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, java.getVersion());
            Version jv = Version.of(java.getVersion());
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, jv.getMajor(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, jv.getMinor(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, jv.getPatch(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, jv.getTag(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, jv.getBuild(), props, true);
        } else {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, "");
        }
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
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getExecutable() {
        return executable;
    }

    @Override
    public void setExecutable(String executable) {
        this.executable = executable;
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
        return output;
    }

    @Override
    public void setOutputs(Set<Artifact> output) {
        this.output.clear();
        this.output.addAll(output);
    }

    @Override
    public void addOutput(Artifact artifact) {
        if (null != artifact) {
            this.output.add(artifact);
        }
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public Java getJava() {
        return java;
    }

    @Override
    public void setJava(Java java) {
        this.java.setAll(java);
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
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", active);
        props.put("executable", executable);
        props.put("templateDirectory", templateDirectory);
        asMap(full, props);
        props.put("extraProperties", getResolvedExtraProperties());
        if (java.isEnabled()) {
            props.put("java", java.asMap(full));
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getName(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);
}
