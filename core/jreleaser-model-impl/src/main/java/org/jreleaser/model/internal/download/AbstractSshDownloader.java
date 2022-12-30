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
package org.jreleaser.model.internal.download;

import java.util.Map;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class AbstractSshDownloader<A extends org.jreleaser.model.api.download.Downloader, S extends AbstractSshDownloader<A, S>> extends AbstractDownloader<A, S> implements SshDownloader<A> {
    private static final long serialVersionUID = -6171515598110485664L;

    protected String username;
    protected String password;
    protected String host;
    protected Integer port;
    protected String knownHostsFile;
    protected String publicKey;
    protected String privateKey;
    protected String passphrase;
    protected String fingerprint;

    public AbstractSshDownloader(String type) {
        super(type);
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.username = merge(this.username, source.username);
        this.password = merge(this.password, source.password);
        this.host = merge(this.host, source.host);
        this.port = merge(this.port, source.port);
        this.knownHostsFile = merge(this.knownHostsFile, source.knownHostsFile);
        this.publicKey = merge(this.publicKey, source.publicKey);
        this.privateKey = merge(this.privateKey, source.privateKey);
        this.passphrase = merge(this.passphrase, source.passphrase);
        this.fingerprint = merge(this.fingerprint, source.fingerprint);
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
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
