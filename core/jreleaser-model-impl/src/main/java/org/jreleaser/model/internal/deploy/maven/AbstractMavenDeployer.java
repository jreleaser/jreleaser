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
package org.jreleaser.model.internal.deploy.maven;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Http;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.mustache.TemplateContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public abstract class AbstractMavenDeployer<S extends AbstractMavenDeployer<S, A>, A extends org.jreleaser.model.api.deploy.maven.MavenDeployer> extends AbstractActivatable<S> implements MavenDeployer<A>, ExtraProperties {
    private static final long serialVersionUID = -5754230006047623161L;

    @JsonIgnore
    private final String type;
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final List<String> stagingRepositories = new ArrayList<>();
    private final Set<ArtifactOverride> artifactOverrides = new LinkedHashSet<>();
    @JsonIgnore
    private String name;
    private int connectTimeout;
    private int readTimeout;
    protected Boolean sign;
    protected Boolean checksums;
    protected Boolean sourceJar;
    protected Boolean javadocJar;
    protected Boolean verifyPom;
    protected Boolean applyMavenCentralRules;
    protected Boolean snapshotSupported;
    private String url;
    private String username;
    private String password;
    private Http.Authorization authorization;

    protected AbstractMavenDeployer(String type) {
        this(type, false);
    }

    protected AbstractMavenDeployer(String type, Boolean snapshotSupported) {
        this.type = type;
        this.snapshotSupported = snapshotSupported;
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.name = merge(this.name, source.getName());
        this.connectTimeout = merge(this.getConnectTimeout(), source.getConnectTimeout());
        this.readTimeout = merge(this.getReadTimeout(), source.getReadTimeout());
        this.sign = merge(this.sign, source.sign);
        this.checksums = merge(this.checksums, source.checksums);
        this.sourceJar = merge(this.sourceJar, source.sourceJar);
        this.javadocJar = merge(this.javadocJar, source.javadocJar);
        this.verifyPom = merge(this.verifyPom, source.verifyPom);
        this.applyMavenCentralRules = merge(this.applyMavenCentralRules, source.applyMavenCentralRules);
        this.url = merge(this.url, source.getUrl());
        this.username = merge(this.username, source.getUsername());
        this.password = merge(this.password, source.getPassword());
        this.authorization = merge(this.authorization, source.getAuthorization());
        this.snapshotSupported = merge(this.snapshotSupported, source.isSnapshotSupported());
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
        setStagingRepositories(merge(this.stagingRepositories, source.getStagingRepositories()));
        setArtifactOverrides(merge(this.artifactOverrides, source.getArtifactOverrides()));
    }

    @Override
    public String prefix() {
        return getType();
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
        return null != sign && sign;
    }

    @Override
    public void setSign(Boolean sign) {
        this.sign = sign;
    }

    @Override
    public boolean isSignSet() {
        return null != sign;
    }

    @Override
    public boolean isChecksums() {
        return null != checksums && checksums;
    }

    @Override
    public void setChecksums(Boolean checksum) {
        this.checksums = checksum;
    }

    @Override
    public boolean isChecksumsSet() {
        return null != checksums;
    }

    @Override
    public boolean isSourceJar() {
        return null == sourceJar || sourceJar;
    }

    @Override
    public void setSourceJar(Boolean sourceJar) {
        this.sourceJar = sourceJar;
    }

    @Override
    public boolean isSourceJarSet() {
        return null != sourceJar;
    }

    @Override
    public boolean isJavadocJar() {
        return null == javadocJar || javadocJar;
    }

    @Override
    public void setJavadocJar(Boolean javadocJar) {
        this.javadocJar = javadocJar;
    }

    @Override
    public boolean isJavadocJarSet() {
        return null != javadocJar;
    }

    @Override
    public boolean isVerifyPom() {
        return null != verifyPom && verifyPom;
    }

    @Override
    public void setVerifyPom(Boolean verifyPom) {
        this.verifyPom = verifyPom;
    }

    @Override
    public boolean isVerifyPomSet() {
        return null != verifyPom;
    }

    @Override
    public boolean isApplyMavenCentralRules() {
        return null != applyMavenCentralRules && applyMavenCentralRules;
    }

    @Override
    public void setApplyMavenCentralRules(Boolean applyMavenCentralRules) {
        this.applyMavenCentralRules = applyMavenCentralRules;
    }

    @Override
    public boolean isApplyMavenCentralRulesSet() {
        return null != applyMavenCentralRules;
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
    public Set<ArtifactOverride> getArtifactOverrides() {
        return artifactOverrides;
    }

    @Override
    public void setArtifactOverrides(Set<ArtifactOverride> artifactOverrides) {
        this.artifactOverrides.clear();
        this.artifactOverrides.addAll(artifactOverrides);
    }

    @Override
    public void addArtifactOverride(ArtifactOverride artifactOverride) {
        if (null != artifactOverride) {
            this.artifactOverrides.add(artifactOverride);
        }
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
    public boolean isSnapshotSupported() {
        return snapshotSupported;
    }

    @Override
    public void setSnapshotSupported(Boolean snapshotSupported) {
        this.snapshotSupported = snapshotSupported;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", getActive());
        props.put("connectTimeout", connectTimeout);
        props.put("readTimeout", readTimeout);
        props.put("authorization", authorization);
        props.put("url", url);
        props.put("username", isNotBlank(username) ? HIDE : UNSET);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
        props.put("sign", isSign());
        props.put("checksums", isChecksums());
        props.put("sourceJar", isSourceJar());
        props.put("javadocJar", isJavadocJar());
        props.put("verifyPom", isVerifyPom());
        props.put("applyMavenCentralRules", isApplyMavenCentralRules());
        props.put("snapshotSupported", isSnapshotSupported());
        props.put("stagingRepositories", stagingRepositories);
        Map<String, Map<String, Object>> mappedArtifacts = new LinkedHashMap<>();
        int i = 0;
        for (ArtifactOverride artifact : getArtifactOverrides()) {
            mappedArtifacts.put("artifact " + (i++), artifact.asMap(full));
        }
        props.put("artifactOverrides", mappedArtifacts);
        asMap(full, props);
        props.put("extraProperties", getExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(this.getName(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);

    @Override
    public String getResolvedUrl(TemplateContext props) {
        props.set("username", username);
        props.set("owner", username);
        props.setAll(getExtraProperties());
        return normalizeUrl(resolveTemplate(url, props));
    }

    protected String normalizeUrl(String url) {
        if (isNotBlank(url) && !url.endsWith("/")) return url + "/";
        return url;
    }

    @Override
    public Http.Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = Http.Authorization.BASIC;
        }

        return authorization;
    }

    @Override
    public List<String> keysFor(String property) {
        return listOf(
            "deploy.maven." + getType() + "." + getName() + "." + property,
            "deploy.maven." + getType() + "." + property,
            getType() + "." + getName() + "." + property,
            getType() + "." + property);
    }
}
