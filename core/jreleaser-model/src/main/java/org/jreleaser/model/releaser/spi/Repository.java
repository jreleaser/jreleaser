/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.model.releaser.spi;

import java.net.URL;
import java.util.Objects;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Repository {
    private final String owner;
    private final String name;
    private final URL url;
    private final String gitUrl;
    private final String httpUrl;

    public Repository(String owner, String name, URL url, String gitUrl, String httpUrl) {
        this.owner = owner;
        this.name = name;
        this.url = url;
        this.gitUrl = gitUrl;
        this.httpUrl = httpUrl;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    @Override
    public String toString() {
        return "Repository[" +
            "owner='" + owner + '\'' +
            ", name='" + name + '\'' +
            ", url=" + url +
            ", gitUrl=" + gitUrl +
            ", httpUrl=" + httpUrl +
            "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repository that = (Repository) o;
        return owner.equals(that.owner) &&
            name.equals(that.name) &&
            url.equals(that.url) &&
            gitUrl.equals(that.gitUrl) &&
            httpUrl.equals(that.httpUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name, url, gitUrl, httpUrl);
    }
}
