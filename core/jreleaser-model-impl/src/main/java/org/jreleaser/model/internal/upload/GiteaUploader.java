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
import static org.jreleaser.model.api.upload.GiteaUploader.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class GiteaUploader extends AbstractGitPackageUploader<org.jreleaser.model.api.upload.GiteaUploader, GiteaUploader> {
    private static final String DOWNLOAD_URL = "https://{{host}}/api/packages/{{owner}}/generic/{{packageName}}/{{packageVersion}}/{{artifactFile}}";
    private static final long serialVersionUID = 8284794407254124499L;

    private String owner;

    @JsonIgnore
    private final org.jreleaser.model.api.upload.GiteaUploader immutable = new org.jreleaser.model.api.upload.GiteaUploader() {
        private static final long serialVersionUID = -7859608360457491380L;

        @Override
        public String getHost() {
            return GiteaUploader.this.getHost();
        }

        @Override
        public String getOwner() {
            return owner;
        }

        @Override
        public String getToken() {
            return GiteaUploader.this.getToken();
        }

        @Override
        public String getPackageName() {
            return GiteaUploader.this.getPackageName();
        }

        @Override
        public String getPackageVersion() {
            return GiteaUploader.this.getPackageVersion();
        }

        @Override
        public String getType() {
            return GiteaUploader.this.getType();
        }

        @Override
        public String getName() {
            return GiteaUploader.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return GiteaUploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return GiteaUploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return GiteaUploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return GiteaUploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return GiteaUploader.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return GiteaUploader.this.isCatalogs();
        }

        @Override
        public Active getActive() {
            return GiteaUploader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return GiteaUploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GiteaUploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return GiteaUploader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(GiteaUploader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return GiteaUploader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return GiteaUploader.this.getReadTimeout();
        }
    };

    public GiteaUploader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.upload.GiteaUploader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(GiteaUploader source) {
        super.merge(source);
        this.owner = merge(this.owner, source.owner);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("owner", owner);
    }

    @Override
    public String getResolvedDownloadUrl(TemplateContext props, Artifact artifact) {
        TemplateContext p = new TemplateContext(artifactProps(props, artifact));
        p.setAll(resolvedExtraProperties());
        p.set("host", getHost());
        p.set("owner", getOwner());
        p.set("packageName", getPackageName());
        p.set("packageVersion", getPackageVersion());
        return resolveTemplate(DOWNLOAD_URL, p);
    }
}
