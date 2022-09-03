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
package org.jreleaser.model.internal.packagers;

import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public final class DockerSpec extends AbstractDockerConfiguration<DockerSpec> implements Domain {
    private final Map<String, Object> matchers = new LinkedHashMap<>();
    private Artifact artifact;
    private String name;

    private final org.jreleaser.model.api.packagers.DockerSpec immutable = new org.jreleaser.model.api.packagers.DockerSpec() {
        private Set<? extends org.jreleaser.model.api.packagers.DockerPackager.Registry> registries;

        @Override
        public org.jreleaser.model.api.common.Artifact getArtifact() {
            return artifact.asImmutable();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Map<String, Object> getMatchers() {
            return unmodifiableMap(matchers);
        }

        @Override
        public String getTemplateDirectory() {
            return templateDirectory;
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(skipTemplates);
        }

        @Override
        public String getBaseImage() {
            return baseImage;
        }

        @Override
        public Map<String, String> getLabels() {
            return unmodifiableMap(labels);
        }

        @Override
        public Set<String> getImageNames() {
            return unmodifiableSet(imageNames);
        }

        @Override
        public List<String> getBuildArgs() {
            return unmodifiableList(buildArgs);
        }

        @Override
        public List<String> getPreCommands() {
            return unmodifiableList(preCommands);
        }

        @Override
        public List<String> getPostCommands() {
            return unmodifiableList(postCommands);
        }

        @Override
        public Set<? extends org.jreleaser.model.api.packagers.DockerPackager.Registry> getRegistries() {
            if (null == registries) {
                registries = DockerSpec.this.registries.stream()
                    .map(DockerConfiguration.Registry::asImmutable)
                    .collect(toSet());
            }
            return registries;
        }

        @Override
        public boolean isUseLocalArtifact() {
            return DockerSpec.this.isUseLocalArtifact();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return DockerSpec.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(DockerSpec.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return DockerSpec.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public org.jreleaser.model.api.packagers.DockerSpec asImmutable() {
        return immutable;
    }

    @Override
    public void merge(DockerSpec source) {
        super.merge(source);
        this.name = merge(this.name, source.name);
        this.artifact = source.artifact;
        setMatchers(merge(this.matchers, source.matchers));
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
        this.artifact.activate();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getMatchers() {
        return matchers;
    }

    public void setMatchers(Map<String, Object> matchers) {
        this.matchers.putAll(matchers);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = super.asMap(full);

        if (!props.isEmpty()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(name, props);
            return map;
        }

        return props;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("matchers", matchers);
        if (artifact != null) {
            props.put("artifact", artifact.asMap(full));
        }
    }

    public boolean matches(Artifact artifact) {
        boolean matched = true;

        for (Map.Entry<String, Object> e : matchers.entrySet()) {
            String key = e.getKey();
            if ("platform".equals(key)) {
                matched &= PlatformUtils.isCompatible(String.valueOf(e.getValue()), artifact.getPlatform());
            } else if (artifact.getExtraProperties().containsKey(key)) {
                matched &= e.getValue().equals(artifact.getExtraProperties().get(key));
            }
        }

        return matched;
    }
}
