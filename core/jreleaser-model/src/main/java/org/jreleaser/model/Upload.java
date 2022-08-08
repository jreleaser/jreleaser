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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.util.Env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.JReleaserOutput.nag;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Upload extends AbstractModelObject<Upload> implements Domain, Activatable {
    private final Map<String, Artifactory> artifactory = new LinkedHashMap<>();
    private final Map<String, FtpUploader> ftp = new LinkedHashMap<>();
    private final Map<String, GiteaUploader> gitea = new LinkedHashMap<>();
    private final Map<String, GitlabUploader> gitlab = new LinkedHashMap<>();
    private final Map<String, HttpUploader> http = new LinkedHashMap<>();
    private final Map<String, S3> s3 = new LinkedHashMap<>();
    private final Map<String, ScpUploader> scp = new LinkedHashMap<>();
    private final Map<String, SftpUploader> sftp = new LinkedHashMap<>();

    private Active active;
    @JsonIgnore
    private boolean enabled = true;

    @Override
    public void freeze() {
        super.freeze();
        artifactory.values().forEach(ModelObject::freeze);
        ftp.values().forEach(ModelObject::freeze);
        gitea.values().forEach(ModelObject::freeze);
        gitlab.values().forEach(ModelObject::freeze);
        http.values().forEach(ModelObject::freeze);
        s3.values().forEach(ModelObject::freeze);
        scp.values().forEach(ModelObject::freeze);
        sftp.values().forEach(ModelObject::freeze);
    }

    @Override
    public void merge(Upload upload) {
        freezeCheck();
        this.active = merge(this.active, upload.active);
        this.enabled = merge(this.enabled, upload.enabled);
        setArtifactory(mergeModel(this.artifactory, upload.artifactory));
        setFtp(mergeModel(this.ftp, upload.ftp));
        setGitea(mergeModel(this.gitea, upload.gitea));
        setGitlab(mergeModel(this.gitlab, upload.gitlab));
        setHttp(mergeModel(this.http, upload.http));
        setS3(mergeModel(this.s3, upload.s3));
        setScp(mergeModel(this.scp, upload.scp));
        setSftp(mergeModel(this.sftp, upload.sftp));
    }

    @Override
    public boolean isEnabled() {
        return enabled && active != null;
    }

    @Deprecated
    public void setEnabled(Boolean enabled) {
        nag("upload.enabled is deprecated since 1.1.0 and will be removed in 2.0.0");
        freezeCheck();
        if (null != enabled) {
            this.active = enabled ? Active.ALWAYS : Active.NEVER;
        }
    }

    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            setActive(Env.resolveOrDefault("upload.active", "", "ALWAYS"));
        }
        enabled = active.check(project);
        return enabled;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        freezeCheck();
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        setActive(Active.of(str));
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    public Optional<? extends Uploader> getUploader(String type, String name) {
        switch (type) {
            case Artifactory.TYPE:
                return Optional.ofNullable(artifactory.get(name));
            case FtpUploader.TYPE:
                return Optional.ofNullable(ftp.get(name));
            case GiteaUploader.TYPE:
                return Optional.ofNullable(gitea.get(name));
            case GitlabUploader.TYPE:
                return Optional.ofNullable(gitlab.get(name));
            case HttpUploader.TYPE:
                return Optional.ofNullable(http.get(name));
            case S3.TYPE:
                return Optional.ofNullable(s3.get(name));
            case ScpUploader.TYPE:
                return Optional.ofNullable(scp.get(name));
            case SftpUploader.TYPE:
                return Optional.ofNullable(sftp.get(name));
        }

        return Optional.empty();
    }

    public Optional<? extends Uploader> getActiveUploader(String type, String name) {
        switch (type) {
            case Artifactory.TYPE:
                return getActiveArtifactory(name);
            case FtpUploader.TYPE:
                return getActiveFtp(name);
            case GiteaUploader.TYPE:
                return getActiveGitea(name);
            case GitlabUploader.TYPE:
                return getActiveGitlab(name);
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

    public Optional<GiteaUploader> getActiveGitea(String name) {
        return gitea.values().stream()
            .filter(Uploader::isEnabled)
            .filter(a -> name.equals(a.name))
            .findFirst();
    }

    public Optional<GitlabUploader> getActiveGitlab(String name) {
        return gitlab.values().stream()
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

    public List<Artifactory> getActiveArtifactories() {
        return artifactory.values().stream()
            .filter(Artifactory::isEnabled)
            .collect(toList());
    }

    public Map<String, Artifactory> getArtifactory() {
        return freezeWrap(artifactory);
    }

    public void setArtifactory(Map<String, Artifactory> artifactory) {
        freezeCheck();
        this.artifactory.clear();
        this.artifactory.putAll(artifactory);
    }

    public void addArtifactory(Artifactory artifactory) {
        freezeCheck();
        this.artifactory.put(artifactory.getName(), artifactory);
    }

    public List<FtpUploader> getActiveFtps() {
        return ftp.values().stream()
            .filter(FtpUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, FtpUploader> getFtp() {
        return freezeWrap(ftp);
    }

    public void setFtp(Map<String, FtpUploader> ftp) {
        freezeCheck();
        this.ftp.clear();
        this.ftp.putAll(ftp);
    }

    public void addFtp(FtpUploader ftp) {
        freezeCheck();
        this.ftp.put(ftp.getName(), ftp);
    }

    public List<GiteaUploader> getActiveGiteas() {
        return gitea.values().stream()
            .filter(GiteaUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, GiteaUploader> getGitea() {
        return freezeWrap(gitea);
    }

    public void setGitea(Map<String, GiteaUploader> gitea) {
        freezeCheck();
        this.gitea.clear();
        this.gitea.putAll(gitea);
    }

    public void addGitea(GiteaUploader gitea) {
        freezeCheck();
        this.gitea.put(gitea.getName(), gitea);
    }

    public List<GitlabUploader> getActiveGitlabs() {
        return gitlab.values().stream()
            .filter(GitlabUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, GitlabUploader> getGitlab() {
        return freezeWrap(gitlab);
    }

    public void setGitlab(Map<String, GitlabUploader> gitlab) {
        freezeCheck();
        this.gitlab.clear();
        this.gitlab.putAll(gitlab);
    }

    public void addGitlab(GitlabUploader gitlab) {
        freezeCheck();
        this.gitlab.put(gitlab.getName(), gitlab);
    }

    public List<HttpUploader> getActiveHttps() {
        return http.values().stream()
            .filter(HttpUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, HttpUploader> getHttp() {
        return freezeWrap(http);
    }

    public void setHttp(Map<String, HttpUploader> http) {
        freezeCheck();
        this.http.clear();
        this.http.putAll(http);
    }

    public void addHttp(HttpUploader http) {
        freezeCheck();
        this.http.put(http.getName(), http);
    }

    public List<S3> getActiveS3s() {
        return s3.values().stream()
            .filter(S3::isEnabled)
            .collect(toList());
    }

    public Map<String, S3> getS3() {
        return freezeWrap(s3);
    }

    public void setS3(Map<String, S3> s3) {
        freezeCheck();
        this.s3.clear();
        this.s3.putAll(s3);
    }

    public void addS3(S3 s3) {
        freezeCheck();
        this.s3.put(s3.getName(), s3);
    }

    public List<ScpUploader> getActiveScps() {
        return scp.values().stream()
            .filter(ScpUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, ScpUploader> getScp() {
        return freezeWrap(scp);
    }

    public void setScp(Map<String, ScpUploader> scp) {
        freezeCheck();
        this.scp.clear();
        this.scp.putAll(scp);
    }

    public void addScp(ScpUploader scp) {
        freezeCheck();
        this.scp.put(scp.getName(), scp);
    }

    public List<SftpUploader> getActiveSftps() {
        return sftp.values().stream()
            .filter(SftpUploader::isEnabled)
            .collect(toList());
    }

    public Map<String, SftpUploader> getSftp() {
        return freezeWrap(sftp);
    }

    public void setSftp(Map<String, SftpUploader> sftp) {
        freezeCheck();
        this.sftp.clear();
        this.sftp.putAll(sftp);
    }

    public void addSftp(SftpUploader sftp) {
        freezeCheck();
        this.sftp.put(sftp.getName(), sftp);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", enabled);
        map.put("active", active);

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

    public <A extends Uploader> Map<String, A> findUploadersByType(String uploaderType) {
        switch (uploaderType) {
            case Artifactory.TYPE:
                return (Map<String, A>) artifactory;
            case FtpUploader.TYPE:
                return (Map<String, A>) ftp;
            case GiteaUploader.TYPE:
                return (Map<String, A>) gitea;
            case GitlabUploader.TYPE:
                return (Map<String, A>) gitlab;
            case HttpUploader.TYPE:
                return (Map<String, A>) http;
            case S3.TYPE:
                return (Map<String, A>) s3;
            case ScpUploader.TYPE:
                return (Map<String, A>) scp;
            case SftpUploader.TYPE:
                return (Map<String, A>) sftp;
        }

        return Collections.emptyMap();
    }

    public <A extends Uploader> List<A> findAllActiveUploaders() {
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
        set.add(GiteaUploader.TYPE);
        set.add(GitlabUploader.TYPE);
        set.add(HttpUploader.TYPE);
        set.add(S3.TYPE);
        set.add(ScpUploader.TYPE);
        set.add(SftpUploader.TYPE);
        return Collections.unmodifiableSet(set);
    }
}
