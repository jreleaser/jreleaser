/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
package org.jreleaser.model.internal.servers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.Gitlab;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
public final class GitlabServer extends AbstractServer<org.jreleaser.model.api.servers.GitlabServer, GitlabServer>
    implements Gitlab {
    private static final long serialVersionUID = -5607834832063597589L;

    private String projectIdentifier;

    @JsonIgnore
    private final org.jreleaser.model.api.servers.GitlabServer immutable = new org.jreleaser.model.api.servers.GitlabServer() {
        private static final long serialVersionUID = 6591793671001232342L;

        @Override
        public String getName() {
            return GitlabServer.this.getName();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GitlabServer.this.asMap(full));
        }

        @Override
        public String getHost() {
            return GitlabServer.this.getHost();
        }

        @Override
        public Integer getPort() {
            return GitlabServer.this.getPort();
        }

        @Override
        public String getUsername() {
            return GitlabServer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return GitlabServer.this.getPassword();
        }

        @Override
        public Integer getConnectTimeout() {
            return GitlabServer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return GitlabServer.this.getReadTimeout();
        }

        @Override
        public String getProjectIdentifier() {
            return GitlabServer.this.getProjectIdentifier();
        }
    };

    @Override
    public org.jreleaser.model.api.servers.GitlabServer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(GitlabServer source) {
        super.merge(source);
        this.projectIdentifier = merge(this.projectIdentifier, source.projectIdentifier);
    }

    @Override
    public String getProjectIdentifier() {
        return projectIdentifier;
    }

    @Override
    public void setProjectIdentifier(String projectIdentifier) {
        this.projectIdentifier = projectIdentifier;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {

    }

    public void mergeWith(GitlabServer other) {
        super.mergeWith(other);
        setProjectIdentifier(merge(other.getProjectIdentifier(), getProjectIdentifier()));
    }
}
