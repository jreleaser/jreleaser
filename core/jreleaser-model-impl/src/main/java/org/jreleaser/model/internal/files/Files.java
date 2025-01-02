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
package org.jreleaser.model.internal.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.Glob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Files extends AbstractActivatable<Files> implements Domain {
    private static final long serialVersionUID = -7799032884331569570L;

    private final Set<Artifact> artifacts = new LinkedHashSet<>();
    private final List<Glob> globs = new ArrayList<>();
    @JsonIgnore
    private final Set<Artifact> paths = new LinkedHashSet<>();
    @JsonIgnore
    private boolean resolved;

    @JsonIgnore
    private final org.jreleaser.model.api.files.Files immutable = new org.jreleaser.model.api.files.Files() {
        private static final long serialVersionUID = -328612924170955820L;

        private Set<? extends org.jreleaser.model.api.common.Artifact> paths;
        private Set<? extends org.jreleaser.model.api.common.Artifact> artifacts;
        private List<? extends org.jreleaser.model.api.common.Glob> globs;

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getPaths() {
            if (null == paths) {
                paths = Files.this.paths.stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return paths;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getArtifacts() {
            if (null == artifacts) {
                artifacts = Files.this.artifacts.stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return artifacts;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getGlobs() {
            if (null == globs) {
                globs = Files.this.globs.stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return globs;
        }

        @Override
        public Active getActive() {
            return Files.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Files.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Files.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.files.Files asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Files source) {
        super.merge(source);
        setArtifacts(merge(this.artifacts, source.artifacts));
        setGlobs(merge(this.globs, source.globs));
    }

    public boolean isEmpty() {
        return artifacts.isEmpty() && globs.isEmpty();
    }

    public boolean arePathsResolved() {
        return resolved;
    }

    public Set<Artifact> getPaths() {
        return Artifact.sortArtifacts(paths);
    }

    public void setPaths(Set<Artifact> paths) {
        this.paths.clear();
        this.paths.addAll(paths);
        this.resolved = true;
    }

    public Set<Artifact> getArtifacts() {
        return Artifact.sortArtifacts(artifacts);
    }

    public void setArtifacts(Set<Artifact> artifacts) {
        this.artifacts.clear();
        this.artifacts.addAll(artifacts);
    }

    public void addArtifacts(Set<Artifact> artifacts) {
        this.artifacts.addAll(artifacts);
    }

    public void addArtifact(Artifact artifact) {
        if (null != artifact) {
            this.artifacts.add(artifact);
        }
    }

    public List<Glob> getGlobs() {
        return globs;
    }

    public void setGlobs(List<Glob> globs) {
        this.globs.clear();
        this.globs.addAll(globs);
    }

    public void addGlobs(List<Glob> globs) {
        this.globs.addAll(globs);
    }

    public void addGlob(Glob glob) {
        if (null != glob) {
            this.globs.add(glob);
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());

        Map<String, Map<String, Object>> mappedArtifacts = new LinkedHashMap<>();
        int i = 0;
        for (Artifact artifact : getArtifacts()) {
            mappedArtifacts.put("artifact " + (i++), artifact.asMap(full));
        }
        map.put("artifacts", mappedArtifacts);

        Map<String, Map<String, Object>> mappedGlobs = new LinkedHashMap<>();
        for (i = 0; i < globs.size(); i++) {
            mappedGlobs.put("glob " + i, globs.get(i).asMap(full));
        }
        map.put("globs", mappedGlobs);

        return map;
    }
}
