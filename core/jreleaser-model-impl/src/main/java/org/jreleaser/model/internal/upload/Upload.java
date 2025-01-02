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
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.distributions.Distribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public final class Upload extends AbstractActivatable<Upload> implements Domain {
    private static final long serialVersionUID = -8267239230459971399L;

    private final Map<String, ArtifactoryUploader> artifactory = new LinkedHashMap<>();
    private final Map<String, FtpUploader> ftp = new LinkedHashMap<>();
    private final Map<String, GiteaUploader> gitea = new LinkedHashMap<>();
    private final Map<String, GitlabUploader> gitlab = new LinkedHashMap<>();
    private final Map<String, HttpUploader> http = new LinkedHashMap<>();
    private final Map<String, S3Uploader> s3 = new LinkedHashMap<>();
    private final Map<String, ScpUploader> scp = new LinkedHashMap<>();
    private final Map<String, SftpUploader> sftp = new LinkedHashMap<>();

    @JsonIgnore
    private final org.jreleaser.model.api.upload.Upload immutable = new org.jreleaser.model.api.upload.Upload() {
        private static final long serialVersionUID = -1954880769141203693L;

        private Map<String, ? extends org.jreleaser.model.api.upload.ArtifactoryUploader> artifactory;
        private Map<String, ? extends org.jreleaser.model.api.upload.FtpUploader> ftp;
        private Map<String, ? extends org.jreleaser.model.api.upload.GiteaUploader> gitea;
        private Map<String, ? extends org.jreleaser.model.api.upload.GitlabUploader> gitlab;
        private Map<String, ? extends org.jreleaser.model.api.upload.HttpUploader> http;
        private Map<String, ? extends org.jreleaser.model.api.upload.S3Uploader> s3;
        private Map<String, ? extends org.jreleaser.model.api.upload.ScpUploader> scp;
        private Map<String, ? extends org.jreleaser.model.api.upload.SftpUploader> sftp;

        @Override
        public Map<String, ? extends org.jreleaser.model.api.upload.ArtifactoryUploader> getArtifactory() {
            if (null == artifactory) {
                artifactory = Upload.this.artifactory.values().stream()
                    .map(ArtifactoryUploader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.upload.Uploader::getName, identity()));
            }
            return artifactory;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.upload.FtpUploader> getFtp() {
            if (null == ftp) {
                ftp = Upload.this.ftp.values().stream()
                    .map(FtpUploader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.upload.Uploader::getName, identity()));
            }
            return ftp;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.upload.GiteaUploader> getGitea() {
            if (null == gitea) {
                gitea = Upload.this.gitea.values().stream()
                    .map(GiteaUploader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.upload.Uploader::getName, identity()));
            }
            return gitea;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.upload.GitlabUploader> getGitlab() {
            if (null == gitlab) {
                gitlab = Upload.this.gitlab.values().stream()
                    .map(GitlabUploader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.upload.Uploader::getName, identity()));
            }
            return gitlab;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.upload.HttpUploader> getHttp() {
            if (null == http) {
                http = Upload.this.http.values().stream()
                    .map(HttpUploader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.upload.Uploader::getName, identity()));
            }
            return http;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.upload.S3Uploader> getS3() {
            if (null == s3) {
                s3 = Upload.this.s3.values().stream()
                    .map(S3Uploader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.upload.Uploader::getName, identity()));
            }
            return s3;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.upload.ScpUploader> getScp() {
            if (null == scp) {
                scp = Upload.this.scp.values().stream()
                    .map(ScpUploader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.upload.Uploader::getName, identity()));
            }
            return scp;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.upload.SftpUploader> getSftp() {
            if (null == sftp) {
                sftp = Upload.this.sftp.values().stream()
                    .map(SftpUploader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.upload.Uploader::getName, identity()));
            }
            return sftp;
        }

        @Override
        public Active getActive() {
            return Upload.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Upload.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Upload.this.asMap(full));
        }
    };

    public Upload() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.upload.Upload asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Upload source) {
        super.merge(source);
        setArtifactory(mergeModel(this.artifactory, source.artifactory));
        setFtp(mergeModel(this.ftp, source.ftp));
        setGitea(mergeModel(this.gitea, source.gitea));
        setGitlab(mergeModel(this.gitlab, source.gitlab));
        setHttp(mergeModel(this.http, source.http));
        setS3(mergeModel(this.s3, source.s3));
        setScp(mergeModel(this.scp, source.scp));
        setSftp(mergeModel(this.sftp, source.sftp));
    }

    @Deprecated
    @JsonPropertyDescription("upload.enabled is deprecated since 1.1.0 and will be removed in 2.0.0")
    public void setEnabled(Boolean enabled) {
        nag("upload.enabled is deprecated since 1.1.0 and will be removed in 2.0.0");
        if (null != enabled) {
            setActive(enabled ? Active.ALWAYS : Active.NEVER);
        }
    }

    public Optional<? extends Uploader<?>> getUploader(String type, String name) {
        switch (type) {
            case org.jreleaser.model.api.upload.ArtifactoryUploader.TYPE:
                return Optional.ofNullable(artifactory.get(name));
            case org.jreleaser.model.api.upload.FtpUploader.TYPE:
                return Optional.ofNullable(ftp.get(name));
            case org.jreleaser.model.api.upload.GiteaUploader.TYPE:
                return Optional.ofNullable(gitea.get(name));
            case org.jreleaser.model.api.upload.GitlabUploader.TYPE:
                return Optional.ofNullable(gitlab.get(name));
            case org.jreleaser.model.api.upload.HttpUploader.TYPE:
                return Optional.ofNullable(http.get(name));
            case org.jreleaser.model.api.upload.S3Uploader.TYPE:
                return Optional.ofNullable(s3.get(name));
            case org.jreleaser.model.api.upload.ScpUploader.TYPE:
                return Optional.ofNullable(scp.get(name));
            case org.jreleaser.model.api.upload.SftpUploader.TYPE:
                return Optional.ofNullable(sftp.get(name));
            default:
                return Optional.empty();
        }
    }

    public Optional<? extends Uploader<?>> getActiveUploader(String type, String name) {
        switch (type) {
            case org.jreleaser.model.api.upload.ArtifactoryUploader.TYPE:
                return getActiveArtifactory(name);
            case org.jreleaser.model.api.upload.FtpUploader.TYPE:
                return getActiveFtp(name);
            case org.jreleaser.model.api.upload.GiteaUploader.TYPE:
                return getActiveGitea(name);
            case org.jreleaser.model.api.upload.GitlabUploader.TYPE:
                return getActiveGitlab(name);
            case org.jreleaser.model.api.upload.HttpUploader.TYPE:
                return getActiveHttp(name);
            case org.jreleaser.model.api.upload.S3Uploader.TYPE:
                return getActiveS3(name);
            case org.jreleaser.model.api.upload.ScpUploader.TYPE:
                return getActiveScp(name);
            case org.jreleaser.model.api.upload.SftpUploader.TYPE:
                return getActiveSftp(name);
            default:
                return Optional.empty();
        }
    }

    public Optional<ArtifactoryUploader> getActiveArtifactory(String name) {
        return artifactory.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<FtpUploader> getActiveFtp(String name) {
        return ftp.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<GiteaUploader> getActiveGitea(String name) {
        return gitea.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<GitlabUploader> getActiveGitlab(String name) {
        return gitlab.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<HttpUploader> getActiveHttp(String name) {
        return http.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<S3Uploader> getActiveS3(String name) {
        return s3.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<ScpUploader> getActiveScp(String name) {
        return scp.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<SftpUploader> getActiveSftp(String name) {
        return sftp.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public List<ArtifactoryUploader> getActiveArtifactories() {
        return artifactory.values().stream()
            .filter(ArtifactoryUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, ArtifactoryUploader> getArtifactory() {
        return artifactory;
    }

    public void setArtifactory(Map<String, ArtifactoryUploader> artifactory) {
        this.artifactory.clear();
        this.artifactory.putAll(artifactory);
    }

    public void addArtifactory(ArtifactoryUploader artifactory) {
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

    public List<GiteaUploader> getActiveGiteas() {
        return gitea.values().stream()
            .filter(GiteaUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, GiteaUploader> getGitea() {
        return gitea;
    }

    public void setGitea(Map<String, GiteaUploader> gitea) {
        this.gitea.clear();
        this.gitea.putAll(gitea);
    }

    public void addGitea(GiteaUploader gitea) {
        this.gitea.put(gitea.getName(), gitea);
    }

    public List<GitlabUploader> getActiveGitlabs() {
        return gitlab.values().stream()
            .filter(GitlabUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, GitlabUploader> getGitlab() {
        return gitlab;
    }

    public void setGitlab(Map<String, GitlabUploader> gitlab) {
        this.gitlab.clear();
        this.gitlab.putAll(gitlab);
    }

    public void addGitlab(GitlabUploader gitlab) {
        this.gitlab.put(gitlab.getName(), gitlab);
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

    public List<S3Uploader> getActiveS3s() {
        return s3.values().stream()
            .filter(S3Uploader::isEnabled)
            .collect(toList());
    }

    public Map<String, S3Uploader> getS3() {
        return s3;
    }

    public void setS3(Map<String, S3Uploader> s3) {
        this.s3.clear();
        this.s3.putAll(s3);
    }

    public void addS3(S3Uploader s3) {
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

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());

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

        List<Map<String, Object>> gitea = this.gitea.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!gitea.isEmpty()) map.put("gitea", gitea);

        List<Map<String, Object>> gitlab = this.gitlab.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!gitlab.isEmpty()) map.put("gitlab", gitlab);

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

        return map;
    }

    public <A extends Uploader<?>> Map<String, A> findUploadersByType(String uploaderType) {
        switch (uploaderType) {
            case org.jreleaser.model.api.upload.ArtifactoryUploader.TYPE:
                return (Map<String, A>) artifactory;
            case org.jreleaser.model.api.upload.FtpUploader.TYPE:
                return (Map<String, A>) ftp;
            case org.jreleaser.model.api.upload.GiteaUploader.TYPE:
                return (Map<String, A>) gitea;
            case org.jreleaser.model.api.upload.GitlabUploader.TYPE:
                return (Map<String, A>) gitlab;
            case org.jreleaser.model.api.upload.HttpUploader.TYPE:
                return (Map<String, A>) http;
            case org.jreleaser.model.api.upload.S3Uploader.TYPE:
                return (Map<String, A>) s3;
            case org.jreleaser.model.api.upload.ScpUploader.TYPE:
                return (Map<String, A>) scp;
            case org.jreleaser.model.api.upload.SftpUploader.TYPE:
                return (Map<String, A>) sftp;
            default:
                return Collections.emptyMap();
        }
    }

    public <A extends Uploader<?>> List<A> findAllActiveUploaders() {
        List<A> uploaders = new ArrayList<>();
        uploaders.addAll((List<A>) getActiveArtifactories());
        uploaders.addAll((List<A>) getActiveFtps());
        uploaders.addAll((List<A>) getActiveGiteas());
        uploaders.addAll((List<A>) getActiveGitlabs());
        uploaders.addAll((List<A>) getActiveHttps());
        uploaders.addAll((List<A>) getActiveS3s());
        uploaders.addAll((List<A>) getActiveScps());
        uploaders.addAll((List<A>) getActiveSftps());
        return uploaders;
    }

    public Map<String, String> resolveDownloadUrls(JReleaserContext context, Distribution distribution, Artifact artifact, String prefix) {
        Map<String, String> urls = new LinkedHashMap<>();

        List<Uploader<?>> uploaders = findAllActiveUploaders();
        for (Uploader<?> uploader : uploaders) {
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
            Uploader<?> uploader = uploaders.get(0);
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
}
