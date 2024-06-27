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
import org.jreleaser.model.internal.common.Authenticatable;
import org.jreleaser.model.internal.common.HostAware;
import org.jreleaser.mustache.TemplateContext;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.api.upload.ForgejoUploader.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.18.0
 */
public final class ForgejoUploader extends AbstractGitPackageUploader<org.jreleaser.model.api.upload.ForgejoUploader, ForgejoUploader>
    implements Authenticatable, HostAware {
    private static final String DOWNLOAD_URL = "https://{{host}}/api/packages/{{owner}}/generic/{{packageName}}/{{packageVersion}}/{{artifactFile}}";
    private static final long serialVersionUID = 4636674888790577506L;

    private String owner;

    @JsonIgnore
    private final org.jreleaser.model.api.upload.ForgejoUploader immutable = new org.jreleaser.model.api.upload.ForgejoUploader() {
        private static final long serialVersionUID = 1814575818743064283L;

        @Override
        public String getHost() {
            return ForgejoUploader.this.getHost();
        }

        @Override
        public String getOwner() {
            return owner;
        }

        @Override
        public String getToken() {
            return ForgejoUploader.this.getToken();
        }

        @Override
        public String getPackageName() {
            return ForgejoUploader.this.getPackageName();
        }

        @Override
        public String getPackageVersion() {
            return ForgejoUploader.this.getPackageVersion();
        }

        @Override
        public String getType() {
            return ForgejoUploader.this.getType();
        }

        @Override
        public String getName() {
            return ForgejoUploader.this.getName();
        }

        @Override
        public String getServerRef() {
            return ForgejoUploader.this.getServerRef();
        }

        @Override
        public boolean isSnapshotSupported() {
            return ForgejoUploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return ForgejoUploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return ForgejoUploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return ForgejoUploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return ForgejoUploader.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return ForgejoUploader.this.isCatalogs();
        }

        @Override
        public Active getActive() {
            return ForgejoUploader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ForgejoUploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ForgejoUploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ForgejoUploader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ForgejoUploader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return ForgejoUploader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return ForgejoUploader.this.getReadTimeout();
        }
    };

    public ForgejoUploader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.upload.ForgejoUploader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ForgejoUploader source) {
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

    @Override
    public String getUsername() {
        // noop
        return null;
    }

    @Override
    public void setUsername(String username) {
        // noop
    }

    @Override
    public String getPassword() {
        return getToken();
    }

    @Override
    public void setPassword(String password) {
        setToken(password);
    }
}
