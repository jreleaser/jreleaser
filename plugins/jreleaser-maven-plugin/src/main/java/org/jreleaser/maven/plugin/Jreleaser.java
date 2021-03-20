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
package org.jreleaser.maven.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Jreleaser {
    private final Project project = new Project();
    private final Release release = new Release();
    private final Packagers packagers = new Packagers();
    private final Announce announce = new Announce();
    private final Signing signing = new Signing();
    private final Set<Artifact> files = new LinkedHashSet<>();
    private final List<Distribution> distributions = new ArrayList<>();

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project.setAll(project);
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release.setAll(release);
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

    public Signing getSigning() {
        return signing;
    }

    public void setSign(Signing signing) {
        this.signing.setAll(signing);
    }

    public Set<Artifact> getFiles() {
        return files;
    }

    public void setFiles(Set<Artifact> files) {
        this.files.clear();
        this.files.addAll(files);
    }

    public void addFiles(Set<Artifact> files) {
        this.files.addAll(files);
    }

    public void addFiles(Artifact artifact) {
        if (null != artifact) {
            this.files.add(artifact);
        }
    }

    public List<Distribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(Collection<Distribution> distributions) {
        this.distributions.clear();
        this.distributions.addAll(distributions);
    }

    public void addDistributions(Collection<Distribution> distributions) {
        this.distributions.addAll(distributions);
    }

    public Distribution findDistribution(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("Distribution name must not be blank");
        }

        if (distributions.isEmpty()) {
            throw new IllegalArgumentException("No distributions have been configured");
        }

        return distributions.stream()
            .filter(d -> name.equals(d.getName()))
            .findFirst()
            .orElseThrow((Supplier<IllegalArgumentException>) () -> {
                throw new IllegalArgumentException("Distribution '" + name + "' not found");
            });
    }
}
