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
package org.jreleaser.maven.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Java implements ExtraProperties {
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private String version;
    private String groupId;
    private String artifactId;
    private String mainClass;
    private Boolean multiProject;

    void setAll(Java project) {
        this.version = project.version;
        this.groupId = project.groupId;
        this.artifactId = project.artifactId;
        this.mainClass = project.mainClass;
        this.multiProject = project.multiProject;
        setExtraProperties(project.extraProperties);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
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
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public Boolean isMultiProject() {
        return multiProject != null && multiProject;
    }

    public void setMultiProject(boolean multiProject) {
        this.multiProject = multiProject;
    }

    public boolean isMultiProjectSet() {
        return multiProject != null;
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }
}
