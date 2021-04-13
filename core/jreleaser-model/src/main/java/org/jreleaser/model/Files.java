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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Files implements Domain {
    private final List<Artifact> artifacts = new ArrayList<>();
    private final List<Glob> globs = new ArrayList<>();
    private final Set<Artifact> paths = new LinkedHashSet<>();
    private boolean resolved;

    public boolean isEmpty() {
        return artifacts.isEmpty() && globs.isEmpty();
    }

    public boolean arePathsResolved() {
        return resolved;
    }

    public Set<Artifact> getPaths() {
        return Collections.unmodifiableSet(paths);
    }

    public void setPaths(Set<Artifact> paths) {
        this.paths.clear();
        this.paths.addAll(paths);
        this.resolved = true;
    }

    void setAll(Files files) {
        setArtifacts(files.artifacts);
        setGlobs(files.globs);
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts.clear();
        this.artifacts.addAll(artifacts);
    }

    public void addArtifacts(List<Artifact> artifacts) {
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

        Map<String, Map<String, Object>> mappedArtifacts = new LinkedHashMap<>();
        for (int i = 0; i < artifacts.size(); i++) {
            mappedArtifacts.put("artifact " + i, artifacts.get(i).asMap(full));
        }
        map.put("artifacts", mappedArtifacts);

        Map<String, Map<String, Object>> mappedGlobs = new LinkedHashMap<>();
        for (int i = 0; i < globs.size(); i++) {
            mappedGlobs.put("glob " + i, globs.get(i).asMap(full));
        }
        map.put("globs", mappedGlobs);

        return map;
    }
}
