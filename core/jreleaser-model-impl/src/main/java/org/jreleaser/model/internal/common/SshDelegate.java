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
package org.jreleaser.model.internal.common;

import java.io.Serializable;
import java.util.Map;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class SshDelegate extends AbstractModelObject<SshDelegate> implements Ssh, Serializable {
    private static final long serialVersionUID = -8963633421280444565L;

    private String username;
    private String password;
    private String host;
    private Integer port;
    private String knownHostsFile;
    private String publicKey;
    private String privateKey;
    private String passphrase;
    private String fingerprint;

   @Override
    public void merge(SshDelegate source) {
        this.username = merge(this.username, source.getUsername());
        this.password = merge(this.password, source.getPassword());
        this.host = merge(this.host, source.getHost());
        this.port = merge(this.port, source.getPort());
        this.knownHostsFile = merge(this.knownHostsFile, source.getKnownHostsFile());
        this.publicKey = merge(this.publicKey, source.getPublicKey());
        this.privateKey = merge(this.privateKey, source.getPrivateKey());
        this.passphrase = merge(this.passphrase, source.getPassphrase());
        this.fingerprint = merge(this.fingerprint, source.getFingerprint());
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Integer getPort() {
        return null != port ? port : 22;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
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

    public void asMap(Map<String, Object> props) {
        props.put("host", isNotBlank(host) ? HIDE : UNSET);
        props.put("port", getPort());
        props.put("username", isNotBlank(username) ? HIDE : UNSET);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
        props.put("knownHostsFile", knownHostsFile);
        props.put("publicKey", isNotBlank(publicKey) ? HIDE : UNSET);
        props.put("privateKey", isNotBlank(privateKey) ? HIDE : UNSET);
        props.put("passphrase", isNotBlank(passphrase) ? HIDE : UNSET);
        props.put("fingerprint", isNotBlank(fingerprint) ? HIDE : UNSET);
    }
}
