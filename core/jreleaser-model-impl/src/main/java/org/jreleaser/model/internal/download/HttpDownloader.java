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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.HttpDelegate;

import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.api.download.HttpDownloader.TYPE;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class HttpDownloader extends AbstractDownloader<org.jreleaser.model.api.download.HttpDownloader, HttpDownloader>
    implements org.jreleaser.model.internal.common.Http {
    private static final long serialVersionUID = -3365213730876579690L;

    private final HttpDelegate delegate = new HttpDelegate();

    @JsonIgnore
    private final org.jreleaser.model.api.download.HttpDownloader immutable = new org.jreleaser.model.api.download.HttpDownloader() {
        private static final long serialVersionUID = -1955685895966905403L;

        private List<? extends Asset> assets;

        @Override
        public String getUsername() {
            return HttpDownloader.this.getUsername();
        }

        @Override
        public String getPassword() {
            return HttpDownloader.this.getPassword();
        }

        @Override
        public Authorization getAuthorization() {
            return HttpDownloader.this.getAuthorization();
        }

        @Override
        public Map<String, String> getHeaders() {
            return unmodifiableMap(HttpDownloader.this.getHeaders());
        }

        @Override
        public String getType() {
            return HttpDownloader.this.getType();
        }

        @Override
        public String getName() {
            return HttpDownloader.this.getName();
        }

        @Override
        public List<? extends Asset> getAssets() {
            if (null == assets) {
                assets = HttpDownloader.this.getAssets().stream()
                    .map(Downloader.Asset::asImmutable)
                    .collect(toList());
            }
            return assets;
        }

        @Override
        public Active getActive() {
            return HttpDownloader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return HttpDownloader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(HttpDownloader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return HttpDownloader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(HttpDownloader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return HttpDownloader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return HttpDownloader.this.getReadTimeout();
        }
    };

    public HttpDownloader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.download.HttpDownloader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(HttpDownloader source) {
        super.merge(source);
        delegate.merge(source.delegate);
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
    public Authorization getAuthorization() {
        return delegate.getAuthorization();
    }

    @Override
    public void setAuthorization(Authorization authorization) {
        delegate.setAuthorization(authorization);
    }

    @Override
    public void setAuthorization(String authorization) {
        delegate.setAuthorization(authorization);
    }

    @Override
    public Map<String, String> getHeaders() {
        return delegate.getHeaders();
    }

    public void setHeaders(Map<String, String> headers) {
        delegate.setHeaders(headers);
    }

    public void asMap(Map<String, Object> props) {
        delegate.asMap(props);
    }

    @Override
    public Authorization resolveAuthorization() {
        return delegate.resolveAuthorization();
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        delegate.asMap(props);
    }
}
