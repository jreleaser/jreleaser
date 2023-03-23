/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
public final class Nexus2MavenDeployer extends AbstractMavenDeployer<Nexus2MavenDeployer, org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer> {
    private static final long serialVersionUID = 5297196267872863920L;

    private String snapshotUrl;
    private Boolean closeRepository;
    private Boolean releaseRepository;
    private int transitionDelay;
    private int transitionMaxRetries;

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer() {
        private static final long serialVersionUID = -3313316023556026481L;

        private Set<? extends org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride> artifactOverrides;

        @Override
        public String getGroup() {
            return org.jreleaser.model.api.deploy.maven.MavenDeployer.GROUP;
        }

        @Override
        public String getSnapshotUrl() {
            return snapshotUrl;
        }

        @Override
        public String getUrl() {
            return Nexus2MavenDeployer.this.getUrl();
        }

        @Override
        public String getUsername() {
            return Nexus2MavenDeployer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return Nexus2MavenDeployer.this.getPassword();
        }

        @Override
        public Http.Authorization getAuthorization() {
            return Nexus2MavenDeployer.this.getAuthorization();
        }

        @Override
        public boolean isCloseRepository() {
            return Nexus2MavenDeployer.this.isCloseRepository();
        }

        @Override
        public boolean isReleaseRepository() {
            return Nexus2MavenDeployer.this.isReleaseRepository();
        }

        @Override
        public boolean isSign() {
            return Nexus2MavenDeployer.this.isSign();
        }

        @Override
        public boolean isChecksums() {
            return Nexus2MavenDeployer.this.isChecksums();
        }

        @Override
        public boolean isSourceJar() {
            return Nexus2MavenDeployer.this.isSourceJar();
        }

        @Override
        public boolean isJavadocJar() {
            return Nexus2MavenDeployer.this.isJavadocJar();
        }

        @Override
        public boolean isVerifyPom() {
            return Nexus2MavenDeployer.this.isVerifyPom();
        }

        @Override
        public boolean isApplyMavenCentralRules() {
            return Nexus2MavenDeployer.this.isApplyMavenCentralRules();
        }

        @Override
        public List<String> getStagingRepositories() {
            return unmodifiableList(Nexus2MavenDeployer.this.getStagingRepositories());
        }

        @Override
        public Set<? extends org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride> getArtifactOverrides() {
            if (null == artifactOverrides) {
                artifactOverrides = Nexus2MavenDeployer.this.getArtifactOverrides().stream()
                    .map(MavenDeployer.ArtifactOverride::asImmutable)
                    .collect(toSet());
            }
            return artifactOverrides;
        }

        @Override
        public String getType() {
            return Nexus2MavenDeployer.this.getType();
        }

        @Override
        public String getName() {
            return Nexus2MavenDeployer.this.getName();
        }

        @Override
        public Active getActive() {
            return Nexus2MavenDeployer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Nexus2MavenDeployer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Nexus2MavenDeployer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return Nexus2MavenDeployer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(Nexus2MavenDeployer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return Nexus2MavenDeployer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return Nexus2MavenDeployer.this.getReadTimeout();
        }

        @Override
        public Integer getTransitionDelay() {
            return transitionDelay;
        }

        @Override
        public Integer getTransitionMaxRetries() {
            return transitionMaxRetries;
        }
    };

    public Nexus2MavenDeployer() {
        super(org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.TYPE);
    }

    @Override
    public org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Nexus2MavenDeployer source) {
        super.merge(source);
        this.snapshotUrl = merge(this.snapshotUrl, source.snapshotUrl);
        this.closeRepository = merge(this.closeRepository, source.closeRepository);
        this.releaseRepository = merge(this.releaseRepository, source.releaseRepository);
        this.transitionDelay = merge(this.transitionDelay, source.transitionDelay);
        this.transitionMaxRetries = merge(this.transitionMaxRetries, source.transitionMaxRetries);
    }

    public String getSnapshotUrl() {
        return snapshotUrl;
    }

    public void setSnapshotUrl(String snapshotUrl) {
        this.snapshotUrl = snapshotUrl;
    }

    public boolean isCloseRepository() {
        return null != closeRepository && closeRepository;
    }

    public void setCloseRepository(Boolean closeRepository) {
        this.closeRepository = closeRepository;
    }

    public boolean isCloseRepositorySet() {
        return null != closeRepository;
    }

    public boolean isReleaseRepository() {
        return null != releaseRepository && releaseRepository;
    }

    public void setReleaseRepository(Boolean releaseRepository) {
        this.releaseRepository = releaseRepository;
    }

    public boolean isReleaseRepositorySet() {
        return null != releaseRepository;
    }

    public Integer getTransitionDelay() {
        return transitionDelay;
    }

    public void setTransitionDelay(Integer transitionDelay) {
        this.transitionDelay = transitionDelay;
    }

    public Integer getTransitionMaxRetries() {
        return transitionMaxRetries;
    }

    public void setTransitionMaxRetries(Integer transitionMaxRetries) {
        this.transitionMaxRetries = transitionMaxRetries;
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    public String getResolvedSnapshotUrl(TemplateContext props) {
        props.set("username", getUsername());
        props.set("owner", getUsername());
        props.setAll(getExtraProperties());
        return resolveTemplate(snapshotUrl, props);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("snapshotUrl", snapshotUrl);
        props.put("closeRepository", isCloseRepository());
        props.put("releaseRepository", isReleaseRepository());
        props.put("transitionDelay", transitionDelay);
        props.put("transitionMaxRetries", transitionMaxRetries);
    }
}
