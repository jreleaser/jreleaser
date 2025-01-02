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
package org.jreleaser.model.internal.packagers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
import static org.jreleaser.model.api.packagers.MacportsPackager.SKIP_MACPORTS;
import static org.jreleaser.model.api.packagers.MacportsPackager.TYPE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.DMG;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
public final class MacportsPackager extends AbstractRepositoryPackager<org.jreleaser.model.api.packagers.MacportsPackager, MacportsPackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = 6339866408211084697L;

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_PACKAGE, setOf(DMG.extension()));
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
        SUPPORTED.put(FLAT_BINARY, emptySet());
    }

    private final List<String> categories = new ArrayList<>();
    private final List<String> maintainers = new ArrayList<>();
    private final MacportsRepository repository = new MacportsRepository();

    private String packageName;
    private Integer revision;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.MacportsPackager immutable = new org.jreleaser.model.api.packagers.MacportsPackager() {
        private static final long serialVersionUID = 1625817436978161466L;

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public Integer getRevision() {
            return revision;
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public List<String> getCategories() {
            return unmodifiableList(categories);
        }

        @Override
        public List<String> getMaintainers() {
            return unmodifiableList(maintainers);
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getPackagerRepository() {
            return getRepository();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return MacportsPackager.this.getCommitAuthor().asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return MacportsPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(MacportsPackager.this.getSkipTemplates());
        }

        @Override
        public String getType() {
            return MacportsPackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return MacportsPackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return MacportsPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return MacportsPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return MacportsPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return MacportsPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return MacportsPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return MacportsPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return MacportsPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return MacportsPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(MacportsPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return MacportsPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(MacportsPackager.this.getExtraProperties());
        }
    };

    public MacportsPackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.MacportsPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(MacportsPackager source) {
        super.merge(source);
        this.packageName = merge(this.packageName, source.packageName);
        this.revision = merge(this.revision, source.revision);
        setRepository(source.repository);
        setCategories(merge(this.categories, source.categories));
        setMaintainers(merge(this.maintainers, source.maintainers));
    }

    public List<String> getResolvedMaintainers(JReleaserContext context) {
        if (maintainers.isEmpty()) {
            GithubReleaser github = context.getModel().getRelease().getGithub();
            if (null != github) {
                String maintainer = github.getUsername();
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
        this.repository.merge(repository);
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
        return getPackagerRepository();
    }

    public PackagerRepository getPackagerRepository() {
        return getRepository();
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) ||
            PlatformUtils.isMac(platform) && PlatformUtils.isIntel(platform);
    }

    @Override
    public boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return SUPPORTED.containsKey(distributionType);
    }

    @Override
    public Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return unmodifiableSet(SUPPORTED.getOrDefault(distributionType, emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_MACPORTS));
    }

    public static final class MacportsRepository extends PackagerRepository {
        private static final long serialVersionUID = 2162971034366096607L;

        public MacportsRepository() {
            super("macports", "macports");
        }
    }
}
