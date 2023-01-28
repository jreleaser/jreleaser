/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.mustache.TemplateContext;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.upload.GiteaUploader.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class GiteaUploader extends AbstractUploader<org.jreleaser.model.api.upload.GiteaUploader, GiteaUploader> {
    private static final String DOWNLOAD_URL = "https://{{host}}/api/packages/{{owner}}/generic/{{packageName}}/{{packageVersion}}/{{artifactFile}}";
    private static final long serialVersionUID = 6871010939362667394L;

    private String host;
    private String owner;
    private String token;
    private String packageName;
    private String packageVersion;

    private final org.jreleaser.model.api.upload.GiteaUploader immutable = new org.jreleaser.model.api.upload.GiteaUploader() {
        private static final long serialVersionUID = -449985306983964476L;

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public String getOwner() {
            return owner;
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
            return GiteaUploader.this.getPrefix();
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
        this.host = merge(this.host, source.host);
        this.owner = merge(this.owner, source.owner);
        this.token = merge(this.token, source.token);
        this.packageName = merge(this.packageName, source.packageName);
        this.packageVersion = merge(this.packageVersion, source.packageVersion);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("host", host);
        props.put("owner", owner);
        props.put("token", isNotBlank(token) ? HIDE : UNSET);
        props.put("packageName", packageName);
        props.put("packageVersion", packageVersion);
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        return getResolvedDownloadUrl(context.fullProps(), artifact);
    }

    @Override
    public String getResolvedDownloadUrl(TemplateContext props, Artifact artifact) {
        TemplateContext p = new TemplateContext(artifactProps(props, artifact));
        p.setAll(getResolvedExtraProperties());
        p.set("host", host);
        p.set("owner", owner);
        p.set("packageName", packageName);
        p.set("packageVersion", packageVersion);
        return resolveTemplate(DOWNLOAD_URL, p);
    }

    public String getResolvedUploadUrl(JReleaserContext context, Artifact artifact) {
        return getResolvedDownloadUrl(context, artifact);
    }
}
