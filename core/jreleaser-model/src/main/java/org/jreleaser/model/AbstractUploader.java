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

import org.jreleaser.util.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
abstract class AbstractUploader implements Uploader {
    protected final String type;
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    protected String name;
    protected boolean enabled;
    protected Active active;
    private String uploadUrl;
    private String downloadUrl;
    private int connectTimeout;
    private int readTimeout;
    private Boolean artifacts;
    private Boolean files;
    private Boolean signatures;

    protected AbstractUploader(String type) {
        this.type = type;
    }

    void setAll(AbstractUploader uploader) {
        this.active = uploader.active;
        this.enabled = uploader.enabled;
        this.name = uploader.name;
        this.uploadUrl = uploader.uploadUrl;
        this.downloadUrl = uploader.downloadUrl;
        this.connectTimeout = uploader.connectTimeout;
        this.readTimeout = uploader.readTimeout;
        this.artifacts = uploader.artifacts;
        this.files = uploader.files;
        this.signatures = uploader.signatures;
        setExtraProperties(uploader.extraProperties);
    }

    @Override
    public String getResolvedUploadUrl(JReleaserContext context, Artifact artifact) {
        Map<String, Object> p = new LinkedHashMap<>(artifactProps(context, artifact));
        p.putAll(getResolvedExtraProperties());
        return applyTemplate(uploadUrl, p);
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        Map<String, Object> p = new LinkedHashMap<>(artifactProps(context, artifact));
        p.putAll(getResolvedExtraProperties());
        return applyTemplate(downloadUrl, p);
    }

    @Override
    public String getPrefix() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            active = Active.NEVER;
        }
        enabled = active.check(project);
        if (project.isSnapshot() && !isSnapshotSupported()) {
            enabled = false;
        }
        return enabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUploadUrl() {
        return uploadUrl;
    }

    @Override
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        this.active = Active.of(str);
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public Boolean isArtifacts() {
        return artifacts == null || artifacts;
    }

    @Override
    public void setArtifacts(Boolean artifacts) {
        this.artifacts = artifacts;
    }

    @Override
    public boolean isArtifactsSet() {
        return artifacts != null;
    }

    @Override
    public Boolean isFiles() {
        return files == null || files;
    }

    @Override
    public void setFiles(Boolean files) {
        this.files = files;
    }

    @Override
    public boolean isFilesSet() {
        return files != null;
    }

    @Override
    public Boolean isSignatures() {
        return signatures == null || signatures;
    }

    @Override
    public void setSignatures(Boolean signatures) {
        this.signatures = signatures;
    }

    @Override
    public boolean isSignaturesSet() {
        return signatures != null;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", active);
        props.put("uploadUrl", uploadUrl);
        props.put("downloadUrl", downloadUrl);
        props.put("connectTimeout", connectTimeout);
        props.put("readTimeout", readTimeout);
        props.put("artifacts", isArtifacts());
        props.put("files", isFiles());
        props.put("signatures", isSignatures());
        asMap(props, full);
        props.put("extraProperties", getResolvedExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(this.getName(), props);
        return map;
    }

    protected abstract void asMap(Map<String, Object> props, boolean full);

    @Override
    public List<String> resolveSkipKeys() {
        String skipUpload = "skipUpload";
        String skipUploadByType = skipUpload + capitalize(type);
        String skipUploadByName = skipUploadByType + getClassNameForLowerCaseHyphenSeparatedName(name);
        return Arrays.asList(skipUpload, skipUploadByType, skipUploadByName);
    }

    @Override
    public Map<String, Object> artifactProps(JReleaserContext context, Artifact artifact) {
        Map<String, Object> props = context.props();

        String platform = isNotBlank(artifact.getPlatform()) ? artifact.getPlatform() : "";
        // add extra properties without clobbering existing keys
        Map<String, Object> artifactProps = artifact.getResolvedExtraProperties("artifact");
        artifactProps.keySet().stream()
            .filter(k -> !props.containsKey(k))
            .filter(k -> !k.startsWith("artifactSkip"))
            .forEach(k -> props.put(k, artifactProps.get(k)));
        String artifactFileName = artifact.getEffectivePath(context).getFileName().toString();
        props.put(Constants.KEY_ARTIFACT_PLATFORM, platform);
        props.put(Constants.KEY_ARTIFACT_FILE_NAME, artifactFileName);
        props.put(Constants.KEY_ARTIFACT_NAME, getFilename(artifactFileName));
        return props;
    }
}
