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
package org.jreleaser.model.spi.release;

import java.util.Objects;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Repository {
    private final String owner;
    private final String name;
    private final String url;
    private final String httpUrl;
    private final Kind kind;

    public Repository(Kind kind, String owner, String name, String url, String httpUrl) {
        this.kind = kind;
        this.owner = owner;
        this.name = name;
        this.url = url;
        this.httpUrl = httpUrl;
    }

    public Kind getKind() {
        return kind;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    @Override
    public String toString() {
        return "Repository[" +
            "kind='" + kind + '\'' +
            ", owner='" + owner + '\'' +
            ", name='" + name + '\'' +
            ", url='" + url + '\'' +
            ", httpUrl'=" + httpUrl + '\'' +
            "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        Repository that = (Repository) o;
        return kind.equals(that.kind) &&
            owner.equals(that.owner) &&
            name.equals(that.name) &&
            httpUrl.equals(that.httpUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, owner, name, httpUrl);
    }

    public enum Kind {
        GITHUB,
        GITLAB,
        CODEBERG,
        OTHER
    }
}
