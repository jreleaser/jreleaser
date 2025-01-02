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
public final class Nexus2MavenDeployer extends AbstractMavenDeployer<Nexus2MavenDeployer, org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer> {
    private static final long serialVersionUID = 9077911047137402294L;

    private String snapshotUrl;
    private String verifyUrl;
    private Boolean closeRepository;
    private Boolean releaseRepository;
    private int transitionDelay;
    private int transitionMaxRetries;

    private String stagingProfileId;
    private String stagingRepositoryId;
    private org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage startStage;
    private org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage endStage;

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer() {
        private static final long serialVersionUID = -2516726037520331601L;

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
        public String getVerifyUrl() {
            return verifyUrl;
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
        public String getStagingProfileId() {
            return Nexus2MavenDeployer.this.getStagingProfileId();
        }

        @Override
        public String getStagingRepositoryId() {
            return Nexus2MavenDeployer.this.getStagingRepositoryId();
        }

        @Override
        public Stage getStartStage() {
            return Nexus2MavenDeployer.this.getStartStage();
        }

        @Override
        public Stage getEndStage() {
            return Nexus2MavenDeployer.this.getEndStage();
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
        super(org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.TYPE, true);
    }

    @Override
    public org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Nexus2MavenDeployer source) {
        super.merge(source);
        this.verifyUrl = merge(this.verifyUrl, source.verifyUrl);
        this.snapshotUrl = merge(this.snapshotUrl, source.snapshotUrl);
        this.closeRepository = merge(this.closeRepository, source.closeRepository);
        this.releaseRepository = merge(this.releaseRepository, source.releaseRepository);
        this.transitionDelay = merge(this.transitionDelay, source.transitionDelay);
        this.transitionMaxRetries = merge(this.transitionMaxRetries, source.transitionMaxRetries);
    }

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public void setVerifyUrl(String verifyUrl) {
        this.verifyUrl = verifyUrl;
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

    public String getStagingProfileId() {
        return stagingProfileId;
    }

    public void setStagingProfileId(String stagingProfileId) {
        this.stagingProfileId = stagingProfileId;
    }

    public String getStagingRepositoryId() {
        return stagingRepositoryId;
    }

    public void setStagingRepositoryId(String stagingRepositoryId) {
        this.stagingRepositoryId = stagingRepositoryId;
    }

    public org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage getStartStage() {
        return startStage;
    }

    public void setStartStage(org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage startStage) {
        this.startStage = startStage;
    }

    public org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage getEndStage() {
        return endStage;
    }

    public void setEndStage(org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage endStage) {
        this.endStage = endStage;
    }

    public String getResolvedSnapshotUrl(TemplateContext props) {
        props.set("username", getUsername());
        props.set("owner", getUsername());
        props.setAll(getExtraProperties());
        return resolveTemplate(snapshotUrl, props);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("verifyUrl", verifyUrl);
        props.put("snapshotUrl", snapshotUrl);
        props.put("closeRepository", isCloseRepository());
        props.put("releaseRepository", isReleaseRepository());
        props.put("transitionDelay", transitionDelay);
        props.put("transitionMaxRetries", transitionMaxRetries);
        props.put("stagingProfileId", stagingProfileId);
        props.put("stagingRepositoryId", stagingRepositoryId);
        props.put("startStage", startStage);
        props.put("endStage", endStage);
    }
}
