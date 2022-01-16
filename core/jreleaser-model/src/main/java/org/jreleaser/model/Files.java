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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Files implements Domain, Activatable {
    private final Set<Artifact> artifacts = new LinkedHashSet<>();
    private final List<Glob> globs = new ArrayList<>();
    @JsonIgnore
    private final Set<Artifact> paths = new LinkedHashSet<>();
    @JsonIgnore
    private boolean resolved;
    private Active active;
    @JsonIgnore
    private boolean enabled;

    void setAll(Files files) {
        this.active = files.active;
        this.enabled = files.enabled;
        setArtifacts(files.artifacts);
        setGlobs(files.globs);
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
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);

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
