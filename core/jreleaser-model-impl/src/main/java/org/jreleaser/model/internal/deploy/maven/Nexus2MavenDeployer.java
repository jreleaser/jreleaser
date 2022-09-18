/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import org.jreleaser.model.Active;
import org.jreleaser.model.Http;

import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class Nexus2MavenDeployer extends AbstractMavenDeployer<Nexus2MavenDeployer> {
    private Boolean closeRepository;
    private Boolean releaseRepository;

    private final org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer() {
        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public Http.Authorization getAuthorization() {
            return authorization;
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
        public boolean isVerifyPom() {
            return Nexus2MavenDeployer.this.isVerifyPom();
        }

        @Override
        public boolean isApplyMavenCentralRules() {
            return Nexus2MavenDeployer.this.isApplyMavenCentralRules();
        }

        @Override
        public List<String> getStagingRepositories() {
            return unmodifiableList(stagingRepositories);
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Active getActive() {
            return active;
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
            return Nexus2MavenDeployer.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }

        @Override
        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        @Override
        public Integer getReadTimeout() {
            return readTimeout;
        }
    };

    public Nexus2MavenDeployer() {
        super(org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.TYPE);
    }

    public org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer asImmutable() {
        return immutable;
    }

    public boolean isCloseRepository() {
        return closeRepository != null && closeRepository;
    }

    public void setCloseRepository(Boolean closeRepository) {
        this.closeRepository = closeRepository;
    }

    public boolean isCloseRepositorySet() {
        return closeRepository != null;
    }

    public boolean isReleaseRepository() {
        return releaseRepository != null && releaseRepository;
    }

    public void setReleaseRepository(Boolean releaseRepository) {
        this.releaseRepository = releaseRepository;
    }

    public boolean isReleaseRepositorySet() {
        return releaseRepository != null;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("closeRepository", isCloseRepository());
        props.put("releaseRepository", isReleaseRepository());
    }
}
