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
import org.jreleaser.util.Env;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.upload.S3Uploader.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public final class S3Uploader extends AbstractUploader<S3Uploader> {
    private final Map<String, String> headers = new LinkedHashMap<>();

    private String region;
    private String bucket;
    private String path;
    private String downloadUrl;
    private String endpoint;
    private String accessKeyId;
    private String secretKey;
    private String sessionToken;

    private final org.jreleaser.model.api.upload.S3Uploader immutable = new org.jreleaser.model.api.upload.S3Uploader() {
        @Override
        public String getRegion() {
            return region;
        }

        @Override
        public String getBucket() {
            return bucket;
        }

        @Override
        public String getAccessKeyId() {
            return accessKeyId;
        }

        @Override
        public String getSecretKey() {
            return secretKey;
        }

        @Override
        public String getSessionToken() {
            return sessionToken;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getDownloadUrl() {
            return downloadUrl;
        }

        @Override
        public String getEndpoint() {
            return endpoint;
        }

        @Override
        public Map<String, String> getHeaders() {
            return unmodifiableMap(headers);
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
            return S3Uploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return S3Uploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return S3Uploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return S3Uploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return S3Uploader.this.isChecksums();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return S3Uploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(S3Uploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return S3Uploader.this.getPrefix();
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

    public S3Uploader() {
        super(TYPE);
    }

    public org.jreleaser.model.api.upload.S3Uploader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(S3Uploader source) {
        super.merge(source);
        this.region = merge(this.region, source.region);
        this.bucket = merge(this.bucket, source.bucket);
        this.path = merge(this.path, source.path);
        this.downloadUrl = merge(this.downloadUrl, source.downloadUrl);
        this.endpoint = merge(this.endpoint, source.endpoint);
        this.accessKeyId = merge(this.accessKeyId, source.accessKeyId);
        this.secretKey = merge(this.secretKey, source.secretKey);
        this.sessionToken = merge(this.sessionToken, source.sessionToken);
        setHeaders(merge(this.headers, source.headers));
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        return getResolvedDownloadUrl(context.fullProps(), artifact);
    }

    @Override
    public String getResolvedDownloadUrl(Map<String, Object> props, Artifact artifact) {
        if (isNotBlank(getResolvedDownloadUrl())) {
            Map<String, Object> p = new LinkedHashMap<>(artifactProps(props, artifact));
            p.putAll(getResolvedExtraProperties());
            p.put("bucket", bucket);
            p.put("region", region);
            return resolveTemplate(getResolvedDownloadUrl(), p);
        }

        if (isBlank(getResolvedEndpoint())) {
            String url = "https://{{bucket}}.s3.{{region}}.amazonaws.com/" + getResolvedPath();
            Map<String, Object> p = new LinkedHashMap<>(artifactProps(props, artifact));
            p.putAll(getResolvedExtraProperties());
            p.put("bucket", bucket);
            p.put("region", region);
            return resolveTemplate(url, p);
        }

        return "";
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
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

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("region", getResolvedRegion());
        props.put("bucket", getResolvedBucket());
        props.put("accessKeyId", isNotBlank(getResolvedAccessKeyId()) ? HIDE : UNSET);
        props.put("secretKey", isNotBlank(getResolvedSecretKey()) ? HIDE : UNSET);
        props.put("sessionToken", isNotBlank(getResolvedSessionToken()) ? HIDE : UNSET);
        props.put("path", getResolvedPath());
        props.put("downloadUrl", getResolvedDownloadUrl());
        props.put("endpoint", getResolvedEndpoint());
        props.put("headers", headers);
    }


    public String getResolvedPath(JReleaserContext context, Artifact artifact) {
        String artifactPath = getResolvedPath();

        String customPathKey = "s3" + capitalize(getName()) + "Path";
        if (artifact.getExtraProperties().containsKey(customPathKey)) {
            artifactPath = artifact.getExtraProperty(customPathKey);
        }

        Map<String, Object> p = new LinkedHashMap<>(artifactProps(context, artifact));
        p.putAll(getResolvedExtraProperties());
        return resolveTemplate(artifactPath, p);
    }

    public String getResolvedRegion() {
        return Env.env("S3_" + Env.toVar(name) + "_REGION", region);
    }

    public String getResolvedBucket() {
        return Env.env("S3_" + Env.toVar(name) + "_BUCKET", bucket);
    }

    public String getResolvedAccessKeyId() {
        return Env.env("S3_" + Env.toVar(name) + "_ACCESS_KEY_ID", accessKeyId);
    }

    public String getResolvedSecretKey() {
        return Env.env("S3_" + Env.toVar(name) + "_SECRET_KEY", secretKey);
    }

    public String getResolvedSessionToken() {
        return Env.env("S3_" + Env.toVar(name) + "_SESSION_TOKEN", sessionToken);
    }

    public String getResolvedPath() {
        return Env.env("S3_" + Env.toVar(name) + "_PATH", path);
    }

    public String getResolvedDownloadUrl() {
        return Env.env("S3_" + Env.toVar(name) + "_DOWNLOAD_URL", downloadUrl);
    }

    public String getResolvedEndpoint() {
        return Env.env("S3_" + Env.toVar(name) + "_ENDPOINT", endpoint);
    }
}
