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
package org.jreleaser.maven.plugin;

import java.util.Objects;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Registry implements Comparable<Registry> {
    protected String server;
    protected String serverName = "DEFAULT";
    protected String repositoryName;
    protected String username;
    protected String password;

    void setAll(Registry registry) {
        this.server = registry.server;
        this.serverName = registry.serverName;
        this.repositoryName = registry.repositoryName;
        this.username = registry.username;
        this.password = registry.password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int compareTo(Registry o) {
        if (null == o) return -1;
        return serverName.compareTo(o.serverName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Registry that = (Registry) o;
        return serverName.equals(that.serverName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverName);
    }
}
