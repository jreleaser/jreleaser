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
package org.jreleaser.model.internal.upload;

import org.jreleaser.model.Active;
import org.jreleaser.model.Http;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.mustache.Templates;
import org.jreleaser.util.FileType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public final class ArtifactoryUploader extends AbstractUploader<org.jreleaser.model.api.upload.ArtifactoryUploader, ArtifactoryUploader> {
    private static final long serialVersionUID = -8107525740451541910L;

    private final List<ArtifactoryRepository> repositories = new ArrayList<>();
    private String host;
    private String username;
    private String password;
    private Http.Authorization authorization;

    private final org.jreleaser.model.api.upload.ArtifactoryUploader immutable = new org.jreleaser.model.api.upload.ArtifactoryUploader() {
        private static final long serialVersionUID = -6040496931102283198L;

        private List<? extends org.jreleaser.model.api.upload.ArtifactoryUploader.ArtifactoryRepository> repositories;

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public Http.Authorization getAuthorization() {
            return authorization;
        }

        @Override
        public List<? extends org.jreleaser.model.api.upload.ArtifactoryUploader.ArtifactoryRepository> getRepositories() {
            if (null == repositories) {
                repositories = ArtifactoryUploader.this.repositories.stream()
                    .map(ArtifactoryUploader.ArtifactoryRepository::asImmutable)
                    .collect(toList());
            }
            return repositories;
        }

        @Override
        public String getType() {
            return ArtifactoryUploader.this.getType();
        }

        @Override
        public String getName() {
            return ArtifactoryUploader.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return ArtifactoryUploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return ArtifactoryUploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return ArtifactoryUploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return ArtifactoryUploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return ArtifactoryUploader.this.isChecksums();
        }

        @Override
        public Active getActive() {
            return ArtifactoryUploader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ArtifactoryUploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ArtifactoryUploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ArtifactoryUploader.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ArtifactoryUploader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return ArtifactoryUploader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return ArtifactoryUploader.this.getReadTimeout();
        }
    };

    public ArtifactoryUploader() {
        super(org.jreleaser.model.api.upload.ArtifactoryUploader.TYPE);
    }

    @Override
    public org.jreleaser.model.api.upload.ArtifactoryUploader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ArtifactoryUploader source) {
        super.merge(source);
        this.host = merge(this.host, source.host);
        this.username = merge(this.username, source.username);
        this.password = merge(this.password, source.password);
        this.authorization = merge(this.authorization, source.authorization);
        setRepositories(merge(this.repositories, source.repositories));
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

    public Http.Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Http.Authorization authorization) {
        this.authorization = authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = Http.Authorization.of(authorization);
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
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("authorization", authorization);
        props.put("host", host);
        props.put("username", isNotBlank(username) ? HIDE : UNSET);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
        List<Map<String, Object>> repositories = this.repositories.stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!repositories.isEmpty()) props.put("repositories", repositories);
    }

    public Http.Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = Http.Authorization.BEARER;
        }

        return authorization;
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        return resolveUrl(context.fullProps(), artifact);
    }

    @Override
    public String getResolvedDownloadUrl(Map<String, Object> props, Artifact artifact) {
        return resolveUrl(props, artifact);
    }

    public String getResolvedUploadUrl(JReleaserContext context, Artifact artifact) {
        return resolveUrl(context.fullProps(), artifact);
    }

    private String resolveUrl(Map<String, Object> props, Artifact artifact) {
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

    public static final class ArtifactoryRepository extends AbstractModelObject<ArtifactoryRepository> implements Domain, Activatable {
        private static final long serialVersionUID = 112708885585709294L;

        private final Set<FileType> fileTypes = new LinkedHashSet<>();

        private Active active;
        private boolean enabled;
        private String path;

        private final org.jreleaser.model.api.upload.ArtifactoryUploader.ArtifactoryRepository immutable = new org.jreleaser.model.api.upload.ArtifactoryUploader.ArtifactoryRepository() {
            private static final long serialVersionUID = -954690979964972109L;

            @Override
            public String getPath() {
                return path;
            }

            @Override
            public Set<FileType> getFileTypes() {
                return unmodifiableSet(ArtifactoryRepository.this.getFileTypes());
            }

            @Override
            public Active getActive() {
                return active;
            }

            @Override
            public boolean isEnabled() {
                return ArtifactoryRepository.this.isEnabled();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(ArtifactoryRepository.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.upload.ArtifactoryUploader.ArtifactoryRepository asImmutable() {
            return immutable;
        }

        @Override
        public void merge(ArtifactoryRepository source) {
            this.active = merge(this.active, source.active);
            this.enabled = merge(this.enabled, source.enabled);
            this.path = merge(this.path, source.path);
            setFileTypes(source.fileTypes);
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
            setActive(Active.of(str));
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
