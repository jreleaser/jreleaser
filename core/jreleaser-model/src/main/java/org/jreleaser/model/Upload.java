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
package org.jreleaser.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Upload implements Domain, EnabledAware {
    private final Map<String, Artifactory> artifactory = new LinkedHashMap<>();
    private final Map<String, FtpUploader> ftp = new LinkedHashMap<>();
    private final Map<String, HttpUploader> http = new LinkedHashMap<>();
    private final Map<String, S3> s3 = new LinkedHashMap<>();
    private final Map<String, ScpUploader> scp = new LinkedHashMap<>();
    private final Map<String, SftpUploader> sftp = new LinkedHashMap<>();
    private final Map<String, AzureArtifacts> azureArtifacts = new LinkedHashMap<>();
    private Boolean enabled;

    void setAll(Upload upload) {
        this.enabled = upload.enabled;
        setArtifactory(upload.artifactory);
        setFtp(upload.ftp);
        setHttp(upload.http);
        setS3(upload.s3);
        setScp(upload.scp);
        setSftp(upload.sftp);
        setAzureArtifacts(upload.azureArtifacts);
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    public Optional<? extends Uploader> getUploader(String type, String name) {
        switch (type) {
            case Artifactory.TYPE:
                return Optional.ofNullable(artifactory.get(name));
            case FtpUploader.TYPE:
                return Optional.ofNullable(ftp.get(name));
            case HttpUploader.TYPE:
                return Optional.ofNullable(http.get(name));
            case S3.TYPE:
                return Optional.ofNullable(s3.get(name));
            case ScpUploader.TYPE:
                return Optional.ofNullable(scp.get(name));
            case SftpUploader.TYPE:
                return Optional.ofNullable(sftp.get(name));
            case AzureArtifacts.TYPE:
                return Optional.ofNullable(azureArtifacts.get(name));
        }

        return Optional.empty();
    }

    public Optional<? extends Uploader> getActiveUploader(String type, String name) {
        switch (type) {
            case Artifactory.TYPE:
                return getActiveArtifactory(name);
            case FtpUploader.TYPE:
                return getActiveFtp(name);
            case HttpUploader.TYPE:
                return getActiveHttp(name);
            case S3.TYPE:
                return getActiveS3(name);
            case ScpUploader.TYPE:
                return getActiveScp(name);
            case SftpUploader.TYPE:
                return getActiveSftp(name);
        }

        return Optional.empty();
    }

    public Optional<Artifactory> getActiveArtifactory(String name) {
        return artifactory.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.name))
            .findFirst();
    }

    public Optional<FtpUploader> getActiveFtp(String name) {
        return ftp.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.name))
            .findFirst();
    }

    public Optional<HttpUploader> getActiveHttp(String name) {
        return http.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.name))
            .findFirst();
    }

    public Optional<S3> getActiveS3(String name) {
        return s3.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.name))
            .findFirst();
    }

    public Optional<ScpUploader> getActiveScp(String name) {
        return scp.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.name))
            .findFirst();
    }

    public Optional<SftpUploader> getActiveSftp(String name) {
        return sftp.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.name))
            .findFirst();
    }

    public Optional<AzureArtifacts> getActiveAzureArtifacts(String name) {
        return azureArtifacts.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.name))
            .findFirst();
    }

    public List<Artifactory> getActiveArtifactories() {
        return artifactory.values().stream()
            .filter(Artifactory::isEnabled)
            .collect(toList());
    }

    public Map<String, Artifactory> getArtifactory() {
        return artifactory;
    }

    public void setArtifactory(Map<String, Artifactory> artifactory) {
        this.artifactory.clear();
        this.artifactory.putAll(artifactory);
    }

    public void addArtifactory(Artifactory artifactory) {
        this.artifactory.put(artifactory.getName(), artifactory);
    }

    public List<FtpUploader> getActiveFtps() {
        return ftp.values().stream()
            .filter(FtpUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, FtpUploader> getFtp() {
        return ftp;
    }

    public void setFtp(Map<String, FtpUploader> ftp) {
        this.ftp.clear();
        this.ftp.putAll(ftp);
    }

    public void addFtp(FtpUploader ftp) {
        this.ftp.put(ftp.getName(), ftp);
    }

    public List<HttpUploader> getActiveHttps() {
        return http.values().stream()
            .filter(HttpUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, HttpUploader> getHttp() {
        return http;
    }

    public void setHttp(Map<String, HttpUploader> http) {
        this.http.clear();
        this.http.putAll(http);
    }

    public void addHttp(HttpUploader http) {
        this.http.put(http.getName(), http);
    }

    public List<S3> getActiveS3s() {
        return s3.values().stream()
            .filter(S3::isEnabled)
            .collect(toList());
    }

    public Map<String, S3> getS3() {
        return s3;
    }

    public void setS3(Map<String, S3> s3) {
        this.s3.clear();
        this.s3.putAll(s3);
    }

    public void addS3(S3 s3) {
        this.s3.put(s3.getName(), s3);
    }

    public List<ScpUploader> getActiveScps() {
        return scp.values().stream()
            .filter(ScpUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, ScpUploader> getScp() {
        return scp;
    }

    public void setScp(Map<String, ScpUploader> scp) {
        this.scp.clear();
        this.scp.putAll(scp);
    }

    public void addScp(ScpUploader scp) {
        this.scp.put(scp.getName(), scp);
    }

    public List<SftpUploader> getActiveSftps() {
        return sftp.values().stream()
            .filter(SftpUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, SftpUploader> getSftp() {
        return sftp;
    }

    public void setSftp(Map<String, SftpUploader> sftp) {
        this.sftp.clear();
        this.sftp.putAll(sftp);
    }

    public void addSftp(SftpUploader sftp) {
        this.sftp.put(sftp.getName(), sftp);
    }

    public List<AzureArtifacts> getActiveAzureArtifactses() {
        return azureArtifacts.values().stream()
            .filter(AzureArtifacts::isEnabled)
            .collect(toList());
    }

    public Map<String, AzureArtifacts> getAzureArtifacts() {
        return azureArtifacts;
    }

    public void setAzureArtifacts(Map<String, AzureArtifacts> azureArtifacts) {
        this.azureArtifacts.clear();
        this.azureArtifacts.putAll(azureArtifacts);
    }

    public void addAzureArtifacts(AzureArtifacts azureArtifacts) {
        this.azureArtifacts.put(azureArtifacts.getName(), azureArtifacts);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());

        List<Map<String, Object>> artifactory = this.artifactory.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!artifactory.isEmpty()) map.put("artifactory", artifactory);

        List<Map<String, Object>> ftp = this.ftp.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!ftp.isEmpty()) map.put("ftp", ftp);

        List<Map<String, Object>> http = this.http.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!http.isEmpty()) map.put("http", http);

        List<Map<String, Object>> s3 = this.s3.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!s3.isEmpty()) map.put("s3", s3);

        List<Map<String, Object>> scp = this.scp.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!scp.isEmpty()) map.put("scp", scp);

        List<Map<String, Object>> sftp = this.sftp.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!sftp.isEmpty()) map.put("sftp", sftp);

        List<Map<String, Object>> azureArtifacts = this.azureArtifacts.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!azureArtifacts.isEmpty()) map.put("azureArtifacts", azureArtifacts);

        return map;
    }

    public <A extends Uploader> Map<String, A> findUploadersByType(String uploaderType) {
        switch (uploaderType) {
            case Artifactory.TYPE:
                return (Map<String, A>) artifactory;
            case FtpUploader.TYPE:
                return (Map<String, A>) ftp;
            case HttpUploader.TYPE:
                return (Map<String, A>) http;
            case S3.TYPE:
                return (Map<String, A>) s3;
            case ScpUploader.TYPE:
                return (Map<String, A>) scp;
            case SftpUploader.TYPE:
                return (Map<String, A>) sftp;
            case AzureArtifacts.TYPE:
                return (Map<String, A>) azureArtifacts;
        }

        return Collections.emptyMap();
    }

    public <A extends Uploader> List<A> findAllActiveUploaders() {
        List<A> uploaders = new ArrayList<>();
        uploaders.addAll((List<A>) getActiveArtifactories());
        uploaders.addAll((List<A>) getActiveFtps());
        uploaders.addAll((List<A>) getActiveHttps());
        uploaders.addAll((List<A>) getActiveS3s());
        uploaders.addAll((List<A>) getActiveScps());
        uploaders.addAll((List<A>) getActiveSftps());
        uploaders.addAll((List<A>) getActiveAzureArtifactses());
        return uploaders;
    }

    public Map<String, String> resolveDownloadUrls(JReleaserContext context, Distribution distribution, Artifact artifact, String prefix) {
        Map<String, String> urls = new LinkedHashMap<>();

        List<Uploader> uploaders = findAllActiveUploaders();
        for (Uploader uploader : uploaders) {
            List<String> keys = uploader.resolveSkipKeys();
            if (isSkip(distribution, keys) ||
                isSkip(artifact, keys)) continue;
            String key = prefix +
                "Download" +
                capitalize(uploader.getType()) +
                getClassNameForLowerCaseHyphenSeparatedName(uploader.getName()) +
                "Url";
            String url = uploader.getResolvedDownloadUrl(context, artifact);
            urls.put(key, url);

            if (findUploadersByType(uploader.getType()).size() == 1 && !isSkip(distribution, keys) &&
                !isSkip(artifact, keys)) {
                key = prefix +
                    "Download" +
                    capitalize(uploader.getType()) +
                    "Url";
                url = uploader.getResolvedDownloadUrl(context, artifact);
                urls.put(key, url);
            }
        }

        if (uploaders.size() == 1) {
            Uploader uploader = uploaders.get(0);
            List<String> keys = uploader.resolveSkipKeys();
            if (!isSkip(distribution, keys) &&
                !isSkip(artifact, keys)) {
                String key = prefix + "DownloadUrl";
                String url = uploader.getResolvedDownloadUrl(context, artifact);
                urls.put(key, url);
            }
        }

        return urls;
    }

    private boolean isSkip(ExtraProperties props, List<String> keys) {
        for (String key : keys) {
            if (props.extraPropertyIsTrue(key)) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> supportedUploaders() {
        Set<String> set = new LinkedHashSet<>();
        set.add(Artifactory.TYPE);
        set.add(FtpUploader.TYPE);
        set.add(HttpUploader.TYPE);
        set.add(S3.TYPE);
        set.add(ScpUploader.TYPE);
        set.add(SftpUploader.TYPE);
        set.add(AzureArtifacts.TYPE);
        return Collections.unmodifiableSet(set);
    }
}
