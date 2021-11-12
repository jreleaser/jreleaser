/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
 * @since 0.9.0
 */
public class Pkg {
    private String name;
    private String version;
    private String archiveType = "zip";
    private String distribution = "zulu";
    private String platform;
    private boolean javafxBundled;

    public void validate(Errors errors) {
        if (isBlank(name)) {
            errors.configuration("jdk.name is missing");
            return;
        }

        if (isBlank(version)) {
            errors.configuration("jdk." + name + ".version is missing");
        }

        if (isBlank(platform)) {
            errors.configuration("jdk." + name + ".platform is missing");
        }

        if (isBlank(distribution)) {
            errors.configuration("jdk." + name + ".distribution is missing");
        }

        if (isBlank(archiveType)) {
            errors.configuration("jdk." + name + ".archiveType is missing");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isJavafxBundled() {
        return javafxBundled;
    }

    public void setJavafxBundled(boolean javafxBundled) {
        this.javafxBundled = javafxBundled;
    }

    @Override
    public String toString() {
        return '[' +
            "name='" + name + "'" +
            ", version='" + version + "'" +
            ", archiveType='" + archiveType + "'" +
            ", distribution='" + distribution + "'" +
            ", platform='" + platform + "'" +
            ", javafxBundled='" + javafxBundled + "'" +
            ']';
    }

    public org.jreleaser.sdk.disco.api.Pkg asDiscoPkg() {
        String[] parts = platform.split("-");
        String operatingSystem = parts[0];
        String architecture = "x86-64";
        if (parts.length == 2) architecture = parts[1];

        org.jreleaser.sdk.disco.api.Pkg pkg = new org.jreleaser.sdk.disco.api.Pkg();
        pkg.setVersion(version);
        pkg.setDistribution(distribution);
        pkg.setOperatingSystem(operatingSystem);
        pkg.setArchitecture(architecture);
        pkg.setArchiveType(archiveType);
        pkg.setJavafxBundled(javafxBundled);
        return pkg;
    }
}
