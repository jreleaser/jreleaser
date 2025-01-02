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
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.TimeoutAware;
import org.jreleaser.model.internal.deploy.Deployer;
import org.jreleaser.mustache.TemplateContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public interface MavenDeployer<A extends org.jreleaser.model.api.deploy.maven.MavenDeployer> extends Deployer<A>, TimeoutAware {
    String getUrl();

    void setUrl(String url);

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    Http.Authorization getAuthorization();

    void setAuthorization(Http.Authorization authorization);

    void setAuthorization(String authorization);

    boolean isSign();

    void setSign(Boolean sign);

    boolean isSignSet();

    boolean isChecksums();

    void setChecksums(Boolean checksums);

    boolean isChecksumsSet();

    boolean isSourceJar();

    void setSourceJar(Boolean sourceJar);

    boolean isSourceJarSet();

    boolean isJavadocJar();

    void setJavadocJar(Boolean javadocJar);

    boolean isJavadocJarSet();

    boolean isVerifyPom();

    void setVerifyPom(Boolean verifyPom);

    boolean isVerifyPomSet();

    boolean isApplyMavenCentralRules();

    void setApplyMavenCentralRules(Boolean applyMavenCentralRules);

    boolean isApplyMavenCentralRulesSet();

    List<String> getStagingRepositories();

    void setStagingRepositories(List<String> stagingRepositories);

    Set<ArtifactOverride> getArtifactOverrides();

    void setArtifactOverrides(Set<ArtifactOverride> artifactOverrides);

    void addArtifactOverride(ArtifactOverride artifactOverride);

    Http.Authorization resolveAuthorization();

    String getResolvedUrl(TemplateContext props);

    List<String> keysFor(String property);

    void setSnapshotSupported(Boolean snapshotSupported);

    public final class ArtifactOverride extends AbstractModelObject<ArtifactOverride> implements Domain {
        private static final long serialVersionUID = 8057060716591206147L;

        private String groupId;
        private String artifactId;
        private Boolean jar;
        private Boolean sourceJar;
        private Boolean javadocJar;
        private Boolean verifyPom;

        @JsonIgnore
        private final org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride immutable = new org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride() {
            private static final long serialVersionUID = -7937881029176143227L;
            @Override
            public String getGroupId() {
                return groupId;
            }

            @Override
            public String getArtifactId() {
                return artifactId;
            }

            @Override
            public boolean isJar() {
                return jar;
            }

            @Override
            public boolean isSourceJar() {
                return sourceJar;
            }

            @Override
            public boolean isJavadocJar() {
                return javadocJar;
            }

            @Override
            public boolean isVerifyPom() {
                return verifyPom;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(ArtifactOverride.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.deploy.maven.MavenDeployer.ArtifactOverride asImmutable() {
            return immutable;
        }

        @Override
        public void merge(ArtifactOverride source) {
            this.groupId = this.merge(this.groupId, source.groupId);
            this.artifactId = this.merge(this.artifactId, source.artifactId);
            this.jar = this.merge(this.jar, source.jar);
            this.sourceJar = this.merge(this.sourceJar, source.sourceJar);
            this.javadocJar = this.merge(this.javadocJar, source.javadocJar);
            this.verifyPom = this.merge(this.verifyPom, source.verifyPom);
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public boolean isJar() {
            return null != jar && jar;
        }

        public void setJar(Boolean jar) {
            this.jar = jar;
        }

        public boolean isJarSet() {
            return null != jar;
        }

        public boolean isSourceJar() {
            return null != sourceJar && sourceJar;
        }

        public void setSourceJar(Boolean sourceJar) {
            this.sourceJar = sourceJar;
        }

        public boolean isSourceJarSet() {
            return null != sourceJar;
        }

        public boolean isJavadocJar() {
            return null != javadocJar && javadocJar;
        }

        public void setJavadocJar(Boolean javadocJar) {
            this.javadocJar = javadocJar;
        }

        public boolean isJavadocJarSet() {
            return null != javadocJar;
        }

        public boolean isVerifyPom() {
            return null != verifyPom && verifyPom;
        }

        public void setVerifyPom(Boolean verifyPom) {
            this.verifyPom = verifyPom;
        }

        public boolean isVerifyPomSet() {
            return null != verifyPom;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("groupId", groupId);
            props.put("artifactId", artifactId);
            props.put("jar", isJar());
            props.put("sourceJar", isSourceJar());
            props.put("javadocJar", isJavadocJar());
            props.put("verifyPom", isVerifyPom());
            return props;
        }
    }
}
