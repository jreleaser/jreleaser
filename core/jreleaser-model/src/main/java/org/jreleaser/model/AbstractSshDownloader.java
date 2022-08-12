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
package org.jreleaser.model;

import org.jreleaser.util.Env;

import java.util.Map;

import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
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
    public void merge(S downloader) {
        freezeCheck();
        super.merge(downloader);
        this.username = merge(this.username, downloader.username);
        this.password = merge(this.password, downloader.password);
        this.host = merge(this.host, downloader.host);
        this.port = merge(this.port, downloader.port);
        this.knownHostsFile = merge(this.knownHostsFile, downloader.knownHostsFile);
        this.publicKey = merge(this.publicKey, downloader.publicKey);
        this.privateKey = merge(this.privateKey, downloader.privateKey);
        this.passphrase = merge(this.passphrase, downloader.passphrase);
        this.fingerprint = merge(this.fingerprint, downloader.fingerprint);
    }

    protected abstract String getEnvPrefix();

    @Override
    public String getResolvedUsername() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_USERNAME",
                "SSH_" + Env.toVar(name) + "_USERNAME",
                getEnvPrefix() + "_USERNAME",
                "SSH_USERNAME"),
            username);
    }

    @Override
    public String getResolvedPassword() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_PASSWORD",
                "SSH_" + Env.toVar(name) + "_PASSWORD",
                getEnvPrefix() + "_PASSWORD",
                "SSH_PASSWORD"),
            password);
    }

    @Override
    public String getResolvedHost() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_HOST",
                "SSH_" + Env.toVar(name) + "_HOST",
                getEnvPrefix() + "_HOST",
                "SSH_HOST"),
            host);
    }

    @Override
    public Integer getResolvedPort() {
        String value = Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_PORT",
                "SSH_" + Env.toVar(name) + "_PORT",
                getEnvPrefix() + "_PORT",
                "SSH_PORT"),
            null == port ? "" : String.valueOf(port));
        return isBlank(value) ? 22 : Integer.parseInt(value);
    }

    @Override
    public String getResolvedPublicKey() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_PUBLIC_KEY",
                "SSH_" + Env.toVar(name) + "_PUBLIC_KEY",
                getEnvPrefix() + "_PUBLIC_KEY",
                "SSH_PUBLIC_KEY"),
            publicKey);
    }

    @Override
    public String getResolvedPrivateKey() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_PRIVATE_KEY",
                "SSH_" + Env.toVar(name) + "_PRIVATE_KEY",
                getEnvPrefix() + "_PRIVATE_KEY",
                "SSH_PRIVATE_KEY"),
            privateKey);
    }

    @Override
    public String getResolvedPassphrase() {
        return Env.env(listOf(
                getEnvPrefix() + "_" + Env.toVar(name) + "_PASSPHRASE",
                "SSH_" + Env.toVar(name) + "_PASSPHRASE",
                getEnvPrefix() + "_PASSPHRASE",
                "SSH_PASSPHRASE"),
            passphrase);
    }

    @Override
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

    @Override
    public void setUsername(String username) {
        freezeCheck();
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        freezeCheck();
        this.password = password;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        freezeCheck();
        this.host = host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public void setPort(Integer port) {
        freezeCheck();
        this.port = port;
    }

    @Override
    public String getKnownHostsFile() {
        return knownHostsFile;
    }

    @Override
    public void setKnownHostsFile(String knownHostsFile) {
        freezeCheck();
        this.knownHostsFile = knownHostsFile;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public void setPublicKey(String publicKey) {
        freezeCheck();
        this.publicKey = publicKey;
    }

    @Override
    public String getPrivateKey() {
        return privateKey;
    }

    @Override
    public void setPrivateKey(String privateKey) {
        freezeCheck();
        this.privateKey = privateKey;
    }

    @Override
    public String getPassphrase() {
        return passphrase;
    }

    @Override
    public void setPassphrase(String passphrase) {
        freezeCheck();
        this.passphrase = passphrase;
    }

    @Override
    public String getFingerprint() {
        return fingerprint;
    }

    @Override
    public void setFingerprint(String fingerprint) {
        freezeCheck();
        this.fingerprint = fingerprint;
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
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
