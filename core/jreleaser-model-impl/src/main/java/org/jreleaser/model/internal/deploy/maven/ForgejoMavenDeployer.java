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
 * @since 1.18.0
 */
public final class ForgejoMavenDeployer extends AbstractMavenDeployer<ForgejoMavenDeployer, org.jreleaser.model.api.deploy.maven.ForgejoMavenDeployer> {
    private static final long serialVersionUID = -8194747998466380252L;

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.maven.ForgejoMavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.ForgejoMavenDeployer() {
        private static final long serialVersionUID = -4836321171503796439L;

        private Set<? extends ArtifactOverride> artifactOverrides;

        @Override
        public String getGroup() {
            return org.jreleaser.model.api.deploy.maven.MavenDeployer.GROUP;
        }

        @Override
        public String getUrl() {
            return ForgejoMavenDeployer.this.getUrl();
        }

        @Override
        public String getUsername() {
            return ForgejoMavenDeployer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return ForgejoMavenDeployer.this.getPassword();
        }

        @Override
        public Http.Authorization getAuthorization() {
            return ForgejoMavenDeployer.this.getAuthorization();
        }

        @Override
        public boolean isSign() {
            return ForgejoMavenDeployer.this.isSign();
        }

        @Override
        public boolean isChecksums() {
            return ForgejoMavenDeployer.this.isChecksums();
        }

        @Override
        public boolean isSourceJar() {
            return ForgejoMavenDeployer.this.isSourceJar();
        }

        @Override
        public boolean isJavadocJar() {
            return ForgejoMavenDeployer.this.isJavadocJar();
        }

        @Override
        public boolean isVerifyPom() {
            return ForgejoMavenDeployer.this.isVerifyPom();
        }

        @Override
        public boolean isApplyMavenCentralRules() {
            return ForgejoMavenDeployer.this.isApplyMavenCentralRules();
        }

        @Override
        public List<String> getStagingRepositories() {
            return unmodifiableList(ForgejoMavenDeployer.this.getStagingRepositories());
        }

        @Override
        public Set<? extends ArtifactOverride> getArtifactOverrides() {
            if (null == artifactOverrides) {
                artifactOverrides = ForgejoMavenDeployer.this.getArtifactOverrides().stream()
                    .map(MavenDeployer.ArtifactOverride::asImmutable)
                    .collect(toSet());
            }
            return artifactOverrides;
        }

        @Override
        public String getType() {
            return ForgejoMavenDeployer.this.getType();
        }

        @Override
        public String getName() {
            return ForgejoMavenDeployer.this.getName();
        }

        @Override
        public String getServerRef() {
            return ForgejoMavenDeployer.this.getServerRef();
        }

        @Override
        public Active getActive() {
            return ForgejoMavenDeployer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ForgejoMavenDeployer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ForgejoMavenDeployer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ForgejoMavenDeployer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ForgejoMavenDeployer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return ForgejoMavenDeployer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return ForgejoMavenDeployer.this.getReadTimeout();
        }
    };

    public ForgejoMavenDeployer() {
        super(org.jreleaser.model.api.deploy.maven.ForgejoMavenDeployer.TYPE);
    }

    @Override
    public org.jreleaser.model.api.deploy.maven.ForgejoMavenDeployer asImmutable() {
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
