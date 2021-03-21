/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractRepositoryTap implements RepositoryTap {
    private String basename;
    private String owner;
    private String name;
    private String username;
    private String token;

    AbstractRepositoryTap(String basename) {
        this.basename = basename;
    }

    void setBasename(String basename) {
        this.basename = basename;
    }

    void setAll(AbstractRepositoryTap tap) {
        this.owner = tap.owner;
        this.name = tap.name;
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
        return basename;
    }

    @Override
    public String getResolvedToken(GitService service) {
        if (isNotBlank(token)) {
            return token;
        }
        return System.getenv(basename.toUpperCase().replaceAll("-", "_") + "_" + service.getName().toUpperCase() + "_TOKEN");
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
    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("owner", owner);
        map.put("name", getResolvedName());
        map.put("username", username);
        map.put("token", isNotBlank(token) ? "************" : "**unset**");
        return map;
    }
}
