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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Ftp;
import org.jreleaser.model.internal.common.FtpDelegate;
import org.jreleaser.mustache.TemplateContext;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.api.download.FtpDownloader.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class FtpUploader extends AbstractUploader<org.jreleaser.model.api.upload.FtpUploader, FtpUploader> implements Ftp {
    private static final long serialVersionUID = -7427075974576853678L;

    private final FtpDelegate delegate = new FtpDelegate();
    private String path;
    private String downloadUrl;

    @JsonIgnore
    private final org.jreleaser.model.api.upload.FtpUploader immutable = new org.jreleaser.model.api.upload.FtpUploader() {
        private static final long serialVersionUID = -1377876046305087409L;

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getDownloadUrl() {
            return downloadUrl;
        }

        @Override
        public String getUsername() {
            return FtpUploader.this.getUsername();
        }

        @Override
        public String getPassword() {
            return FtpUploader.this.getPassword();
        }

        @Override
        public String getHost() {
            return FtpUploader.this.getHost();
        }

        @Override
        public Integer getPort() {
            return FtpUploader.this.getPort();
        }

        @Override
        public String getType() {
            return FtpUploader.this.getType();
        }

        @Override
        public String getName() {
            return FtpUploader.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return FtpUploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return FtpUploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return FtpUploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return FtpUploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return FtpUploader.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return FtpUploader.this.isCatalogs();
        }

        @Override
        public Active getActive() {
            return FtpUploader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return FtpUploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(FtpUploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return FtpUploader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(FtpUploader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return FtpUploader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return FtpUploader.this.getReadTimeout();
        }
    };

    public FtpUploader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.upload.FtpUploader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(FtpUploader source) {
        super.merge(source);
        this.delegate.merge(source.delegate);
        this.path = merge(this.path, source.path);
        this.downloadUrl = merge(this.downloadUrl, source.downloadUrl);
    }

    @Override
    public String getUsername() {
        return delegate.getUsername();
    }

    @Override
    public void setUsername(String username) {
        delegate.setUsername(username);
    }

    @Override
    public String getPassword() {
        return delegate.getPassword();
    }

    @Override
    public void setPassword(String password) {
        delegate.setPassword(password);
    }

    @Override
    public String getHost() {
        return delegate.getHost();
    }

    @Override
    public void setHost(String host) {
        delegate.setHost(host);
    }

    @Override
    public Integer getPort() {
        return delegate.getPort();
    }

    @Override
    public void setPort(Integer port) {
        delegate.setPort(port);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        delegate.asMap(props);
        props.put("path", path);
        props.put("downloadUrl", downloadUrl);
    }

    public String getResolvedPath(JReleaserContext context, Artifact artifact) {
        TemplateContext p = artifactProps(context.fullProps(), artifact);
        p.setAll(resolvedExtraProperties());
        return resolveTemplate(path, p);
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        return getResolvedDownloadUrl(context.fullProps(), artifact);
    }

    @Override
    public String getResolvedDownloadUrl(TemplateContext props, Artifact artifact) {
        TemplateContext p = new TemplateContext(artifactProps(props, artifact));
        p.setAll(resolvedExtraProperties());
        return resolveTemplate(downloadUrl, p);
    }
}
