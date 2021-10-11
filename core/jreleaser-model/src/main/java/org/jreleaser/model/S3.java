/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.model;

import org.jreleaser.util.Env;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class S3 extends AbstractUploader {
    public static final String TYPE = "s3";

    private final Map<String, String> headers = new LinkedHashMap<>();
    private String region;
    private String bucket;
    private String path;
    private String endpoint;
    private String accessKeyId;
    private String secretKey;
    private String sessionToken;

    public S3() {
        super(TYPE);
    }

    void setAll(S3 s3) {
        super.setAll(s3);
        this.region = s3.region;
        this.bucket = s3.bucket;
        this.path = s3.path;
        this.endpoint = s3.endpoint;
        this.accessKeyId = s3.accessKeyId;
        this.secretKey = s3.secretKey;
        this.sessionToken = s3.sessionToken;
        setHeaders(s3.headers);
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        if (isBlank(getResolvedEndpoint())) {
            String url = "https://{{bucket}}.s3.{{region}}.amazonaws.com/" + getResolvedPath();
            Map<String, Object> p = new LinkedHashMap<>(artifactProps(context, artifact));
            p.putAll(getResolvedExtraProperties());
            p.put("bucket", bucket);
            p.put("region", region);
            return applyTemplate(url, p);
        }

        return "";
    }

    public String getResolvedPath(JReleaserContext context, Artifact artifact) {
        String path = getResolvedPath();
        Map<String, Object> p = new LinkedHashMap<>(artifactProps(context, artifact));
        p.putAll(getResolvedExtraProperties());
        return applyTemplate(path, p);
    }

    public String getResolvedRegion() {
        return Env.resolve("S3_" + Env.toVar(name) + "_REGION", region);
    }

    public String getResolvedBucket() {
        return Env.resolve("S3_" + Env.toVar(name) + "_BUCKET", bucket);
    }

    public String getResolvedAccessKeyId() {
        return Env.resolve("S3_" + Env.toVar(name) + "_ACCESS_KEY_ID", accessKeyId);
    }

    public String getResolvedSecretKey() {
        return Env.resolve("S3_" + Env.toVar(name) + "_SECRET_KEY", secretKey);
    }

    public String getResolvedSessionToken() {
        return Env.resolve("S3_" + Env.toVar(name) + "_SESSION_TOKEN", sessionToken);
    }

    public String getResolvedPath() {
        return Env.resolve("S3_" + Env.toVar(name) + "_PATH", path);
    }

    public String getResolvedEndpoint() {
        return Env.resolve("S3_" + Env.toVar(name) + "_ENDPOINT", endpoint);
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

    public void addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("region", getResolvedRegion());
        props.put("bucket", getResolvedBucket());
        props.put("accessKeyId", isNotBlank(getResolvedAccessKeyId()) ? HIDE : UNSET);
        props.put("secretKey", isNotBlank(getResolvedSecretKey()) ? HIDE : UNSET);
        props.put("sessionToken", isNotBlank(getResolvedSessionToken()) ? HIDE : UNSET);
        props.put("path", getResolvedPath());
        props.put("endpoint", getResolvedEndpoint());
        props.put("headers", headers);
    }
}
