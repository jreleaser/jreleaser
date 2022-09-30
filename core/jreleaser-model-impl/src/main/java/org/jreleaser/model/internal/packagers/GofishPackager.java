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
package org.jreleaser.model.internal.packagers;

import org.jreleaser.model.Active;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.api.packagers.GofishPackager.SKIP_GOFISH;
import static org.jreleaser.model.api.packagers.GofishPackager.TYPE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.TAR;
import static org.jreleaser.util.FileType.TAR_GZ;
import static org.jreleaser.util.FileType.TAR_XZ;
import static org.jreleaser.util.FileType.TGZ;
import static org.jreleaser.util.FileType.TXZ;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public final class GofishPackager extends AbstractRepositoryPackager<org.jreleaser.model.api.packagers.GofishPackager, GofishPackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        Set<String> extensions = setOf(
            TAR_GZ.extension(),
            TAR_XZ.extension(),
            TGZ.extension(),
            TXZ.extension(),
            TAR.extension(),
            ZIP.extension());

        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_IMAGE, extensions);
    }

    private final GofishRepository repository = new GofishRepository();

    private final org.jreleaser.model.api.packagers.GofishPackager immutable = new org.jreleaser.model.api.packagers.GofishPackager() {
        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getPackagerRepository() {
            return getRepository();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return commitAuthor.asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return templateDirectory;
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(skipTemplates);
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getDownloadUrl() {
            return downloadUrl;
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return GofishPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return GofishPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return GofishPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return GofishPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return GofishPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return GofishPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return GofishPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GofishPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return GofishPackager.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public GofishPackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.GofishPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(GofishPackager source) {
        super.merge(source);
        setRepository(source.repository);
    }

    public GofishRepository getRepository() {
        return repository;
    }

    public void setRepository(GofishRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("repository", repository.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return getPackagerRepository();
    }

    public PackagerRepository getPackagerRepository() {
        return repository;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) ||
            PlatformUtils.isMac(platform) ||
            PlatformUtils.isWindows(platform) ||
            PlatformUtils.isLinux(platform) && !PlatformUtils.isAlpineLinux(platform);
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
        return isFalse(artifact.getExtraProperties().get(SKIP_GOFISH));
    }

    public static final class GofishRepository extends PackagerRepository {
        public GofishRepository() {
            super("gofish", "fish-food");
        }
    }
}
