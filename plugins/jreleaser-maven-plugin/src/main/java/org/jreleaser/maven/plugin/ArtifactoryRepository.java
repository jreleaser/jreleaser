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
package org.jreleaser.maven.plugin;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public class ArtifactoryRepository implements Activatable {
    private final Set<FileType> fileTypes = new LinkedHashSet<>();

    private Active active;
    private String path;

    void setAll(ArtifactoryRepository repository) {
        this.active = repository.active;
        this.path = repository.path;
        setFileTypes(repository.fileTypes);
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

    public boolean isSet() {
        return active != null ||
            isNotBlank(path) ||
            !fileTypes.isEmpty();
    }
}
