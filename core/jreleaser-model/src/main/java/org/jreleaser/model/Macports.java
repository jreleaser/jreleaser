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
package org.jreleaser.model;

import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.DMG;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
public class Macports extends AbstractRepositoryPackager<Macports> {
    public static final String TYPE = "macports";
    public static final String SKIP_MACPORTS = "skipMacports";
    public static final String APP_NAME = "appName";

    private static final Map<Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(NATIVE_PACKAGE, setOf(DMG.extension()));
    }

    private final List<String> categories = new ArrayList<>();
    private final List<String> maintainers = new ArrayList<>();
    private final MacportsRepository repository = new MacportsRepository();

    private String packageName;
    private Integer revision;

    public Macports() {
        super(TYPE);
    }

    @Override
    public void freeze() {
        super.freeze();
        repository.freeze();
    }

    @Override
    public void merge(Macports macports) {
        freezeCheck();
        super.merge(macports);
        this.packageName = merge(this.packageName, macports.packageName);
        this.revision = merge(this.revision, macports.revision);
        setRepository(macports.repository);
        setCategories(merge(this.categories, macports.categories));
        setMaintainers(merge(this.maintainers, macports.maintainers));
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
        freezeCheck();
        this.packageName = packageName;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        freezeCheck();
        this.revision = revision;
    }

    public MacportsRepository getRepository() {
        return repository;
    }

    public void setRepository(MacportsRepository repository) {
        this.repository.merge(repository);
    }

    public List<String> getCategories() {
        return freezeWrap(categories);
    }

    public void setCategories(List<String> categories) {
        freezeCheck();
        this.categories.clear();
        this.categories.addAll(categories);
    }

    public List<String> getMaintainers() {
        return freezeWrap(maintainers);
    }

    public void setMaintainers(List<String> maintainers) {
        freezeCheck();
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
        return SUPPORTED.containsKey(distribution.getType());
    }

    @Override
    public Set<String> getSupportedExtensions(Distribution distribution) {
        return Collections.unmodifiableSet(SUPPORTED.getOrDefault(distribution.getType(), Collections.emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_MACPORTS));
    }

    public static class MacportsRepository extends AbstractRepositoryTap<MacportsRepository> {
        public MacportsRepository() {
            super("macports", "macports");
        }
    }
}
