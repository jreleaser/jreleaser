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

import org.jreleaser.util.Env;
import org.jreleaser.util.FileType;
import org.jreleaser.util.Templates;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Artifactory extends AbstractUploader {
    public static final String TYPE = "artifactory";

    private final List<ArtifactoryRepository> repositories = new ArrayList<>();

    private String host;
    private String username;
    private String password;
    private HttpUploader.Authorization authorization;

    public Artifactory() {
        super(TYPE);
    }

    void setAll(Artifactory artifactory) {
        super.setAll(artifactory);
        this.host = artifactory.host;
        this.username = artifactory.username;
        this.password = artifactory.password;
        this.authorization = artifactory.authorization;
        setRepositories(artifactory.repositories);
    }

    public HttpUploader.Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = HttpUploader.Authorization.BEARER;
        }

        return authorization;
    }

    public String getResolvedHost() {
        return Env.resolve("ARTIFACTORY_" + Env.toVar(name) + "_HOST", host);
    }

    public String getResolvedUsername() {
        return Env.resolve("ARTIFACTORY_" + Env.toVar(name) + "_USERNAME", username);
    }

    public String getResolvedPassword() {
        return Env.resolve("ARTIFACTORY_" + Env.toVar(name) + "_PASSWORD", password);
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

    public void setAuthorization(String authorization) {
        this.authorization = HttpUploader.Authorization.of(authorization);
    }

    public List<ArtifactoryRepository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<ArtifactoryRepository> repositories) {
        this.repositories.clear();
        this.repositories.addAll(repositories);
    }

    public void addRepository(ArtifactoryRepository repository) {
        if (null != repository) {
            this.repositories.add(repository);
        }
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("authorization", authorization);
        props.put("host", getResolvedHost());
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
        List<Map<String, Object>> repositories = this.repositories.stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!repositories.isEmpty()) props.put("repositories", repositories);
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        return resolveUrl(context.fullProps(), artifact);
    }

    @Override
    public String getResolvedDownloadUrl(Map<String,Object> props, Artifact artifact) {
        return resolveUrl(props, artifact);
    }

    public String getResolvedUploadUrl(JReleaserContext context, Artifact artifact) {
        return resolveUrl(context.fullProps(), artifact);
    }

    private String resolveUrl(Map<String,Object> props, Artifact artifact) {
        Map<String, Object> p = new LinkedHashMap<>(artifactProps(props, artifact));
        p.put("artifactoryHost", host);

        Optional<ArtifactoryRepository> repository = repositories.stream()
            .filter(r -> r.handles(artifact))
            .findFirst();

        if (repository.isPresent()) {
            p.put("repositoryPath", repository.get().getPath());
            String url = "{{artifactoryHost}}/{{repositoryPath}}";
            return Templates.resolveTemplate(url, p);
        }

        return "";
    }

    public static class ArtifactoryRepository implements Domain, Activatable {
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
}
