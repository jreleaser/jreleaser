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
public final class FtpServer extends AbstractServer<org.jreleaser.model.api.servers.FtpServer, FtpServer>
    implements org.jreleaser.model.internal.common.Ftp {
    private static final long serialVersionUID = 2456265712555123405L;

    @JsonIgnore
    private final org.jreleaser.model.api.servers.FtpServer immutable = new org.jreleaser.model.api.servers.FtpServer() {
        private static final long serialVersionUID = 4101700126246419455L;

        @Override
        public String getName() {
            return FtpServer.this.getName();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(FtpServer.this.asMap(full));
        }

        @Override
        public String getHost() {
            return FtpServer.this.getHost();
        }

        @Override
        public Integer getPort() {
            return FtpServer.this.getPort();
        }

        @Override
        public String getUsername() {
            return FtpServer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return FtpServer.this.getPassword();
        }

        @Override
        public Integer getConnectTimeout() {
            return FtpServer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return FtpServer.this.getReadTimeout();
        }
    };

    @Override
    public org.jreleaser.model.api.servers.FtpServer asImmutable() {
        return immutable;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {

    }
}
