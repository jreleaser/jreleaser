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
package org.jreleaser.jdks.maven.plugin;

import org.jreleaser.util.Errors;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Jdk {
    private String name;
    private String platform;
    private String url;
    private String filename;
    private String checksum;
    private String checksumType = "sha256";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksumType() {
        return checksumType;
    }

    public void setChecksumType(String checksumType) {
        this.checksumType = checksumType;
    }

    public void validate(Errors errors) {
        if (isBlank(name)) {
            errors.configuration("jdk.name is missing");
            return;
        }

        if (isBlank(platform)) {
            errors.configuration("jdk." + name + ".platform is missing");
        }
        if (isBlank(url)) {
            errors.configuration("jdk." + name + ".url is missing");
        }
        if (isBlank(checksum)) {
            errors.configuration("jdk." + name + ".checksum is missing");
        }
        if (isBlank(checksumType)) {
            errors.configuration("jdk." + name + ".checksumType is missing");
        }
    }
}
