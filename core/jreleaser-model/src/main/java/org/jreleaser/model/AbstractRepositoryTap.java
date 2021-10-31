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
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractRepositoryTap implements RepositoryTap {
    protected String basename;
    protected String tapName;
    protected String owner;
    protected String name;
    protected String branch;
    protected String username;
    protected String token;

    AbstractRepositoryTap(String basename, String tapName) {
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

    void setAll(AbstractRepositoryTap tap) {
        this.owner = tap.owner;
        this.name = tap.name;
        this.branch = tap.branch;
        this.username = tap.username;
        this.token = tap.token;
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
    public String getResolvedUsername(GitService service) {
        return Env.resolve(Env.toVar(basename + "_"
            + service.getServiceName()) + "_USERNAME", username);
    }

    @Override
    public String getResolvedToken(GitService service) {
        return Env.resolve(Env.toVar(basename + "_"
            + service.getServiceName()) + "_TOKEN", token);
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
    public String getBranch() {
        return branch;
    }

    @Override
    public void setBranch(String branch) {
        this.branch = branch;
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
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("owner", owner);
        map.put("name", getResolvedName());
        map.put("branch", branch);
        map.put("username", username);
        map.put("token", isNotBlank(token) ? HIDE : UNSET);
        return map;
    }
}
