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
package org.jreleaser.model.internal.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Java extends AbstractModelObject<Java> implements Domain, ExtraProperties, EnabledAware {
    private static final long serialVersionUID = -2234061310893799176L;

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private Boolean enabled;
    private String version;
    private String groupId;
    private String artifactId;
    private String mainModule;
    private String mainClass;
    private Boolean multiProject;

    @JsonIgnore
    private final org.jreleaser.model.api.common.Java immutable = new org.jreleaser.model.api.common.Java() {
        private static final long serialVersionUID = 1595567967292822458L;

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String getGroupId() {
            return groupId;
        }

        @Override
        public String getArtifactId() {
            return artifactId;
        }

        @Override
        public boolean isMultiProject() {
            return Java.this.isMultiProject();
        }

        @Override
        public String getMainClass() {
            return mainClass;
        }

        @Override
        public String getMainModule() {
            return mainModule;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Java.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return Java.this.isEnabled();
        }

        @Override
        public String getPrefix() {
            return Java.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public org.jreleaser.model.api.common.Java asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Java source) {
        this.enabled = merge(this.enabled, source.enabled);
        this.version = merge(this.version, source.version);
        this.groupId = merge(this.groupId, source.groupId);
        this.artifactId = merge(this.artifactId, source.artifactId);
        this.mainModule = merge(this.mainModule, source.mainModule);
        this.mainClass = merge(this.mainClass, source.mainClass);
        this.multiProject = merge(this.multiProject, source.multiProject);
        setExtraProperties(merge(this.extraProperties, source.extraProperties));
    }

    @Override
    public boolean isEnabled() {
        return null != enabled && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return null != enabled;
    }

    @Override
    public String prefix() {
        return "java";
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

    public boolean isMultiProject() {
        return null != multiProject && multiProject;
    }

    public void setMultiProject(Boolean multiProject) {
        this.multiProject = multiProject;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMainModule() {
        return mainModule;
    }

    public void setMainModule(String mainModule) {
        this.mainModule = mainModule;
    }

    public boolean isMultiProjectSet() {
        return null != multiProject;
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
        map.put("extraProperties", getExtraProperties());
        return map;
    }
}
