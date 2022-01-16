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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Files implements Activatable{
    private final Set<Artifact> artifacts = new LinkedHashSet<>();
    private final List<Glob> globs = new ArrayList<>();
    private Active active;

    void setAll(Files files) {
        this.active = files.active;
        setArtifacts(files.artifacts);
        setGlobs(files.globs);
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

    public Set<Artifact> getArtifacts() {
        return artifacts;
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
}
