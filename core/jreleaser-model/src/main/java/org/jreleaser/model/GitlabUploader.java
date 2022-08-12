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

import org.jreleaser.util.Env;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class GitlabUploader extends AbstractUploader<GitlabUploader> {
    public static final String TYPE = "gitlab";

    private static final String DOWNLOAD_URL = "https://{{host}}/api/v4/projects/{{projectIdentifier}}/packages/generic/{{packageName}}/{{packageVersion}}/{{artifactFile}}";

    private String host;
    private String token;
    private String packageName;
    private String packageVersion;
    private String projectIdentifier;

    public GitlabUploader() {
        super(TYPE);
        setHost("gitlab.com");
    }

    @Override
    public void merge(GitlabUploader gitlab) {
        freezeCheck();
        super.merge(gitlab);
        this.host = merge(this.host, gitlab.host);
        this.token = merge(this.token, gitlab.token);
        this.packageName = merge(this.packageName, gitlab.packageName);
        this.packageVersion = merge(this.packageVersion, gitlab.packageVersion);
        this.projectIdentifier = merge(this.projectIdentifier, gitlab.projectIdentifier);
    }

    public String getResolvedToken() {
        return Env.env(listOf(
                "GITLAB_" + Env.toVar(name) + "_TOKEN",
                "GITLAB_TOKEN"),
            token);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        freezeCheck();
        this.host = host;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        freezeCheck();
        this.token = token;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        freezeCheck();
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        freezeCheck();
        this.packageVersion = packageVersion;
    }

    public String getProjectIdentifier() {
        return projectIdentifier;
    }

    public void setProjectIdentifier(String projectIdentifier) {
        freezeCheck();
        this.projectIdentifier = projectIdentifier;
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("host", host);
        props.put("token", isNotBlank(getResolvedToken()) ? HIDE : UNSET);
        props.put("packageName", packageName);
        props.put("packageVersion", packageVersion);
        props.put("projectIdentifier", projectIdentifier);
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        return getResolvedDownloadUrl(context.fullProps(), artifact);
    }

    @Override
    public String getResolvedDownloadUrl(Map<String, Object> props, Artifact artifact) {
        Map<String, Object> p = new LinkedHashMap<>(artifactProps(props, artifact));
        p.putAll(getResolvedExtraProperties());
        p.put("host", host);
        p.put("packageName", packageName);
        p.put("packageVersion", packageVersion);
        p.put("projectIdentifier", projectIdentifier);
        return resolveTemplate(DOWNLOAD_URL, p);
    }

    public String getResolvedUploadUrl(JReleaserContext context, Artifact artifact) {
        return getResolvedDownloadUrl(context, artifact);
    }
}
