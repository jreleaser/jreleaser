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
import org.jreleaser.mustache.TemplateContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.18.0
 */
public final class Nexus3MavenDeployer extends AbstractMavenDeployer<Nexus3MavenDeployer, org.jreleaser.model.api.deploy.maven.Nexus3MavenDeployer> {
    private static final long serialVersionUID = -459043430947610014L;

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.maven.Nexus3MavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.Nexus3MavenDeployer() {
        private static final long serialVersionUID = 4388421534842190207L;

        private Set<? extends ArtifactOverride> artifactOverrides;

        @Override
        public String getGroup() {
            return org.jreleaser.model.api.deploy.maven.MavenDeployer.GROUP;
        }

        @Override
        public String getUrl() {
            return Nexus3MavenDeployer.this.getUrl();
        }

        @Override
        public String getUsername() {
            return Nexus3MavenDeployer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return Nexus3MavenDeployer.this.getPassword();
        }

        @Override
        public Http.Authorization getAuthorization() {
            return Nexus3MavenDeployer.this.getAuthorization();
        }

        @Override
        public boolean isSign() {
            return Nexus3MavenDeployer.this.isSign();
        }

        @Override
        public boolean isChecksums() {
            return Nexus3MavenDeployer.this.isChecksums();
        }

        @Override
        public boolean isSourceJar() {
            return Nexus3MavenDeployer.this.isSourceJar();
        }

        @Override
        public boolean isJavadocJar() {
            return Nexus3MavenDeployer.this.isJavadocJar();
        }

        @Override
        public boolean isVerifyPom() {
            return Nexus3MavenDeployer.this.isVerifyPom();
        }

        @Override
        public boolean isApplyMavenCentralRules() {
            return Nexus3MavenDeployer.this.isApplyMavenCentralRules();
        }

        @Override
        public List<String> getStagingRepositories() {
            return unmodifiableList(Nexus3MavenDeployer.this.getStagingRepositories());
        }

        @Override
        public Set<? extends ArtifactOverride> getArtifactOverrides() {
            if (null == artifactOverrides) {
                artifactOverrides = Nexus3MavenDeployer.this.getArtifactOverrides().stream()
                    .map(MavenDeployer.ArtifactOverride::asImmutable)
                    .collect(toSet());
            }
            return artifactOverrides;
        }

        @Override
        public String getType() {
            return Nexus3MavenDeployer.this.getType();
        }

        @Override
        public String getName() {
            return Nexus3MavenDeployer.this.getName();
        }

        @Override
        public String getServerRef() {
            return Nexus3MavenDeployer.this.getServerRef();
        }

        @Override
        public Active getActive() {
            return Nexus3MavenDeployer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Nexus3MavenDeployer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Nexus3MavenDeployer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return Nexus3MavenDeployer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(Nexus3MavenDeployer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return Nexus3MavenDeployer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return Nexus3MavenDeployer.this.getReadTimeout();
        }
    };

    public Nexus3MavenDeployer() {
        super(org.jreleaser.model.api.deploy.maven.Nexus3MavenDeployer.TYPE, false);
    }

    @Override
    public org.jreleaser.model.api.deploy.maven.Nexus3MavenDeployer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Nexus3MavenDeployer source) {
        super.merge(source);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {

    }

    @Override
    public String getResolvedUrl(TemplateContext props) {
        props.set("username", getUsername());
        props.set("owner", getUsername());
        props.setAll(getExtraProperties());
        return resolveTemplate(getUrl(), props);
    }
}
