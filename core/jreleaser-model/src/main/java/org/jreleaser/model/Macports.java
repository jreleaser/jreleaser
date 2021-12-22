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
package org.jreleaser.model;

import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
public class Macports extends AbstractRepositoryTool {
    public static final String NAME = "macports";
    private final List<String> categories = new ArrayList<>();
    private final List<String> maintainers = new ArrayList<>();
    private final MacportsRepository repository = new MacportsRepository();

    private String packageName;
    private Integer revision;

    public Macports() {
        super(NAME);
    }

    void setAll(Macports macports) {
        super.setAll(macports);
        this.packageName = macports.packageName;
        this.revision = macports.revision;
        setRepository(macports.repository);
        setCategories(macports.categories);
        setMaintainers(macports.maintainers);
    }

    public List<String> getResolvedMaintainers(JReleaserContext context) {
        if (maintainers.isEmpty()) {
            Github github = context.getModel().getRelease().getGithub();
            if (github != null) {
                String maintainer = github.getResolvedUsername();
                if (isNotBlank(maintainer)) {
                    maintainers.add("@" + maintainer);
                }
            }
        }
        return maintainers;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public MacportsRepository getRepository() {
        return repository;
    }

    public void setRepository(MacportsRepository repository) {
        this.repository.setAll(repository);
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories.clear();
        this.categories.addAll(categories);
    }

    public List<String> getMaintainers() {
        return maintainers;
    }

    public void setMaintainers(List<String> maintainers) {
        this.maintainers.clear();
        this.maintainers.addAll(maintainers);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("packageName", packageName);
        props.put("revision", revision);
        props.put("categories", categories);
        props.put("maintainers", maintainers);
        props.put("repository", repository.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return repository;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) ||
            (PlatformUtils.isMac(platform) && PlatformUtils.isIntel(platform));
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return distribution.getType() == Distribution.DistributionType.JAVA_BINARY ||
            distribution.getType() == Distribution.DistributionType.JLINK ||
            distribution.getType() == Distribution.DistributionType.NATIVE_IMAGE ||
            distribution.getType() == Distribution.DistributionType.BINARY;
    }

    public static class MacportsRepository extends AbstractRepositoryTap {
        public MacportsRepository() {
            super("macports", "macports");
        }
    }
}
