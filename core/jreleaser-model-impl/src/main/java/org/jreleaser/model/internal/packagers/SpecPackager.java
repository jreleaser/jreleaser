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
import org.jreleaser.model.internal.common.Artifact;
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
import static org.jreleaser.model.api.packagers.SpecPackager.SKIP_SPEC;
import static org.jreleaser.model.api.packagers.SpecPackager.TYPE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.TAR;
import static org.jreleaser.util.FileType.TAR_BZ2;
import static org.jreleaser.util.FileType.TAR_GZ;
import static org.jreleaser.util.FileType.TAR_XZ;
import static org.jreleaser.util.FileType.TBZ2;
import static org.jreleaser.util.FileType.TGZ;
import static org.jreleaser.util.FileType.TXZ;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
public final class SpecPackager extends AbstractRepositoryPackager<org.jreleaser.model.api.packagers.SpecPackager, SpecPackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = 3054130455318535496L;

    static {
        Set<String> extensions = setOf(
            TAR_BZ2.extension(),
            TAR_GZ.extension(),
            TAR_XZ.extension(),
            TBZ2.extension(),
            TGZ.extension(),
            TXZ.extension(),
            TAR.extension(),
            ZIP.extension());

        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(FLAT_BINARY, emptySet());
    }

    private final List<String> requires = new ArrayList<>();
    private final SpecRepository repository = new SpecRepository();

    private String packageName;
    private String release;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.SpecPackager immutable = new org.jreleaser.model.api.packagers.SpecPackager() {
        private static final long serialVersionUID = -3139422195483595734L;

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public String getRelease() {
            return release;
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public List<String> getRequires() {
            return unmodifiableList(requires);
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getPackagerRepository() {
            return getRepository();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return SpecPackager.this.getCommitAuthor().asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return SpecPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(SpecPackager.this.getSkipTemplates());
        }

        @Override
        public String getType() {
            return SpecPackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return SpecPackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return SpecPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return SpecPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return SpecPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return SpecPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return SpecPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return SpecPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return SpecPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return SpecPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SpecPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return SpecPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(SpecPackager.this.getExtraProperties());
        }
    };

    public SpecPackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.SpecPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SpecPackager source) {
        super.merge(source);
        this.packageName = merge(this.packageName, source.packageName);
        this.release = merge(this.release, source.release);
        setRepository(source.repository);
        setRequires(merge(this.requires, source.requires));
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public SpecRepository getRepository() {
        return repository;
    }

    public void setRepository(SpecRepository repository) {
        this.repository.merge(repository);
    }

    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(List<String> requires) {
        this.requires.clear();
        this.requires.addAll(requires);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("packageName", packageName);
        props.put("release", release);
        props.put("requires", requires);
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
            PlatformUtils.isLinux(platform) && PlatformUtils.isIntel(platform) && !PlatformUtils.isAlpineLinux(platform);
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
        return isFalse(artifact.getExtraProperties().get(SKIP_SPEC));
    }

    public static final class SpecRepository extends PackagerRepository {
        private static final long serialVersionUID = -4727714362653863706L;

        public SpecRepository() {
            super("spec", "spec");
        }
    }
}
