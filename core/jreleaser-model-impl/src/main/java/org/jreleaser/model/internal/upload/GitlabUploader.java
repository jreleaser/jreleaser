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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.upload.GitlabUploader.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class GitlabUploader extends AbstractUploader<org.jreleaser.model.api.upload.GitlabUploader, GitlabUploader> {
    private static final String DOWNLOAD_URL = "https://{{host}}/api/v4/projects/{{projectIdentifier}}/packages/generic/{{packageName}}/{{packageVersion}}/{{artifactFile}}";
    private static final long serialVersionUID = 4471788124636163088L;

    private String host;
    private String token;
    private String packageName;
    private String packageVersion;
    private String projectIdentifier;

    private final org.jreleaser.model.api.upload.GitlabUploader immutable = new org.jreleaser.model.api.upload.GitlabUploader() {
        private static final long serialVersionUID = 7721607213181794594L;

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public String getPackageVersion() {
            return packageVersion;
        }

        @Override
        public String getProjectIdentifier() {
            return projectIdentifier;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isSnapshotSupported() {
            return GitlabUploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return GitlabUploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return GitlabUploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return GitlabUploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return GitlabUploader.this.isChecksums();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return GitlabUploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GitlabUploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return GitlabUploader.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }

        @Override
        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        @Override
        public Integer getReadTimeout() {
            return readTimeout;
        }
    };

    public GitlabUploader() {
        super(TYPE);
        setHost("gitlab.com");
    }

    @Override
    public org.jreleaser.model.api.upload.GitlabUploader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(GitlabUploader source) {
        super.merge(source);
        this.host = merge(this.host, source.host);
        this.token = merge(this.token, source.token);
        this.packageName = merge(this.packageName, source.packageName);
        this.packageVersion = merge(this.packageVersion, source.packageVersion);
        this.projectIdentifier = merge(this.projectIdentifier, source.projectIdentifier);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getProjectIdentifier() {
        return projectIdentifier;
    }

    public void setProjectIdentifier(String projectIdentifier) {
        this.projectIdentifier = projectIdentifier;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("host", host);
        props.put("token", isNotBlank(token) ? HIDE : UNSET);
        props.put("packageName", packageName);
        props.put("packageVersion", packageVersion);
        props.put("projectIdentifier", projectIdentifier);
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        return getResolvedDownloadUrl(context.fullProps(), artifact);
    }

    @Override
    public String getResolvedDownloadUrl(Map<String, Object> props, Artifact artifact) {
        Map<String, Object> p = new LinkedHashMap<>(artifactProps(props, artifact));
        p.putAll(getResolvedExtraProperties());
        p.put("host", host);
        p.put("packageName", packageName);
        p.put("packageVersion", packageVersion);
        p.put("projectIdentifier", projectIdentifier);
        return resolveTemplate(DOWNLOAD_URL, p);
    }

    public String getResolvedUploadUrl(JReleaserContext context, Artifact artifact) {
        return getResolvedDownloadUrl(context, artifact);
    }
}
