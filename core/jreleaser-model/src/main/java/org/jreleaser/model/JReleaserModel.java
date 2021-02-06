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
package org.jreleaser.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserModel extends AbstractDomain {
    private final Project project = new Project();
    private final Release release = new Release();
    private final Packagers packagers = new Packagers();
    private final Map<String, Distribution> distributions = new LinkedHashMap<>();

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

    public Map<String, Distribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(Map<String, Distribution> distributions) {
        this.distributions.clear();
        this.distributions.putAll(distributions);
    }

    public void addDistributions(Map<String, Distribution> distributions) {
        this.distributions.putAll(distributions);
    }

    public void addDistribution(Distribution distribution) {
        this.distributions.put(distribution.getName(), distribution);
    }

    public Distribution findDistribution(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("Distribution name must not be blank");
        }

        if (distributions.isEmpty()) {
            throw new IllegalArgumentException("No distributions have been configured");
        }

        if (distributions.containsKey(name)) {
            return distributions.get(name);
        }

        throw new IllegalArgumentException("Distribution '" + name + "' not found");
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("project", project.asMap());
        map.put("release", release.asMap());
        map.put("packagers", packagers.asMap());
        map.put("distributions", distributions.values()
            .stream()
            .map(Distribution::asMap)
            .collect(Collectors.toList()));
        return map;
    }
}
