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
import org.jreleaser.model.Flatpak;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Icon;
import org.jreleaser.model.internal.common.Screenshot;
import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import static org.jreleaser.model.api.packagers.FlatpakPackager.SKIP_FLATPAK;
import static org.jreleaser.model.api.packagers.FlatpakPackager.TYPE;
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
 * @since 1.2.0
 */
public final class FlatpakPackager extends AbstractAppdataPackager<org.jreleaser.model.api.packagers.FlatpakPackager, FlatpakPackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = 1676752377583110248L;

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
        SUPPORTED.put(FLAT_BINARY, emptySet());
    }

    private final FlatpakRepository repository = new FlatpakRepository();
    private final Set<String> sdkExtensions = new LinkedHashSet<>();
    private final Set<String> finishArgs = new LinkedHashSet<>();
    private Flatpak.Runtime runtime;
    private String runtimeVersion;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.FlatpakPackager immutable = new org.jreleaser.model.api.packagers.FlatpakPackager() {
        private static final long serialVersionUID = -7925187932243488062L;

        private List<? extends org.jreleaser.model.api.common.Screenshot> screenshots;
        private List<? extends org.jreleaser.model.api.common.Icon> icons;

        @Override
        public String getComponentId() {
            return FlatpakPackager.this.getComponentId();
        }

        @Override
        public List<String> getCategories() {
            return unmodifiableList(FlatpakPackager.this.getCategories());
        }

        @Override
        public String getDeveloperName() {
            return FlatpakPackager.this.getDeveloperName();
        }

        @Override
        public Flatpak.Runtime getRuntime() {
            return runtime;
        }

        @Override
        public String getRuntimeVersion() {
            return runtimeVersion;
        }

        @Override
        public Set<String> getSdkExtensions() {
            return unmodifiableSet(sdkExtensions);
        }

        @Override
        public Set<String> getFinishArgs() {
            return unmodifiableSet(finishArgs);
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Screenshot> getScreenshots() {
            if (null == screenshots) {
                screenshots = FlatpakPackager.this.getScreenshots().stream()
                    .map(Screenshot::asImmutable)
                    .collect(toList());
            }
            return screenshots;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Icon> getIcons() {
            if (null == icons) {
                icons = FlatpakPackager.this.getIcons().stream()
                    .map(Icon::asImmutable)
                    .collect(toList());
            }
            return icons;
        }

        @Override
        public Set<String> getSkipReleases() {
            return unmodifiableSet(FlatpakPackager.this.getSkipReleases());
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
            return FlatpakPackager.this.getCommitAuthor().asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return FlatpakPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(FlatpakPackager.this.getSkipTemplates());
        }

        @Override
        public String getType() {
            return FlatpakPackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return FlatpakPackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return FlatpakPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return FlatpakPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return FlatpakPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return FlatpakPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return FlatpakPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return FlatpakPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return FlatpakPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return FlatpakPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(FlatpakPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return FlatpakPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(FlatpakPackager.this.getExtraProperties());
        }
    };

    public FlatpakPackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.FlatpakPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(FlatpakPackager source) {
        super.merge(source);
        this.runtime = merge(this.runtime, source.runtime);
        this.runtimeVersion = merge(this.runtimeVersion, source.runtimeVersion);
        setSdkExtensions(merge(this.sdkExtensions, source.sdkExtensions));
        setFinishArgs(merge(this.finishArgs, source.finishArgs));
        setRepository(source.repository);
    }

    @Override
    public Set<Stereotype> getSupportedStereotypes() {
        return setOf(Stereotype.CLI, Stereotype.DESKTOP);
    }

    public Flatpak.Runtime getRuntime() {
        return runtime;
    }

    public void setRuntime(Flatpak.Runtime runtime) {
        this.runtime = runtime;
    }

    public void setRuntime(String runtime) {
        setRuntime(Flatpak.Runtime.of(runtime));
    }

    public String getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setRuntimeVersion(String runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    public Set<String> getSdkExtensions() {
        return sdkExtensions;
    }

    public void setSdkExtensions(Set<String> sdkExtensions) {
        this.sdkExtensions.clear();
        this.sdkExtensions.addAll(sdkExtensions);
    }

    public Set<String> getFinishArgs() {
        return finishArgs;
    }

    public void setFinishArgs(Set<String> finishArgs) {
        this.finishArgs.clear();
        this.finishArgs.addAll(finishArgs);
    }

    public FlatpakRepository getRepository() {
        return repository;
    }

    public void setRepository(FlatpakRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> map) {
        super.asMap(full, map);
        map.put("runtime", runtime);
        map.put("runtimeVersion", runtimeVersion);
        map.put("sdkExtensions", sdkExtensions);
        map.put("finishArgs", finishArgs);
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
    public boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return SUPPORTED.containsKey(distributionType);
    }

    @Override
    public Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return unmodifiableSet(SUPPORTED.getOrDefault(distributionType, emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_FLATPAK));
    }

    public static final class FlatpakRepository extends PackagerRepository {
        private static final long serialVersionUID = 6821986475211460731L;

        public FlatpakRepository() {
            super("flatpak", "flatpak");
        }
    }
}
