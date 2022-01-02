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

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Artifactory extends AbstractUploader {
    public static final String NAME = "artifactory";

    private final List<ArtifactoryRepository> repositories = new ArrayList<>();

    private String host;
    private String username;
    private String password;
    private HttpUploader.Authorization authorization;

    public Artifactory() {
        super(NAME);
    }

    public HttpUploader.Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = HttpUploader.Authorization.NONE;
        }

        return authorization;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HttpUploader.Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(HttpUploader.Authorization authorization) {
        this.authorization = authorization;
    }

    public List<ArtifactoryRepository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<ArtifactoryRepository> repositories) {
        this.repositories.clear();
        this.repositories.addAll(repositories);
    }

    public static class ArtifactoryRepository implements Activatable {
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
}
