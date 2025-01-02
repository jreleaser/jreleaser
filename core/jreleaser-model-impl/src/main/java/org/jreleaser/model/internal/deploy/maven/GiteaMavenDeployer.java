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
public final class GiteaMavenDeployer extends AbstractMavenDeployer<GiteaMavenDeployer, org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer> {
    private static final long serialVersionUID = -5441090984288035705L;

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer() {
        private static final long serialVersionUID = -5072992326711451976L;

        private Set<? extends org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride> artifactOverrides;

        @Override
        public String getGroup() {
            return org.jreleaser.model.api.deploy.maven.MavenDeployer.GROUP;
        }

        @Override
        public String getUrl() {
            return GiteaMavenDeployer.this.getUrl();
        }

        @Override
        public String getUsername() {
            return GiteaMavenDeployer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return GiteaMavenDeployer.this.getPassword();
        }

        @Override
        public Http.Authorization getAuthorization() {
            return GiteaMavenDeployer.this.getAuthorization();
        }

        @Override
        public boolean isSign() {
            return GiteaMavenDeployer.this.isSign();
        }

        @Override
        public boolean isChecksums() {
            return GiteaMavenDeployer.this.isChecksums();
        }

        @Override
        public boolean isSourceJar() {
            return GiteaMavenDeployer.this.isSourceJar();
        }

        @Override
        public boolean isJavadocJar() {
            return GiteaMavenDeployer.this.isJavadocJar();
        }

        @Override
        public boolean isVerifyPom() {
            return GiteaMavenDeployer.this.isVerifyPom();
        }

        @Override
        public boolean isApplyMavenCentralRules() {
            return GiteaMavenDeployer.this.isApplyMavenCentralRules();
        }

        @Override
        public List<String> getStagingRepositories() {
            return unmodifiableList(GiteaMavenDeployer.this.getStagingRepositories());
        }

        @Override
        public Set<? extends org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride> getArtifactOverrides() {
            if (null == artifactOverrides) {
                artifactOverrides = GiteaMavenDeployer.this.getArtifactOverrides().stream()
                    .map(MavenDeployer.ArtifactOverride::asImmutable)
                    .collect(toSet());
            }
            return artifactOverrides;
        }

        @Override
        public String getType() {
            return GiteaMavenDeployer.this.getType();
        }

        @Override
        public String getName() {
            return GiteaMavenDeployer.this.getName();
        }

        @Override
        public Active getActive() {
            return GiteaMavenDeployer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return GiteaMavenDeployer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GiteaMavenDeployer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return GiteaMavenDeployer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(GiteaMavenDeployer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return GiteaMavenDeployer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return GiteaMavenDeployer.this.getReadTimeout();
        }
    };

    public GiteaMavenDeployer() {
        super(org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer.TYPE);
    }

    @Override
    public org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer asImmutable() {
        return immutable;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        // noop
    }

    @Override
    public Http.Authorization resolveAuthorization() {
        setAuthorization(Http.Authorization.BEARER);
        return getAuthorization();
    }
}
