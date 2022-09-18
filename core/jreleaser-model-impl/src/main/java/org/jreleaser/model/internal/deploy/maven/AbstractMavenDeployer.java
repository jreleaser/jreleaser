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
package org.jreleaser.model.internal.deploy.maven;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.Http;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public abstract class AbstractMavenDeployer<S extends AbstractMavenDeployer<S>> extends AbstractModelObject<S> implements MavenDeployer, ExtraProperties {
    @JsonIgnore
    protected final String type;
    protected final Map<String, Object> extraProperties = new LinkedHashMap<>();
    protected final List<String> stagingRepositories = new ArrayList<>();
    @JsonIgnore
    protected String name;
    @JsonIgnore
    protected boolean enabled;
    protected Active active;
    protected int connectTimeout;
    protected int readTimeout;
    protected Boolean sign;
    protected Boolean verifyPom;
    protected Boolean applyMavenCentralRules;
    protected String url;
    protected String username;
    protected String password;
    protected Http.Authorization authorization;

    protected AbstractMavenDeployer(String type) {
        this.type = type;
    }

    @Override
    public void merge(S source) {
        this.active = merge(this.active, source.active);
        this.enabled = merge(this.enabled, source.enabled);
        this.name = merge(this.name, source.name);
        this.connectTimeout = merge(this.connectTimeout, source.connectTimeout);
        this.readTimeout = merge(this.readTimeout, source.readTimeout);
        this.sign = merge(this.sign, source.sign);
        this.verifyPom = merge(this.verifyPom, source.verifyPom);
        this.applyMavenCentralRules = merge(this.applyMavenCentralRules, source.applyMavenCentralRules);
        this.url = merge(this.url, source.url);
        this.username = merge(this.username, source.username);
        this.password = merge(this.password, source.password);
        this.authorization = merge(this.authorization, source.authorization);
        setExtraProperties(merge(this.extraProperties, source.extraProperties));
        setStagingRepositories(merge(this.stagingRepositories, source.stagingRepositories));
    }

    @Override
    public String getPrefix() {
        return type;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    @Override
    public boolean resolveEnabled(Project project) {
        if (null == active) {
            active = Active.NEVER;
        }
        enabled = active.check(project);
        if (project.isSnapshot()) {
            enabled = false;
        }
        return enabled;
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
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
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
    public String getType() {
        return type;
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
    public boolean isSign() {
        return sign != null && sign;
    }

    @Override
    public void setSign(Boolean sign) {
        this.sign = sign;
    }

    @Override
    public boolean isSignSet() {
        return sign != null;
    }

    @Override
    public boolean isVerifyPom() {
        return verifyPom != null && verifyPom;
    }

    @Override
    public void setVerifyPom(Boolean verifyPom) {
        this.verifyPom = verifyPom;
    }

    @Override
    public boolean isVerifyPomSet() {
        return verifyPom != null;
    }

    @Override
    public boolean isApplyMavenCentralRules() {
        return applyMavenCentralRules != null && applyMavenCentralRules;
    }

    @Override
    public void setApplyMavenCentralRules(Boolean applyMavenCentralRules) {
        this.applyMavenCentralRules = applyMavenCentralRules;
    }

    @Override
    public boolean isApplyMavenCentralRulesSet() {
        return applyMavenCentralRules != null;
    }

    @Override
    public List<String> getStagingRepositories() {
        return stagingRepositories;
    }

    @Override
    public void setStagingRepositories(List<String> stagingRepositories) {
        this.stagingRepositories.clear();
        this.stagingRepositories.addAll(stagingRepositories);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
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
    public Http.Authorization getAuthorization() {
        return authorization;
    }

    @Override
    public void setAuthorization(Http.Authorization authorization) {
        this.authorization = authorization;
    }

    @Override
    public void setAuthorization(String authorization) {
        this.authorization = Http.Authorization.of(authorization);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", active);
        props.put("connectTimeout", connectTimeout);
        props.put("readTimeout", readTimeout);
        props.put("authorization", authorization);
        props.put("url", url);
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
        props.put("sign", isSign());
        props.put("verifyPom", isVerifyPom());
        props.put("applyMavenCentralRules", isApplyMavenCentralRules());
        props.put("stagingRepositories", stagingRepositories);
        asMap(full, props);
        props.put("extraProperties", getResolvedExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(this.getName(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);

    public String getResolvedUrl(Map<String, Object> props) {
        props.put("username", getResolvedUsername());
        props.put("owner", getResolvedUsername());
        props.putAll(getExtraProperties());
        return resolveTemplate(url, props);
    }

    public String getResolvedUsername() {
        return Env.env(getType().toUpperCase(Locale.ENGLISH) + "_" + Env.toVar(name) + "_USERNAME", username);
    }

    public String getResolvedPassword() {
        return Env.env(getType().toUpperCase(Locale.ENGLISH) + "_" + Env.toVar(name) + "_PASSWORD", password);
    }

    @Override
    public Http.Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = Http.Authorization.BASIC;
        }

        return authorization;
    }
}
