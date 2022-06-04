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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractRepositoryTap<S extends AbstractRepositoryTap<S>> extends AbstractModelObject<S> implements RepositoryTap {
    protected Active active;
    @JsonIgnore
    protected boolean enabled;
    @JsonIgnore
    protected String basename;
    @JsonIgnore
    protected String tapName;
    protected String owner;
    protected String name;
    protected String tagName;
    protected String branch;
    protected String username;
    protected String token;
    protected String commitMessage;

    AbstractRepositoryTap(String basename, String tapName) {
        this.basename = basename;
        this.tapName = tapName;
    }

    @Override
    public String getBasename() {
        return basename;
    }

    public void setTapName(String tapName) {
        freezeCheck();
        this.tapName = tapName;
    }

    @Override
    public void merge(S tap) {
        freezeCheck();
        this.active = merge(this.active, tap.active);
        this.enabled = merge(this.enabled, tap.enabled);
        this.owner = merge(this.owner, tap.owner);
        this.name = merge(this.name, tap.name);
        this.tagName = merge(this.tagName, tap.tagName);
        this.branch = merge(this.branch, tap.branch);
        this.username = merge(this.username, tap.username);
        this.token = merge(this.token, tap.token);
        this.commitMessage = merge(this.commitMessage, tap.commitMessage);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            active = Active.RELEASE;
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
    public String getResolvedCommitMessage(Map<String, Object> props) {
        return resolveTemplate(commitMessage, props);
    }

    @Override
    public String getResolvedTagName(Map<String, Object> props) {
        return resolveTemplate(tagName, props);
    }

    @Override
    public String getResolvedUsername(GitService service) {
        return Env.env(Env.toVar(basename + "_"
            + service.getServiceName()) + "_USERNAME", username);
    }

    @Override
    public String getResolvedToken(GitService service) {
        return Env.env(Env.toVar(basename + "_"
            + service.getServiceName()) + "_TOKEN", token);
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        freezeCheck();
        this.owner = owner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        freezeCheck();
        this.name = name;
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public void setTagName(String tagName) {
        freezeCheck();
        this.tagName = tagName;
    }

    @Override
    public String getBranch() {
        return branch;
    }

    @Override
    public void setBranch(String branch) {
        freezeCheck();
        this.branch = branch;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        freezeCheck();
        this.username = username;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        freezeCheck();
        this.token = token;
    }

    @Override
    public String getCommitMessage() {
        return commitMessage;
    }

    @Override
    public void setCommitMessage(String commitMessage) {
        freezeCheck();
        this.commitMessage = commitMessage;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);
        map.put("owner", owner);
        map.put("name", getResolvedName());
        map.put("tagName", tagName);
        map.put("branch", branch);
        map.put("username", username);
        map.put("token", isNotBlank(token) ? HIDE : UNSET);
        map.put("commitMessage", commitMessage);
        return map;
    }
}
