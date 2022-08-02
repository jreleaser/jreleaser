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
package org.jreleaser.model.releaser.spi;

import org.jreleaser.util.Version;

import java.util.Date;
import java.util.Objects;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class Release {
    private final String name;
    private final String tagName;
    private final String url;
    private final Date publishedAt;
    private Version version;

    public Release(String name, String tagName, String url, Date publishedAt) {
        this.name = name;
        this.tagName = tagName;
        this.url = url;
        this.publishedAt = publishedAt;
    }

    public String getName() {
        return name;
    }

    public String getTagName() {
        return tagName;
    }

    public String getUrl() {
        return url;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Release release = (Release) o;
        return name.equals(release.name) &&
            tagName.equals(release.tagName) &&
            url.equals(release.url) &&
            publishedAt.equals(release.publishedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tagName, url, publishedAt);
    }

    @Override
    public String toString() {
        return "Release[" +
            "name='" + name + '\'' +
            ", tagName='" + tagName + '\'' +
            ", version='" + version + '\'' +
            ", url='" + url + '\'' +
            ", publishedAt=" + publishedAt +
            "]";
    }
}
