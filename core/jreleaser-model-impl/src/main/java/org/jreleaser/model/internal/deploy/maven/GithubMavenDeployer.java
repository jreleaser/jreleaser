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
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class GithubMavenDeployer extends AbstractMavenDeployer<GithubMavenDeployer, org.jreleaser.model.api.deploy.maven.GithubMavenDeployer> {
    private static final long serialVersionUID = 7453425593383154500L;

    private String repository;

    private final org.jreleaser.model.api.deploy.maven.GithubMavenDeployer immutable = new org.jreleaser.model.api.deploy.maven.GithubMavenDeployer() {
        private static final long serialVersionUID = 900691521486342222L;

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
        public boolean isSign() {
            return GithubMavenDeployer.this.isSign();
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
            return GithubMavenDeployer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GithubMavenDeployer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return GithubMavenDeployer.this.getPrefix();
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
    public String getResolvedUrl(Map<String, Object> props) {
        props.put("username", username);
        props.put("owner", username);
        props.put("repository", repository);
        props.putAll(getExtraProperties());
        return resolveTemplate(url, props);
    }

    @Override
    public Http.Authorization resolveAuthorization() {
        authorization = Http.Authorization.BEARER;
        return authorization;
    }
}
