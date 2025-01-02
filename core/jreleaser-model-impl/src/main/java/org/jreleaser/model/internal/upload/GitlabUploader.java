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
package org.jreleaser.model.internal.upload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.mustache.TemplateContext;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.api.upload.GitlabUploader.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class GitlabUploader extends AbstractGitPackageUploader<org.jreleaser.model.api.upload.GitlabUploader, GitlabUploader> {
    private static final String DOWNLOAD_URL = "https://{{host}}/api/v4/projects/{{projectIdentifier}}/packages/generic/{{packageName}}/{{packageVersion}}/{{artifactFile}}";
    private static final long serialVersionUID = 5043963981384840431L;

    private String projectIdentifier;

    @JsonIgnore
    private final org.jreleaser.model.api.upload.GitlabUploader immutable = new org.jreleaser.model.api.upload.GitlabUploader() {
        private static final long serialVersionUID = -7870246763484590832L;

        @Override
        public String getHost() {
            return GitlabUploader.this.getHost();
        }

        @Override
        public String getToken() {
            return GitlabUploader.this.getToken();
        }

        @Override
        public String getPackageName() {
            return GitlabUploader.this.getPackageName();
        }

        @Override
        public String getPackageVersion() {
            return GitlabUploader.this.getPackageVersion();
        }

        @Override
        public String getProjectIdentifier() {
            return projectIdentifier;
        }

        @Override
        public String getType() {
            return GitlabUploader.this.getType();
        }

        @Override
        public String getName() {
            return GitlabUploader.this.getName();
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
        public boolean isCatalogs() {
            return GitlabUploader.this.isCatalogs();
        }

        @Override
        public Active getActive() {
            return GitlabUploader.this.getActive();
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
            return GitlabUploader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(GitlabUploader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return GitlabUploader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return GitlabUploader.this.getReadTimeout();
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
        this.projectIdentifier = merge(this.projectIdentifier, source.projectIdentifier);
    }

    public String getProjectIdentifier() {
        return projectIdentifier;
    }

    public void setProjectIdentifier(String projectIdentifier) {
        this.projectIdentifier = projectIdentifier;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("projectIdentifier", projectIdentifier);
    }

    @Override
    public String getResolvedDownloadUrl(TemplateContext props, Artifact artifact) {
        TemplateContext p = new TemplateContext(artifactProps(props, artifact));
        p.setAll(resolvedExtraProperties());
        p.set("host", getHost());
        p.set("packageName", getPackageName());
        p.set("packageVersion", getPackageVersion());
        p.set("projectIdentifier", getProjectIdentifier());
        return resolveTemplate(DOWNLOAD_URL, p);
    }
}
