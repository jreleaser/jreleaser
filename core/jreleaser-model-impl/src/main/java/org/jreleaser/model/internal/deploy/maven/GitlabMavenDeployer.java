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
public final class GitlabMavenDeployer extends AbstractMavenDeployer<GitlabMavenDeployer, org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer> {
    private static final long serialVersionUID = 924140634644168936L;

    private String projectIdentifier;

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer() {
        private static final long serialVersionUID = 1856197755643058769L;

        private Set<? extends org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride> artifactOverrides;

        @Override
        public String getGroup() {
            return org.jreleaser.model.api.deploy.maven.MavenDeployer.GROUP;
        }

        @Override
        public String getProjectIdentifier() {
            return GitlabMavenDeployer.this.getProjectIdentifier();
        }

        @Override
        public String getUrl() {
            return GitlabMavenDeployer.this.getUrl();
        }

        @Override
        public String getUsername() {
            return GitlabMavenDeployer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return GitlabMavenDeployer.this.getPassword();
        }

        @Override
        public Http.Authorization getAuthorization() {
            return GitlabMavenDeployer.this.getAuthorization();
        }

        @Override
        public boolean isSign() {
            return GitlabMavenDeployer.this.isSign();
        }

        @Override
        public boolean isChecksums() {
            return GitlabMavenDeployer.this.isChecksums();
        }

        @Override
        public boolean isSourceJar() {
            return GitlabMavenDeployer.this.isSourceJar();
        }

        @Override
        public boolean isJavadocJar() {
            return GitlabMavenDeployer.this.isJavadocJar();
        }

        @Override
        public boolean isVerifyPom() {
            return GitlabMavenDeployer.this.isVerifyPom();
        }

        @Override
        public boolean isApplyMavenCentralRules() {
            return GitlabMavenDeployer.this.isApplyMavenCentralRules();
        }

        @Override
        public List<String> getStagingRepositories() {
            return unmodifiableList(GitlabMavenDeployer.this.getStagingRepositories());
        }

        @Override
        public Set<? extends org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride> getArtifactOverrides() {
            if (null == artifactOverrides) {
                artifactOverrides = GitlabMavenDeployer.this.getArtifactOverrides().stream()
                    .map(MavenDeployer.ArtifactOverride::asImmutable)
                    .collect(toSet());
            }
            return artifactOverrides;
        }

        @Override
        public String getType() {
            return GitlabMavenDeployer.this.getType();
        }

        @Override
        public String getName() {
            return GitlabMavenDeployer.this.getName();
        }

        @Override
        public Active getActive() {
            return GitlabMavenDeployer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return GitlabMavenDeployer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GitlabMavenDeployer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return GitlabMavenDeployer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(GitlabMavenDeployer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return GitlabMavenDeployer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return GitlabMavenDeployer.this.getReadTimeout();
        }
    };

    public GitlabMavenDeployer() {
        super(org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer.TYPE);
    }

    @Override
    public org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(GitlabMavenDeployer source) {
        super.merge(source);
        this.projectIdentifier = merge(this.projectIdentifier, source.projectIdentifier);
    }

    public String getProjectIdentifier() {
        return projectIdentifier;
    }

    public void setProjectIdentifier(String projectIdentifier) {
        this.projectIdentifier = projectIdentifier;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("projectIdentifier", projectIdentifier);
    }

    @Override
    public String getResolvedUrl(TemplateContext props) {
        props.set("username", getUsername());
        props.set("owner", getUsername());
        props.set("projectIdentifier", projectIdentifier);
        props.setAll(getExtraProperties());
        return normalizeUrl(resolveTemplate(getUrl(), props));
    }

    @Override
    public Http.Authorization resolveAuthorization() {
        setAuthorization(Http.Authorization.BEARER);
        return getAuthorization();
    }
}
