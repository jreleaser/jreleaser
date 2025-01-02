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
import org.jreleaser.model.Active;
import org.jreleaser.model.Http;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.12.0
 */
public final class MavenCentralMavenDeployer extends AbstractMavenDeployer<MavenCentralMavenDeployer, org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer> {
    private static final long serialVersionUID = -7077345304257049811L;

    private org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage stage;
    private String namespace;
    private String deploymentId;
    private int retryDelay;
    private int maxRetries;
    private String verifyUrl;

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer() {
        private static final long serialVersionUID = 2746737103966334955L;

        private Set<? extends ArtifactOverride> artifactOverrides;

        @Override
        public Stage getStage() {
            return MavenCentralMavenDeployer.this.getStage();
        }

        @Override
        public String getNamespace() {
            return MavenCentralMavenDeployer.this.getNamespace();
        }

        @Override
        public String getDeploymentId() {
            return MavenCentralMavenDeployer.this.getDeploymentId();
        }

        @Override
        public Integer getRetryDelay() {
            return MavenCentralMavenDeployer.this.getRetryDelay();
        }

        @Override
        public Integer getMaxRetries() {
            return MavenCentralMavenDeployer.this.getMaxRetries();
        }

        @Override
        public String getVerifyUrl() {
            return verifyUrl;
        }

        @Override
        public String getGroup() {
            return org.jreleaser.model.api.deploy.maven.MavenDeployer.GROUP;
        }

        @Override
        public String getUrl() {
            return MavenCentralMavenDeployer.this.getUrl();
        }

        @Override
        public String getUsername() {
            return MavenCentralMavenDeployer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return MavenCentralMavenDeployer.this.getPassword();
        }

        @Override
        public Http.Authorization getAuthorization() {
            return MavenCentralMavenDeployer.this.getAuthorization();
        }

        @Override
        public boolean isSign() {
            return MavenCentralMavenDeployer.this.isSign();
        }

        @Override
        public boolean isChecksums() {
            return MavenCentralMavenDeployer.this.isChecksums();
        }

        @Override
        public boolean isSourceJar() {
            return MavenCentralMavenDeployer.this.isSourceJar();
        }

        @Override
        public boolean isJavadocJar() {
            return MavenCentralMavenDeployer.this.isJavadocJar();
        }

        @Override
        public boolean isVerifyPom() {
            return MavenCentralMavenDeployer.this.isVerifyPom();
        }

        @Override
        public boolean isApplyMavenCentralRules() {
            return MavenCentralMavenDeployer.this.isApplyMavenCentralRules();
        }

        @Override
        public List<String> getStagingRepositories() {
            return unmodifiableList(MavenCentralMavenDeployer.this.getStagingRepositories());
        }

        @Override
        public Set<? extends ArtifactOverride> getArtifactOverrides() {
            if (null == artifactOverrides) {
                artifactOverrides = MavenCentralMavenDeployer.this.getArtifactOverrides().stream()
                    .map(MavenDeployer.ArtifactOverride::asImmutable)
                    .collect(toSet());
            }
            return artifactOverrides;
        }

        @Override
        public String getType() {
            return MavenCentralMavenDeployer.this.getType();
        }

        @Override
        public String getName() {
            return MavenCentralMavenDeployer.this.getName();
        }

        @Override
        public Active getActive() {
            return MavenCentralMavenDeployer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return MavenCentralMavenDeployer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(MavenCentralMavenDeployer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return MavenCentralMavenDeployer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(MavenCentralMavenDeployer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return MavenCentralMavenDeployer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return MavenCentralMavenDeployer.this.getReadTimeout();
        }
    };

    public MavenCentralMavenDeployer() {
        super(org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.TYPE);
    }

    @Override
    public org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(MavenCentralMavenDeployer source) {
        super.merge(source);
        this.stage = merge(this.stage, source.stage);
        this.namespace = merge(this.namespace, source.namespace);
        this.deploymentId = merge(this.deploymentId, source.deploymentId);
        this.retryDelay = merge(this.retryDelay, source.retryDelay);
        this.maxRetries = merge(this.maxRetries, source.maxRetries);
        this.verifyUrl = merge(this.verifyUrl, source.verifyUrl);
    }

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public void setVerifyUrl(String verifyUrl) {
        this.verifyUrl = verifyUrl;
    }

    public org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage getStage() {
        return stage;
    }

    public void setStage(org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage stage) {
        this.stage = stage;
    }

    public void setStage(String stage) {
        if (isNotBlank(stage)) {
            this.stage = org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage.of(stage);
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Integer getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(Integer retryDelay) {
        this.retryDelay = retryDelay;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("stage", stage);
        props.put("namespace", namespace);
        props.put("deploymentId", deploymentId);
        props.put("verifyUrl", verifyUrl);
        props.put("retryDelay", retryDelay);
        props.put("maxRetries", maxRetries);
    }
}
