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
package org.jreleaser.maven.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Jreleaser {
    private final Environment environment = new Environment();
    private final Project project = new Project();
    private final Platform platform = new Platform();
    private final Release release = new Release();
    private final Packagers packagers = new Packagers();
    private final Upload upload = new Upload();
    private final Announce announce = new Announce();
    private final Assemble assemble = new Assemble();
    private final Checksum checksum = new Checksum();
    private final Signing signing = new Signing();
    private final Files files = new Files();
    private final Map<String, Distribution> distributions = new LinkedHashMap<>();

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment.setAll(environment);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project.setAll(project);
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform.setAll(platform);
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release.setAll(release);
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload.setAll(upload);
    }

    public Packagers getPackagers() {
        return packagers;
    }

    public void setPackagers(Packagers packagers) {
        this.packagers.setAll(packagers);
    }

    public Announce getAnnounce() {
        return announce;
    }

    public void setAnnounce(Announce announce) {
        this.announce.setAll(announce);
    }

    public Assemble getAssemble() {
        return assemble;
    }

    public void setAssemble(Assemble assemble) {
        this.assemble.setAll(assemble);
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public void setChecksum(Checksum checksum) {
        this.checksum.setAll(checksum);
    }

    public Signing getSigning() {
        return signing;
    }

    public void setSign(Signing signing) {
        this.signing.setAll(signing);
    }

    public Files getFiles() {
        return files;
    }

    public void setFiles(Files files) {
        this.files.setAll(files);
    }

    public Map<String, Distribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(Map<String, Distribution> distributions) {
        this.distributions.clear();
        this.distributions.putAll(distributions);
    }
}
