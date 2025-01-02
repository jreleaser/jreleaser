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
package org.jreleaser.sdk.sdkman.api;

import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Release extends Candidate {
    private String platform = "UNIVERSAL";
    private String url;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Release[" +
            "candidate='" + candidate + '\'' +
            ", version='" + version + '\'' +
            ", platform='" + platform + '\'' +
            ", url='" + url + '\'' +
            ']';
    }

    public static Release of(String candidate,
                             String version,
                             String platform,
                             String url) {
        Release o = new Release();
        o.candidate = requireNonBlank(candidate, "'candidate' must not be blank").trim();
        o.version = requireNonBlank(version, "'version' must not be blank").trim();
        o.platform = isNotBlank(platform) ? platform.trim() : "UNIVERSAL";
        o.url = requireNonBlank(url, "'url' must not be blank").trim();
        return o;
    }
}
