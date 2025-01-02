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
import org.jreleaser.model.internal.common.Icon;
import org.jreleaser.model.internal.common.Screenshot;
import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
import static org.jreleaser.model.api.packagers.AppImagePackager.SKIP_APPIMAGE;
import static org.jreleaser.model.api.packagers.AppImagePackager.TYPE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.JAR;
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
 * @since 1.2.0
 */
public final class AppImagePackager extends AbstractAppdataPackager<org.jreleaser.model.api.packagers.AppImagePackager, AppImagePackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = -5619053700424178633L;

    static {
        Set<String> extensions = setOf(
            TAR_GZ.extension(),
            TAR_XZ.extension(),
            TGZ.extension(),
            TXZ.extension(),
            TAR.extension(),
            ZIP.extension());

        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
        SUPPORTED.put(FLAT_BINARY, emptySet());
    }

    private final AppImageRepository repository = new AppImageRepository();
    private Boolean requiresTerminal;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.AppImagePackager immutable = new org.jreleaser.model.api.packagers.AppImagePackager() {
        private static final long serialVersionUID = -5612869889684591103L;

        private List<? extends org.jreleaser.model.api.common.Screenshot> screenshots;
        private List<? extends org.jreleaser.model.api.common.Icon> icons;

        @Override
        public String getComponentId() {
            return AppImagePackager.this.getComponentId();
        }

        @Override
        public List<String> getCategories() {
            return unmodifiableList(AppImagePackager.this.getCategories());
        }

        @Override
        public String getDeveloperName() {
            return AppImagePackager.this.getDeveloperName();
        }

        @Override
        public boolean isRequiresTerminal() {
            return AppImagePackager.this.isRequiresTerminal();
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Screenshot> getScreenshots() {
            if (null == screenshots) {
                screenshots = AppImagePackager.this.getScreenshots().stream()
                    .map(Screenshot::asImmutable)
                    .collect(toList());
            }
            return screenshots;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Icon> getIcons() {
            if (null == icons) {
                icons = AppImagePackager.this.getIcons().stream()
                    .map(Icon::asImmutable)
                    .collect(toList());
            }
            return icons;
        }

        @Override
        public Set<String> getSkipReleases() {
            return unmodifiableSet(AppImagePackager.this.getSkipReleases());
        }

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
            return AppImagePackager.this.getCommitAuthor().asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return AppImagePackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(AppImagePackager.this.getSkipTemplates());
        }

        @Override
        public String getType() {
            return AppImagePackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return AppImagePackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return AppImagePackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return AppImagePackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return AppImagePackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return AppImagePackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return AppImagePackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return AppImagePackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return AppImagePackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return AppImagePackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(AppImagePackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return AppImagePackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(AppImagePackager.this.getExtraProperties());
        }
    };

    public AppImagePackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.AppImagePackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(AppImagePackager source) {
        super.merge(source);
        this.requiresTerminal = merge(this.requiresTerminal, source.requiresTerminal);
        setRepository(source.repository);
    }

    @Override
    public Set<Stereotype> getSupportedStereotypes() {
        return setOf(Stereotype.CLI, Stereotype.DESKTOP);
    }

    public boolean isRequiresTerminal() {
        return null != requiresTerminal && requiresTerminal;
    }

    public void setRequiresTerminal(Boolean requiresTerminal) {
        this.requiresTerminal = requiresTerminal;
    }

    public boolean isRequiresTerminalSet() {
        return null != requiresTerminal;
    }

    public AppImageRepository getRepository() {
        return repository;
    }

    public void setRepository(AppImageRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> map) {
        super.asMap(full, map);
        map.put("requiresTerminal", isRequiresTerminal());
        map.put("repository", repository.asMap(full));
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
            PlatformUtils.isLinux(platform) && PlatformUtils.isIntel64(platform) && !PlatformUtils.isAlpineLinux(platform);
    }

    @Override
    public boolean supportsDistribution(Distribution.DistributionType distributionType) {
        return SUPPORTED.containsKey(distributionType);
    }

    @Override
    public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
        return unmodifiableSet(SUPPORTED.getOrDefault(distributionType, emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_APPIMAGE));
    }

    public static final class AppImageRepository extends PackagerRepository {
        private static final long serialVersionUID = -2610749875787410531L;

        public AppImageRepository() {
            super("appimage", "appimage");
        }
    }
}
