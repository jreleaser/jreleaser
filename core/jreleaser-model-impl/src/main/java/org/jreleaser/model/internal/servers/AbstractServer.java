/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
package org.jreleaser.model.internal.servers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractModelObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
public abstract class AbstractServer<A extends org.jreleaser.model.api.servers.Server, S extends AbstractServer<A, S>> extends AbstractModelObject<S> implements Server<A> {
    private static final long serialVersionUID = -5413158670428652917L;

    @JsonIgnore
    private String name;
    private String host;
    private String username;
    private String password;
    private Integer port;
    private Integer connectTimeout;
    private Integer readTimeout;

    @Override
    public void merge(S source) {
        this.name = merge(this.name, source.getName());
        this.host = merge(this.host, source.getHost());
        this.username = merge(this.username, source.getUsername());
        this.password = merge(this.password, source.getPassword());
        this.port = merge(this.port, source.getPort());
        this.connectTimeout = merge(this.connectTimeout, source.getConnectTimeout());
        this.readTimeout = merge(this.readTimeout, source.getReadTimeout());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
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
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Integer getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("host", host);
        props.put("port", getPort());
        props.put("username", isNotBlank(username) ? HIDE : UNSET);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
        props.put("connectTimeout", connectTimeout);
        props.put("readTimeout", readTimeout);
        asMap(full, props);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(this.getName(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);

    public <T extends Server<?>> void mergeWith(T other) {
        setName(merge(other.getName(), getName()));
        setHost(merge(other.getHost(), getHost()));
        setPort(merge(other.getPort(), getPort()));
        setUsername(merge(other.getUsername(), getUsername()));
        setPassword(merge(other.getPassword(), getPassword()));
        setConnectTimeout(merge(other.getConnectTimeout(), getConnectTimeout()));
        setReadTimeout(merge(other.getReadTimeout(), getReadTimeout()));
    }
}
