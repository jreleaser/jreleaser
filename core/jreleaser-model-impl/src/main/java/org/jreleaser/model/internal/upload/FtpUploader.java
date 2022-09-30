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
import org.jreleaser.model.internal.common.Ftp;
import org.jreleaser.util.Env;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.download.FtpDownloader.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class FtpUploader extends AbstractUploader<org.jreleaser.model.api.upload.FtpUploader, FtpUploader> implements Ftp {
    private String username;
    private String password;
    private String host;
    private Integer port;
    private String path;
    private String downloadUrl;

    private final org.jreleaser.model.api.upload.FtpUploader immutable = new org.jreleaser.model.api.upload.FtpUploader() {
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
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public Integer getPort() {
            return port;
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
        public Active getActive() {
            return active;
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
            return FtpUploader.this.getPrefix();
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
        this.username = merge(this.username, source.username);
        this.password = merge(this.password, source.password);
        this.host = merge(this.host, source.host);
        this.port = merge(this.port, source.port);
        this.path = merge(this.path, source.path);
        this.downloadUrl = merge(this.downloadUrl, source.downloadUrl);
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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
        props.put("host", getResolvedHost());
        props.put("port", getResolvedPort());
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
        props.put("path", path);
        props.put("downloadUrl", downloadUrl);
    }

    public String getResolvedUsername() {
        return Env.env("FTP_" + Env.toVar(name) + "_USERNAME", username);
    }

    public String getResolvedPassword() {
        return Env.env("FTP_" + Env.toVar(name) + "_PASSWORD", password);
    }

    public String getResolvedHost() {
        return Env.env("FTP_" + Env.toVar(name) + "_HOST", host);
    }

    public Integer getResolvedPort() {
        String value = Env.env("FTP_" + Env.toVar(name) + "_PORT", null == port ? "" : String.valueOf(port));
        return isBlank(value) ? 21 : Integer.parseInt(value);
    }

    public String getResolvedPath(JReleaserContext context, Artifact artifact) {
        Map<String, Object> p = artifactProps(context.fullProps(), artifact);
        p.putAll(getResolvedExtraProperties());
        return resolveTemplate(path, p);
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        return getResolvedDownloadUrl(context.fullProps(), artifact);
    }

    @Override
    public String getResolvedDownloadUrl(Map<String, Object> props, Artifact artifact) {
        Map<String, Object> p = new LinkedHashMap<>(artifactProps(props, artifact));
        p.putAll(getResolvedExtraProperties());
        return resolveTemplate(downloadUrl, p);
    }
}
