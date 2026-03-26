/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.zernio.api;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.24.0
 */
public class Post {
    private String content;
    private boolean publishNow = true;
    private final Set<Platform> platforms = new LinkedHashSet<>();

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getPublishNow() {
        return publishNow;
    }

    public void setPublishNow(boolean publishNow) {
        this.publishNow = publishNow;
    }

    public Set<Platform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Set<Platform> platforms) {
        this.platforms.clear();
        this.platforms.addAll(platforms);
    }

    @Override
    public String toString() {
        return "Post[" +
            "content='" + content + '\'' +
            ", publishNow=" + publishNow +
            ", platforms=" + platforms +
            ']';
    }

    public static Post of(String content,
                          Set<Platform> platforms) {
        Post o = new Post();
        o.content = requireNonBlank(content, "'content' must not be blank").trim();
        o.setPlatforms(platforms);
        return o;
    }
}
