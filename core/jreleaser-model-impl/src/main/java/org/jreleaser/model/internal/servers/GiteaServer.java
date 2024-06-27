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

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
public final class GiteaServer extends AbstractServer<org.jreleaser.model.api.servers.GiteaServer, GiteaServer> {
    private static final long serialVersionUID = -171963552224835459L;

    @JsonIgnore
    private final org.jreleaser.model.api.servers.GiteaServer immutable = new org.jreleaser.model.api.servers.GiteaServer() {
        private static final long serialVersionUID = 2224328758365761456L;

        @Override
        public String getName() {
            return GiteaServer.this.getName();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GiteaServer.this.asMap(full));
        }

        @Override
        public String getHost() {
            return GiteaServer.this.getHost();
        }

        @Override
        public Integer getPort() {
            return GiteaServer.this.getPort();
        }

        @Override
        public String getUsername() {
            return GiteaServer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return GiteaServer.this.getPassword();
        }

        @Override
        public Integer getConnectTimeout() {
            return GiteaServer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return GiteaServer.this.getReadTimeout();
        }
    };

    @Override
    public org.jreleaser.model.api.servers.GiteaServer asImmutable() {
        return immutable;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {

    }
}
