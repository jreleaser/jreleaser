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

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class ArtifactoryMavenDeployer extends AbstractMavenDeployer<ArtifactoryMavenDeployer, org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer> {
    private static final long serialVersionUID = 2876306953402604076L;

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer() {
        private static final long serialVersionUID = 2401988830267833191L;

        private Set<? extends org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride> artifactOverrides;

        @Override
        public String getGroup() {
            return org.jreleaser.model.api.deploy.maven.MavenDeployer.GROUP;
        }

        @Override
        public String getUrl() {
            return ArtifactoryMavenDeployer.this.getUrl();
        }

        @Override
        public String getUsername() {
            return ArtifactoryMavenDeployer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return ArtifactoryMavenDeployer.this.getPassword();
        }

        @Override
        public Http.Authorization getAuthorization() {
            return ArtifactoryMavenDeployer.this.getAuthorization();
        }

        @Override
        public boolean isSign() {
            return ArtifactoryMavenDeployer.this.isSign();
        }

        @Override
        public boolean isChecksums() {
            return ArtifactoryMavenDeployer.this.isChecksums();
        }

        @Override
        public boolean isSourceJar() {
            return ArtifactoryMavenDeployer.this.isSourceJar();
        }

        @Override
        public boolean isJavadocJar() {
            return ArtifactoryMavenDeployer.this.isJavadocJar();
        }

        @Override
        public boolean isVerifyPom() {
            return ArtifactoryMavenDeployer.this.isVerifyPom();
        }

        @Override
        public boolean isApplyMavenCentralRules() {
            return ArtifactoryMavenDeployer.this.isApplyMavenCentralRules();
        }

        @Override
        public List<String> getStagingRepositories() {
            return unmodifiableList(ArtifactoryMavenDeployer.this.getStagingRepositories());
        }

        @Override
        public Set<? extends org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride> getArtifactOverrides() {
            if (null == artifactOverrides) {
                artifactOverrides = ArtifactoryMavenDeployer.this.getArtifactOverrides().stream()
                    .map(MavenDeployer.ArtifactOverride::asImmutable)
                    .collect(toSet());
            }
            return artifactOverrides;
        }

        @Override
        public String getType() {
            return ArtifactoryMavenDeployer.this.getType();
        }

        @Override
        public String getName() {
            return ArtifactoryMavenDeployer.this.getName();
        }

        @Override
        public Active getActive() {
            return ArtifactoryMavenDeployer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ArtifactoryMavenDeployer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ArtifactoryMavenDeployer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ArtifactoryMavenDeployer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ArtifactoryMavenDeployer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return ArtifactoryMavenDeployer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return ArtifactoryMavenDeployer.this.getReadTimeout();
        }
    };

    public ArtifactoryMavenDeployer() {
        super(org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer.TYPE);
    }

    @Override
    public org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer asImmutable() {
        return immutable;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        // noop
    }

    @Override
    public Http.Authorization resolveAuthorization() {
        if (null == getAuthorization()) {
            setAuthorization(Http.Authorization.BEARER);
        }

        return getAuthorization();
    }
}
