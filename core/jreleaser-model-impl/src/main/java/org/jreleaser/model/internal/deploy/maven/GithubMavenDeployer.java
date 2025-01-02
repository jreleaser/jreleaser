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
 * @since 1.3.0
 */
public final class GithubMavenDeployer extends AbstractMavenDeployer<GithubMavenDeployer, org.jreleaser.model.api.deploy.maven.GithubMavenDeployer> {
    private static final long serialVersionUID = 5073629187876822221L;

    private String repository;

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.maven.GithubMavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.GithubMavenDeployer() {
        private static final long serialVersionUID = -7375325967023331116L;

        private Set<? extends org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride> artifactOverrides;

        @Override
        public String getGroup() {
            return org.jreleaser.model.api.deploy.maven.MavenDeployer.GROUP;
        }

        @Override
        public String getRepository() {
            return GithubMavenDeployer.this.getRepository();
        }

        @Override
        public String getUrl() {
            return GithubMavenDeployer.this.getUrl();
        }

        @Override
        public String getUsername() {
            return GithubMavenDeployer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return GithubMavenDeployer.this.getPassword();
        }

        @Override
        public Http.Authorization getAuthorization() {
            return GithubMavenDeployer.this.getAuthorization();
        }

        @Override
        public boolean isSign() {
            return GithubMavenDeployer.this.isSign();
        }

        @Override
        public boolean isChecksums() {
            return GithubMavenDeployer.this.isChecksums();
        }

        @Override
        public boolean isSourceJar() {
            return GithubMavenDeployer.this.isSourceJar();
        }

        @Override
        public boolean isJavadocJar() {
            return GithubMavenDeployer.this.isJavadocJar();
        }

        @Override
        public boolean isVerifyPom() {
            return GithubMavenDeployer.this.isVerifyPom();
        }

        @Override
        public boolean isApplyMavenCentralRules() {
            return GithubMavenDeployer.this.isApplyMavenCentralRules();
        }

        @Override
        public List<String> getStagingRepositories() {
            return unmodifiableList(GithubMavenDeployer.this.getStagingRepositories());
        }

        @Override
        public Set<? extends org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride> getArtifactOverrides() {
            if (null == artifactOverrides) {
                artifactOverrides = GithubMavenDeployer.this.getArtifactOverrides().stream()
                    .map(MavenDeployer.ArtifactOverride::asImmutable)
                    .collect(toSet());
            }
            return artifactOverrides;
        }

        @Override
        public String getType() {
            return GithubMavenDeployer.this.getType();
        }

        @Override
        public String getName() {
            return GithubMavenDeployer.this.getName();
        }

        @Override
        public Active getActive() {
            return GithubMavenDeployer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return GithubMavenDeployer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GithubMavenDeployer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return GithubMavenDeployer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(GithubMavenDeployer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return GithubMavenDeployer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return GithubMavenDeployer.this.getReadTimeout();
        }
    };

    public GithubMavenDeployer() {
        super(org.jreleaser.model.api.deploy.maven.GithubMavenDeployer.TYPE);
    }

    @Override
    public org.jreleaser.model.api.deploy.maven.GithubMavenDeployer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(GithubMavenDeployer source) {
        super.merge(source);
        this.repository = merge(this.repository, source.repository);
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("repository", repository);
    }

    @Override
    public String getResolvedUrl(TemplateContext props) {
        props.set("username", getUsername());
        props.set("owner", getUsername());
        props.set("repository", repository);
        props.setAll(getExtraProperties());
        return normalizeUrl(resolveTemplate(getUrl(), props));
    }

    @Override
    public Http.Authorization resolveAuthorization() {
        setAuthorization(Http.Authorization.BEARER);
        return getAuthorization();
    }
}
