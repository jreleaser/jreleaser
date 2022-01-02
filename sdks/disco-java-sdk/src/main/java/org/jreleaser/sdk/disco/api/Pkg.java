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
package org.jreleaser.sdk.disco.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pkg {
    private String id;
    private String version;
    private String archiveType = "zip";
    private String distribution;
    private String distributionVersion;
    private String operatingSystem;
    private String architecture = "x86-64";
    private String libc_type;
    private boolean javafxBundled;
    private boolean directlyDownloadable;
    private String filename;
    private Links links;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(String archiveType) {
        this.archiveType = archiveType;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public String getDistributionVersion() {
        return distributionVersion;
    }

    public void setDistributionVersion(String distributionVersion) {
        this.distributionVersion = distributionVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
        if ("linux_musl".equalsIgnoreCase(operatingSystem) ||
            "alpine_linux".equalsIgnoreCase(operatingSystem)) {
            libc_type = "musl";
        }
        if ("linux".equalsIgnoreCase(operatingSystem)) {
            libc_type = "glibc";
        }
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getLibc_type() {
        return libc_type;
    }

    public void setLibc_type(String libc_type) {
        this.libc_type = libc_type;
    }

    public boolean isJavafxBundled() {
        return javafxBundled;
    }

    public void setJavafxBundled(boolean javafxBundled) {
        this.javafxBundled = javafxBundled;
    }

    public boolean isDirectlyDownloadable() {
        return directlyDownloadable;
    }

    public void setDirectlyDownloadable(boolean directlyDownloadable) {
        this.directlyDownloadable = directlyDownloadable;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String formatAsQuery() {
        String s = "[" +
            "version='" + version + '\'' +
            ", distribution='" + distribution + '\'' +
            ", operatingSystem='" + operatingSystem + '\'' +
            ", architecture='" + architecture + '\'' +
            ", archiveType='" + archiveType + '\'' +
            ", javafxBundled='" + javafxBundled + '\'' +
            ", packageType='jdk'";
        if (isNotBlank(libc_type)) s += ", libcType='" + libc_type + '\'';
        s += ']';

        return s;
    }

    public Map<String, String> asQuery() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("version", version);
        map.put("distribution", distribution);
        map.put("operating_system", operatingSystem);
        map.put("architecture", architecture);
        map.put("archive_type", archiveType);
        map.put("javafx_bundled", String.valueOf(javafxBundled));
        map.put("package_type", "jdk");
        if (isNotBlank(libc_type)) map.put("libc_type", libc_type);
        return map;
    }
}
