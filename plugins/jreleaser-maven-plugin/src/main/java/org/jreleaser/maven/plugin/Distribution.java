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
package org.jreleaser.maven.plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Distribution extends Packagers implements ExtraProperties, Activatable {
    private final List<String> tags = new ArrayList<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Set<Artifact> artifacts = new LinkedHashSet<>();
    private final Java java = new Java();
    private final Platform platform = new Platform();
    private final Executable executable = new Executable();

    private String name;
    private DistributionType type = DistributionType.JAVA_BINARY;
    private Active active;

    void setAll(Distribution distribution) {
        super.setAll(distribution);
        this.active = distribution.active;
        this.name = distribution.name;
        this.type = distribution.type;
        setExecutable(distribution.executable);
        this.java.setAll(distribution.java);
        setPlatform(distribution.platform);
        setTags(distribution.tags);
        setExtraProperties(distribution.extraProperties);
        setArtifacts(distribution.artifacts);
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
    public String resolveActive() {
        return active != null ? active.name() : null;
    }

    public DistributionType getType() {
        return type;
    }

    public void setType(DistributionType type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = DistributionType.of(type);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Executable getExecutable() {
        return executable;
    }

    public void setExecutable(Executable executable) {
        this.executable.setAll(executable);
    }

    public void setExecutable(String executable) {
        System.out.println("executable has been deprecated since 1.0.0-M1 and will be removed in the future. Use executable.name instead");
        this.executable.setName(executable);
    }

    public void setExecutableExtension(String executableExtension) {
        System.out.println("executableExtension has been deprecated since 1.0.0-M1 and will be removed in the future. Use executable.windowsExtension instead");
        this.executable.setWindowsExtension(executableExtension);
    }

    public Java getJava() {
        return java;
    }

    public void setJava(Java java) {
        this.java.setAll(java);
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform.setAll(platform);
    }

    public Set<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Set<Artifact> artifacts) {
        this.artifacts.clear();
        this.artifacts.addAll(artifacts);
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
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

    public enum DistributionType {
        BINARY,
        JAVA_BINARY,
        JLINK,
        SINGLE_JAR,
        NATIVE_IMAGE,
        NATIVE_PACKAGE;

        public static DistributionType of(String str) {
            if (isBlank(str)) return null;
            return DistributionType.valueOf(str.replaceAll(" ", "_")
                .replaceAll("-", "_")
                .toUpperCase().trim());
        }
    }

    public static class Executable {
        private String name;
        private String unixExtension;
        private String windowsExtension;

        void setAll(Distribution.Executable executable) {
            this.name = executable.name;
            this.unixExtension = executable.unixExtension;
            this.windowsExtension = executable.windowsExtension;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUnixExtension() {
            return unixExtension;
        }

        public void setUnixExtension(String unixExtension) {
            this.unixExtension = unixExtension;
        }

        public String getWindowsExtension() {
            return windowsExtension;
        }

        public void setWindowsExtension(String windowsExtension) {
            this.windowsExtension = windowsExtension;
        }
    }
}
