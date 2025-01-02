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
package org.jreleaser.model.internal.packagers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.mustache.TemplateContext;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractRepositoryTap<S extends AbstractRepositoryTap<S>> extends AbstractActivatable<S> implements RepositoryTap {
    private static final long serialVersionUID = 8737648708964053648L;

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    @JsonIgnore
    private final String basename;
    @JsonIgnore
    private String tapName;
    private String owner;
    private String name;
    private String tagName;
    private String branch;
    private String branchPush;
    private String username;
    private String token;
    private String commitMessage;

    protected AbstractRepositoryTap(String basename, String tapName) {
        this.basename = basename;
        this.tapName = tapName;
    }

    @Override
    public String getBasename() {
        return basename;
    }

    public void setTapName(String tapName) {
        this.tapName = tapName;
    }

    protected String getTapName() {
        return tapName;
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.owner = merge(this.owner, source.getOwner());
        this.name = merge(this.name, source.getName());
        this.tagName = merge(this.tagName, source.getTagName());
        this.branch = merge(this.branch, source.getBranch());
        this.branchPush = merge(this.branchPush, source.getBranchPush());
        this.username = merge(this.username, source.getUsername());
        this.token = merge(this.token, source.getToken());
        this.commitMessage = merge(this.commitMessage, source.getCommitMessage());
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
    }

    @Override
    public String getCanonicalRepoName() {
        return owner + "/" + getResolvedName();
    }

    @Override
    public String getResolvedName() {
        if (isNotBlank(name)) {
            return name;
        }
        return tapName;
    }

    @Override
    public String getResolvedCommitMessage(TemplateContext props) {
        return resolveTemplate(commitMessage, props);
    }

    @Override
    public String getResolvedTagName(TemplateContext props) {
        return resolveTemplate(tagName, props);
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
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
    public String getTagName() {
        return tagName;
    }

    @Override
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String getBranch() {
        return branch;
    }

    @Override
    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public String getBranchPush() {
        return branchPush;
    }

    @Override
    public void setBranchPush(String branchPush) {
        this.branchPush = branchPush;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getCommitMessage() {
        return commitMessage;
    }

    @Override
    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
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
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.put("owner", owner);
        map.put("name", getResolvedName());
        map.put("tagName", tagName);
        map.put("branch", branch);
        map.put("branchPush", branchPush);
        map.put("username", username);
        map.put("token", isNotBlank(token) ? HIDE : UNSET);
        map.put("commitMessage", commitMessage);
        map.put("extraProperties", getExtraProperties());
        return map;
    }
}
