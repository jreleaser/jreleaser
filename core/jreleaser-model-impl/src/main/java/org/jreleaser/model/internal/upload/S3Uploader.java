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
import org.jreleaser.mustache.TemplateContext;

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
public final class S3Uploader extends AbstractUploader<org.jreleaser.model.api.upload.S3Uploader, S3Uploader> {
    private static final long serialVersionUID = 2634650056338097232L;

    private final Map<String, String> headers = new LinkedHashMap<>();

    private String region;
    private String bucket;
    private String path;
    private String downloadUrl;
    private String endpoint;
    private String accessKeyId;
    private String secretKey;
    private String sessionToken;

    @JsonIgnore
    private final org.jreleaser.model.api.upload.S3Uploader immutable = new org.jreleaser.model.api.upload.S3Uploader() {
        private static final long serialVersionUID = -3263159021072324345L;

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
            return S3Uploader.this.getType();
        }

        @Override
        public String getName() {
            return S3Uploader.this.getName();
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
        public boolean isCatalogs() {
            return S3Uploader.this.isCatalogs();
        }

        @Override
        public Active getActive() {
            return S3Uploader.this.getActive();
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
            return S3Uploader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(S3Uploader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return S3Uploader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return S3Uploader.this.getReadTimeout();
        }
    };

    public S3Uploader() {
        super(TYPE);
    }

    @Override
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
    public String getResolvedDownloadUrl(TemplateContext props, Artifact artifact) {
        if (isNotBlank(getDownloadUrl())) {
            TemplateContext p = new TemplateContext(artifactProps(props, artifact));
            p.setAll(resolvedExtraProperties());
            p.set("bucket", bucket);
            p.set("region", region);
            return resolveTemplate(getDownloadUrl(), p);
        }

        if (isBlank(getEndpoint())) {
            String url = "https://{{bucket}}.s3.{{region}}.amazonaws.com/" + path;
            TemplateContext p = new TemplateContext(artifactProps(props, artifact));
            p.setAll(resolvedExtraProperties());
            p.set("bucket", bucket);
            p.set("region", region);
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
        props.put("region", region);
        props.put("bucket", bucket);
        props.put("accessKeyId", isNotBlank(accessKeyId) ? HIDE : UNSET);
        props.put("secretKey", isNotBlank(secretKey) ? HIDE : UNSET);
        props.put("sessionToken", isNotBlank(sessionToken) ? HIDE : UNSET);
        props.put("path", path);
        props.put("downloadUrl", downloadUrl);
        props.put("endpoint", endpoint);
        props.put("headers", headers);
    }


    public String getResolvedPath(JReleaserContext context, Artifact artifact) {
        String artifactPath = path;

        String customPathKey = "s3" + capitalize(getName()) + "Path";
        if (artifact.getExtraProperties().containsKey(customPathKey)) {
            artifactPath = artifact.getExtraProperty(customPathKey);
        }

        TemplateContext p = new TemplateContext(artifactProps(context, artifact));
        p.setAll(resolvedExtraProperties());
        return resolveTemplate(artifactPath, p);
    }
}
