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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Java extends AbstractModelObject<Java> implements Domain, ExtraProperties, EnabledAware {
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    protected Boolean enabled;
    private String version;
    private String groupId;
    private String artifactId;
    private String mainModule;
    private String mainClass;
    private Boolean multiProject;

    @Override
    public void merge(Java java) {
        freezeCheck();
        this.enabled = merge(this.enabled, java.enabled);
        this.version = merge(this.version, java.version);
        this.groupId = merge(this.groupId, java.groupId);
        this.artifactId = merge(this.artifactId, java.artifactId);
        this.mainModule = merge(this.mainModule, java.mainModule);
        this.mainClass = merge(this.mainClass, java.mainClass);
        this.multiProject = merge(this.multiProject, java.multiProject);
        setExtraProperties(merge(this.extraProperties, java.extraProperties));
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        freezeCheck();
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    @Override
    public String getPrefix() {
        return "java";
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        freezeCheck();
        if (isNotBlank(version) && version.startsWith("1.8")) {
            this.version = "8";
        } else {
            this.version = version;
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        freezeCheck();
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        freezeCheck();
        this.artifactId = artifactId;
    }

    public boolean isMultiProject() {
        return multiProject != null && multiProject;
    }

    public void setMultiProject(Boolean multiProject) {
        freezeCheck();
        this.multiProject = multiProject;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        freezeCheck();
        this.mainClass = mainClass;
    }

    public String getMainModule() {
        return mainModule;
    }

    public void setMainModule(String mainModule) {
        freezeCheck();
        this.mainModule = mainModule;
    }

    public boolean isMultiProjectSet() {
        return multiProject != null;
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return freezeWrap(extraProperties);
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        freezeCheck();
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        freezeCheck();
        this.extraProperties.putAll(extraProperties);
    }

    public boolean isSet() {
        return isEnabledSet() ||
            isNotBlank(version) ||
            isNotBlank(groupId) ||
            isNotBlank(artifactId) ||
            isNotBlank(mainModule) ||
            isNotBlank(mainClass) ||
            isMultiProjectSet() ||
            !extraProperties.isEmpty();
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("version", version);
        map.put("groupId", groupId);
        map.put("artifactId", artifactId);
        map.put("mainModule", mainModule);
        map.put("mainClass", mainClass);
        map.put("multiProject", isMultiProject());
        map.put("extraProperties", getResolvedExtraProperties());
        return map;
    }
}
