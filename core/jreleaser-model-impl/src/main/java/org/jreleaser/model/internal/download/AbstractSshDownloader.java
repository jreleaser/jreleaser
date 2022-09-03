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

import org.jreleaser.util.Env;

import java.util.Map;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class AbstractSshDownloader<S extends AbstractSshDownloader<S>> extends AbstractDownloader<S> implements SshDownloader {
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

    protected abstract String getEnvPrefix();

    public String getResolvedUsername() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_USERNAME",
                "SSH_" + Env.toVar(name) + "_USERNAME",
                getEnvPrefix() + "_USERNAME",
                "SSH_USERNAME"),
            username);
    }

    public String getResolvedPassword() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_PASSWORD",
                "SSH_" + Env.toVar(name) + "_PASSWORD",
                getEnvPrefix() + "_PASSWORD",
                "SSH_PASSWORD"),
            password);
    }

    public String getResolvedHost() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_HOST",
                "SSH_" + Env.toVar(name) + "_HOST",
                getEnvPrefix() + "_HOST",
                "SSH_HOST"),
            host);
    }

    public Integer getResolvedPort() {
        String value = Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_PORT",
                "SSH_" + Env.toVar(name) + "_PORT",
                getEnvPrefix() + "_PORT",
                "SSH_PORT"),
            null == port ? "" : String.valueOf(port));
        return isBlank(value) ? 22 : Integer.parseInt(value);
    }

    public String getResolvedPublicKey() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_PUBLIC_KEY",
                "SSH_" + Env.toVar(name) + "_PUBLIC_KEY",
                getEnvPrefix() + "_PUBLIC_KEY",
                "SSH_PUBLIC_KEY"),
            publicKey);
    }

    public String getResolvedPrivateKey() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_PRIVATE_KEY",
                "SSH_" + Env.toVar(name) + "_PRIVATE_KEY",
                getEnvPrefix() + "_PRIVATE_KEY",
                "SSH_PRIVATE_KEY"),
            privateKey);
    }

    public String getResolvedPassphrase() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_PASSPHRASE",
                "SSH_" + Env.toVar(name) + "_PASSPHRASE",
                getEnvPrefix() + "_PASSPHRASE",
                "SSH_PASSPHRASE"),
            passphrase);
    }

    public String getResolvedFingerprint() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_FINGERPRINT",
                "SSH_" + Env.toVar(name) + "_FINGERPRINT",
                getEnvPrefix() + "_FINGERPRINT",
                "SSH_FINGERPRINT"),
            fingerprint);
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String getKnownHostsFile() {
        return knownHostsFile;
    }

    public void setKnownHostsFile(String knownHostsFile) {
        this.knownHostsFile = knownHostsFile;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("host", isNotBlank(getResolvedHost()) ? HIDE : UNSET);
        props.put("port", getResolvedPort());
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
        props.put("knownHostsFile", knownHostsFile);
        props.put("publicKey", isNotBlank(getResolvedPublicKey()) ? HIDE : UNSET);
        props.put("privateKey", isNotBlank(getResolvedPrivateKey()) ? HIDE : UNSET);
        props.put("passphrase", isNotBlank(getResolvedPassphrase()) ? HIDE : UNSET);
        props.put("fingerprint", isNotBlank(getResolvedFingerprint()) ? HIDE : UNSET);
    }
}
