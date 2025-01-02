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
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.CommitAuthor;
import org.jreleaser.model.internal.common.CommitAuthorAware;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
import static org.jreleaser.model.api.packagers.JibPackager.SKIP_JIB;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public final class JibPackager extends AbstractJibConfiguration<JibPackager> implements RepositoryPackager<org.jreleaser.model.api.packagers.JibPackager>, CommitAuthorAware {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = -5161609035832200577L;

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
        SUPPORTED.put(FLAT_BINARY, emptySet());
    }

    private final Map<String, JibSpec> specs = new LinkedHashMap<>();
    private final CommitAuthor commitAuthor = new CommitAuthor();
    private final JibRepository repository = new JibRepository();

    private Boolean continueOnError;
    private String downloadUrl;
    private String version;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.JibPackager immutable = new org.jreleaser.model.api.packagers.JibPackager() {
        private static final long serialVersionUID = -1607794151817682330L;

        private Set<? extends org.jreleaser.model.api.packagers.JibConfiguration.Registry> registries;
        private Map<String, ? extends org.jreleaser.model.api.packagers.JibSpec> specs;

        @Override
        public Map<String, ? extends org.jreleaser.model.api.packagers.JibSpec> getSpecs() {
            if (null == specs) {
                specs = JibPackager.this.specs.values().stream()
                    .map(JibSpec::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.packagers.JibSpec::getName, identity()));
            }
            return specs;
        }

        @Override
        public String getVersion() {
            return JibPackager.this.getVersion();
        }

        @Override
        public JibRepository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return commitAuthor.asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return JibPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(JibPackager.this.getSkipTemplates());
        }

        @Override
        public String getBaseImage() {
            return JibPackager.this.getBaseImage();
        }

        @Override
        public String getCreationTime() {
            return JibPackager.this.getCreationTime();
        }

        @Override
        public Format getFormat() {
            return JibPackager.this.getFormat();
        }

        @Override
        public Map<String, String> getEnvironment() {
            return unmodifiableMap(JibPackager.this.getEnvironment());
        }

        @Override
        public Set<String> getVolumes() {
            return unmodifiableSet(JibPackager.this.getVolumes());
        }

        @Override
        public Set<String> getExposedPorts() {
            return unmodifiableSet(JibPackager.this.getExposedPorts());
        }

        @Override
        public String getUser() {
            return JibPackager.this.getUser();
        }

        @Override
        public String getWorkingDirectory() {
            return JibPackager.this.getWorkingDirectory();
        }

        @Override
        public Map<String, String> getLabels() {
            return unmodifiableMap(JibPackager.this.getLabels());
        }

        @Override
        public Set<String> getImageNames() {
            return unmodifiableSet(JibPackager.this.getImageNames());
        }

        @Override
        public Set<? extends org.jreleaser.model.api.packagers.JibConfiguration.Registry> getRegistries() {
            if (null == registries) {
                registries = JibPackager.this.getRegistries().stream()
                    .map(JibConfiguration.Registry::asImmutable)
                    .collect(toSet());
            }
            return registries;
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getPackagerRepository() {
            return getRepository();
        }

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public String getDownloadUrl() {
            return downloadUrl;
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return JibPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType) {
            return JibPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType) {
            return JibPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return JibPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return JibPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return JibPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return JibPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return JibPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(JibPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return JibPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(JibPackager.this.getExtraProperties());
        }
    };

    @JsonIgnore
    private boolean failed;

    @Override
    public org.jreleaser.model.api.packagers.JibPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(JibPackager source) {
        super.merge(source);
        this.continueOnError = merge(this.continueOnError, source.continueOnError);
        this.downloadUrl = merge(this.downloadUrl, source.downloadUrl);
        this.version = merge(this.version, source.version);
        this.failed = source.failed;
        setSpecs(mergeModel(this.specs, source.specs));
        setCommitAuthor(source.commitAuthor);
        setRepository(source.repository);
    }

    @Override
    public boolean resolveEnabled(Project project, Distribution distribution) {
        resolveEnabled(project);
        if (!supportsDistribution(distribution.getType())) {
            disable();
        }
        return isEnabled();
    }

    @Override
    public void fail() {
        this.failed = true;
    }

    @Override
    public boolean isFailed() {
        return failed;
    }

    @Override
    public boolean isContinueOnError() {
        return null != continueOnError && continueOnError;
    }

    @Override
    public void setContinueOnError(Boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    @Override
    public boolean isContinueOnErrorSet() {
        return null != continueOnError;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || PlatformUtils.isUnix(platform);
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
    public Set<Stereotype> getSupportedStereotypes() {
        return EnumSet.allOf(Stereotype.class);
    }

    @Override
    public List<Artifact> resolveCandidateArtifacts(JReleaserContext context, Distribution distribution) {
        if (distribution.getType() == FLAT_BINARY && supportsDistribution(distribution.getType())) {
            return distribution.getArtifacts().stream()
                .filter(Artifact::isActiveAndSelected)
                .filter(artifact -> supportsPlatform(artifact.getPlatform()))
                .filter(this::isNotSkipped)
                .sorted(Artifact.comparatorByPlatform())
                .collect(toList());
        }

        List<String> fileExtensions = new ArrayList<>(getSupportedFileExtensions(distribution.getType()));
        fileExtensions.sort(naturalOrder());

        return distribution.getArtifacts().stream()
            .filter(Artifact::isActiveAndSelected)
            .filter(artifact -> fileExtensions.stream().anyMatch(ext -> artifact.getResolvedPath(context, distribution).toString().endsWith(ext)))
            .filter(artifact -> supportsPlatform(artifact.getPlatform()))
            .filter(this::isNotSkipped)
            .sorted(Artifact.comparatorByPlatform().thenComparingInt(artifact -> {
                String ext = FileType.getExtension(artifact.getResolvedPath(context, distribution));
                return fileExtensions.indexOf(ext);
            }))
            .collect(toList());
    }

    @Override
    public List<Artifact> resolveArtifacts(JReleaserContext context, Distribution distribution) {
        return resolveCandidateArtifacts(context, distribution).stream()
            .filter(Artifact::resolvedPathExists)
            .collect(toList());
    }

    private boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_JIB));
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    @Override
    public void setCommitAuthor(CommitAuthor commitAuthor) {
        this.commitAuthor.merge(commitAuthor);
    }

    public List<JibSpec> getActiveSpecs() {
        return specs.values().stream()
            .filter(JibSpec::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, JibSpec> getSpecs() {
        return specs;
    }

    public void setSpecs(Map<String, JibSpec> specs) {
        this.specs.clear();
        this.specs.putAll(specs);
    }

    public void addSpecs(Map<String, JibSpec> specs) {
        this.specs.putAll(specs);
    }

    public void addSpec(JibSpec spec) {
        this.specs.put(spec.getName(), spec);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getType(), super.asMap(full));
        return map;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("version", version);
        props.put("commitAuthor", commitAuthor.asMap(full));
        props.put("repository", repository.asMap(full));
        props.put("downloadUrl", downloadUrl);
        props.put("continueOnError", isContinueOnError());
        List<Map<String, Object>> specs = this.specs.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(Collectors.toList());
        if (!specs.isEmpty()) props.put("specs", specs);
    }

    public void setRepository(JibRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return getPackagerRepository();
    }

    public JibRepository getPackagerRepository() {
        return repository;
    }

    public static final class JibRepository extends AbstractRepositoryTap<JibRepository> implements Domain {
        private static final long serialVersionUID = 695045361878174791L;

        private Boolean versionedSubfolders;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.JibPackager.JibRepository immutable = new org.jreleaser.model.api.packagers.JibPackager.JibRepository() {
            private static final long serialVersionUID = -8630728744390799997L;

            @Override
            public boolean isVersionedSubfolders() {
                return JibRepository.this.isVersionedSubfolders();
            }

            @Override
            public String getBasename() {
                return JibRepository.this.getBasename();
            }

            @Override
            public String getCanonicalRepoName() {
                return JibRepository.this.getCanonicalRepoName();
            }

            @Override
            public String getName() {
                return JibRepository.this.getName();
            }

            @Override
            public String getTagName() {
                return JibRepository.this.getTagName();
            }

            @Override
            public String getBranch() {
                return JibRepository.this.getBranch();
            }

            @Override
            public String getBranchPush() {
                return JibRepository.this.getBranchPush();
            }

            @Override
            public String getUsername() {
                return JibRepository.this.getUsername();
            }

            @Override
            public String getToken() {
                return JibRepository.this.getToken();
            }

            @Override
            public String getCommitMessage() {
                return JibRepository.this.getCommitMessage();
            }

            @Override
            public Active getActive() {
                return JibRepository.this.getActive();
            }

            @Override
            public boolean isEnabled() {
                return JibRepository.this.isEnabled();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(JibRepository.this.asMap(full));
            }

            @Override
            public String getOwner() {
                return JibRepository.this.getOwner();
            }

            @Override
            public String getPrefix() {
                return JibRepository.this.prefix();
            }

            @Override
            public Map<String, Object> getExtraProperties() {
                return unmodifiableMap(JibRepository.this.getExtraProperties());
            }
        };

        public JibRepository() {
            super("jib", "jib");
        }

        public org.jreleaser.model.api.packagers.JibPackager.JibRepository asImmutable() {
            return immutable;
        }

        @Override
        public void merge(JibRepository source) {
            super.merge(source);
            this.versionedSubfolders = this.merge(this.versionedSubfolders, source.versionedSubfolders);
        }

        @Override
        public String prefix() {
            return "repository";
        }

        public boolean isVersionedSubfolders() {
            return null != versionedSubfolders && versionedSubfolders;
        }

        public void setVersionedSubfolders(Boolean versionedSubfolders) {
            this.versionedSubfolders = versionedSubfolders;
        }

        public boolean isVersionedSubfoldersSet() {
            return null != versionedSubfolders;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = super.asMap(full);
            map.put("versionedSubfolders", isVersionedSubfolders());
            return map;
        }
    }
}
