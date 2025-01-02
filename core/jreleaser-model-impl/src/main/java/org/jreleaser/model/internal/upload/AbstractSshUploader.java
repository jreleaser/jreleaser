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
package org.jreleaser.model.internal.upload;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.SshDelegate;
import org.jreleaser.mustache.TemplateContext;

import java.util.Map;

import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class AbstractSshUploader<A extends org.jreleaser.model.api.upload.SshUploader, S extends AbstractSshUploader<A, S>> extends AbstractUploader<A, S> implements SshUploader<A> {
    private static final long serialVersionUID = 9028661488115999432L;

    private final SshDelegate delegate = new SshDelegate();
    private String path;
    private String downloadUrl;

    protected AbstractSshUploader(String type) {
        super(type);
    }

    protected SshDelegate delegate() {
        return delegate;
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.delegate.merge(source.delegate());
        this.path = merge(this.path, source.getPath());
        this.downloadUrl = merge(this.downloadUrl, source.getDownloadUrl());
    }

    @Override
    public String getResolvedPath(JReleaserContext context, Artifact artifact) {
        TemplateContext p = artifactProps(context.fullProps(), artifact);
        p.setAll(resolvedExtraProperties());
        return resolveTemplate(path, p);
    }

    @Override
    public String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact) {
        return getResolvedDownloadUrl(context.fullProps(), artifact);
    }

    @Override
    public String getResolvedDownloadUrl(TemplateContext props, Artifact artifact) {
        TemplateContext p = new TemplateContext(artifactProps(props, artifact));
        p.setAll(resolvedExtraProperties());
        return resolveTemplate(downloadUrl, p);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
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
        props.put("path", path);
        props.put("downloadUrl", downloadUrl);
    }
}
