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
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
public final class SshServer extends AbstractServer<org.jreleaser.model.api.servers.SshServer, SshServer>
    implements org.jreleaser.model.internal.common.Ssh {
    private static final long serialVersionUID = 1765069184183351312L;

    private String knownHostsFile;
    private String publicKey;
    private String privateKey;
    private String passphrase;
    private String fingerprint;

    @JsonIgnore
    private final org.jreleaser.model.api.servers.SshServer immutable = new org.jreleaser.model.api.servers.SshServer() {
        private static final long serialVersionUID = -9105629673856893410L;

        @Override
        public String getName() {
            return SshServer.this.getName();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SshServer.this.asMap(full));
        }

        @Override
        public String getHost() {
            return SshServer.this.getHost();
        }

        @Override
        public Integer getPort() {
            return SshServer.this.getPort();
        }

        @Override
        public String getUsername() {
            return SshServer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return SshServer.this.getPassword();
        }

        @Override
        public Integer getConnectTimeout() {
            return SshServer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return SshServer.this.getReadTimeout();
        }

        @Override
        public String getKnownHostsFile() {
            return SshServer.this.getKnownHostsFile();
        }

        @Override
        public String getPublicKey() {
            return SshServer.this.getPublicKey();
        }

        @Override
        public String getPrivateKey() {
            return SshServer.this.getPrivateKey();
        }

        @Override
        public String getPassphrase() {
            return SshServer.this.getPassphrase();
        }

        @Override
        public String getFingerprint() {
            return SshServer.this.getFingerprint();
        }
    };

    @Override
    public org.jreleaser.model.api.servers.SshServer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SshServer source) {
        super.merge(source);
        this.knownHostsFile = merge(this.knownHostsFile, source.getKnownHostsFile());
        this.publicKey = merge(this.publicKey, source.getPublicKey());
        this.privateKey = merge(this.privateKey, source.getPrivateKey());
        this.passphrase = merge(this.passphrase, source.getPassphrase());
        this.fingerprint = merge(this.fingerprint, source.getFingerprint());
    }

    @Override
    public String getKnownHostsFile() {
        return knownHostsFile;
    }

    @Override
    public void setKnownHostsFile(String knownHostsFile) {
        this.knownHostsFile = knownHostsFile;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String getPrivateKey() {
        return privateKey;
    }

    @Override
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String getPassphrase() {
        return passphrase;
    }

    @Override
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    public String getFingerprint() {
        return fingerprint;
    }

    @Override
    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("knownHostsFile", knownHostsFile);
        props.put("publicKey", isNotBlank(publicKey) ? HIDE : UNSET);
        props.put("privateKey", isNotBlank(privateKey) ? HIDE : UNSET);
        props.put("passphrase", isNotBlank(passphrase) ? HIDE : UNSET);
        props.put("fingerprint", isNotBlank(fingerprint) ? HIDE : UNSET);
    }

    public void mergeWith(SshServer other) {
        super.mergeWith(other);
        setKnownHostsFile(merge(other.getKnownHostsFile(), getKnownHostsFile()));
        setPublicKey(merge(other.getPublicKey(), getPublicKey()));
        setPrivateKey(merge(other.getPrivateKey(), getPrivateKey()));
        setPassphrase(merge(other.getPassphrase(), getPassphrase()));
        setFingerprint(merge(other.getFingerprint(), getFingerprint()));
    }
}
