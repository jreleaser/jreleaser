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
package org.jreleaser.model.internal.download;

import org.jreleaser.model.internal.common.SshDelegate;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class AbstractSshDownloader<A extends org.jreleaser.model.api.download.Downloader, S extends AbstractSshDownloader<A, S>> extends AbstractDownloader<A, S> implements SshDownloader<A> {
    private static final long serialVersionUID = 7169250979780745522L;

    private final SshDelegate delegate = new SshDelegate();

    protected AbstractSshDownloader(String type) {
        super(type);
    }

    protected SshDelegate delegate() {
        return delegate;
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        delegate.merge(source.delegate());
    }

    @Override
    public String getUsername() {
        return delegate.getUsername();
    }

    @Override
    public void setUsername(String username) {
        delegate.setUsername(username);
    }

    @Override
    public String getPassword() {
        return delegate.getPassword();
    }

    @Override
    public void setPassword(String password) {
        delegate.setPassword(password);
    }

    @Override
    public String getHost() {
        return delegate.getHost();
    }

    @Override
    public void setHost(String host) {
        delegate.setHost(host);
    }

    @Override
    public Integer getPort() {
        return delegate.getPort();
    }

    @Override
    public void setPort(Integer port) {
        delegate.setPort(port);
    }

    @Override
    public String getKnownHostsFile() {
        return delegate.getKnownHostsFile();
    }

    @Override
    public void setKnownHostsFile(String knownHostsFile) {
        delegate.setKnownHostsFile(knownHostsFile);
    }

    @Override
    public String getPublicKey() {
        return delegate.getPublicKey();
    }

    @Override
    public void setPublicKey(String publicKey) {
        delegate.setPublicKey(publicKey);
    }

    @Override
    public String getPrivateKey() {
        return delegate.getPrivateKey();
    }

    @Override
    public void setPrivateKey(String privateKey) {
        delegate.setPrivateKey(privateKey);
    }

    @Override
    public String getPassphrase() {
        return delegate.getPassphrase();
    }

    @Override
    public void setPassphrase(String passphrase) {
        delegate.setPassphrase(passphrase);
    }

    @Override
    public String getFingerprint() {
        return delegate.getFingerprint();
    }

    @Override
    public void setFingerprint(String fingerprint) {
        delegate.setFingerprint(fingerprint);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        delegate.asMap(props);
    }
}
