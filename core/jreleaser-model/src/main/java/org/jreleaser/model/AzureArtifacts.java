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

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jreleaser.util.Env;
import org.jreleaser.util.Templates;

/**
 * @author JIHUN KIM
 * @since 1.1.0
 */
public class AzureArtifacts extends AbstractUploader {
    public static final String TYPE = "azureartifacts";

    private String host;
    private String username;
    private String personalAccessToken;
    private String project;
    private String organization;
    private String feed;
    private String path;

    public AzureArtifacts() {
        super(TYPE);
    }

    void setAll(AzureArtifacts azureArtifacts) {
        super.setAll(azureArtifacts);
        this.host = azureArtifacts.host;
        this.username = azureArtifacts.username;
        this.personalAccessToken= azureArtifacts.personalAccessToken;
        this.project = azureArtifacts.project;
        this.organization = azureArtifacts.organization;
        this.feed = azureArtifacts.feed;
        this.path = azureArtifacts.path;
    }

    public String getResolvedHost() {
        return Env.resolve("AZURE_ARTIFACTS_" + Env.toVar(name) + "_HOST", host);
    }

    public String getResolvedUsername() {
        return Env.resolve("AZURE_ARTIFACTS_" + Env.toVar(name) + "_USERNAME", username);
    }

    public String getResolvedPersonalAccessToken() {
        return Env.resolve("AZURE_ARTIFACTS_" + Env.toVar(name) + "_TOKEN", personalAccessToken);
    }

    public String getResolvedProject() {
        return Env.resolve("AZURE_ARTIFACTS_" + Env.toVar(name) + "_PROJECT", project);
    }

    public String getResolvedOrganization() {
        return Env.resolve("AZURE_ARTIFACTS_" + Env.toVar(name) + "_ORGANIZATION", organization);
    }
    
    public String getResolvedFeed() {
        return Env.resolve("AZURE_ARTIFACTS_" + Env.toVar(name) + "_FEED", feed);
    }

    public String getResolvedPath() {
        return Env.resolve("AZURE_ARTIFACTS_" + Env.toVar(name) + "_PATH", path);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("host", getResolvedHost());
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("personalAccessToken", isNotBlank(getResolvedPersonalAccessToken()) ? HIDE : UNSET);
        props.put("project", getResolvedProject());
        props.put("organization", getResolvedOrganization());
        props.put("feed", getResolvedFeed());
        props.put("path", getResolvedPath());
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        return resolveUrl(context.fullProps(), artifact);
    }
    
    @Override
    public String getResolvedDownloadUrl(Map<String,Object> props, Artifact artifact) {
        return resolveUrl(props, artifact);
    }

    public String getResolvedUploadUrl(JReleaserContext context, Artifact artifact) {
        return resolveUrl(context.fullProps(), artifact);
    }
    

    private String resolveUrl(Map<String,Object> props, Artifact artifact) {
        Map<String, Object> p = new LinkedHashMap<>(artifactProps(props, artifact));
        p.put("azuredevopsHost", host);
        p.put("organization", organization);
        p.put("project", project);
        p.put("feed", feed);
        p.put("path", path);

        String url = "{{azuredevopsHost}}/{{organization}}/{{project}}/_packaging/{{feed}}/maven/v1/{{path}}";
                
        return Templates.resolveTemplate(url, p);
    }
}
