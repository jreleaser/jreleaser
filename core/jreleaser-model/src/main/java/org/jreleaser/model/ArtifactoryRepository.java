/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import org.jreleaser.util.FileType;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.getFilename;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public class ArtifactoryRepository implements Domain, Activatable {
    private final Set<FileType> fileTypes = new LinkedHashSet<>();

    private Active active;
    private boolean enabled;
    private String path;

    void setAll(ArtifactoryRepository repository) {
        this.active = repository.active;
        this.enabled = repository.enabled;
        this.path = repository.path;
        setFileTypes(repository.fileTypes);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            active = Active.RELEASE;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<FileType> getFileTypes() {
        return fileTypes;
    }

    public void setFileTypes(Set<FileType> fileTypes) {
        this.fileTypes.clear();
        this.fileTypes.addAll(fileTypes);
    }

    public void addFileType(FileType fileType) {
        this.fileTypes.add(fileType);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);
        map.put("path", path);
        map.put("fileTypes", fileTypes);
        return map;
    }

    public boolean handles(Artifact artifact) {
        if (!enabled) return false;
        if (fileTypes.isEmpty()) return true;

        String artifactFileName = artifact.getResolvedPath().getFileName().toString();
        String artifactName = getFilename(artifactFileName, FileType.getSupportedExtensions());
        String archiveFormat = artifactFileName.substring(artifactName.length() + 1);
        FileType fileType = FileType.of(archiveFormat);

        return fileTypes.contains(fileType);
    }
}
