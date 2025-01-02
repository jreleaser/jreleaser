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
import static org.jreleaser.model.api.packagers.DockerPackager.SKIP_DOCKER;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class DockerPackager extends AbstractDockerConfiguration<DockerPackager> implements RepositoryPackager<org.jreleaser.model.api.packagers.DockerPackager>, CommitAuthorAware {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = -8293471753814007950L;

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
        SUPPORTED.put(FLAT_BINARY, emptySet());
    }

    private final Map<String, DockerSpec> specs = new LinkedHashMap<>();
    private final CommitAuthor commitAuthor = new CommitAuthor();
    private final DockerRepository repository = new DockerRepository();

    private Boolean continueOnError;
    private String downloadUrl;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.DockerPackager immutable = new org.jreleaser.model.api.packagers.DockerPackager() {
        private static final long serialVersionUID = -8249625838795141546L;

        private Set<? extends org.jreleaser.model.api.packagers.DockerConfiguration.Registry> registries;
        private Map<String, ? extends org.jreleaser.model.api.packagers.DockerSpec> specs;

        @Override
        public Map<String, ? extends org.jreleaser.model.api.packagers.DockerSpec> getSpecs() {
            if (null == specs) {
                specs = DockerPackager.this.specs.values().stream()
                    .map(DockerSpec::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.packagers.DockerSpec::getName, identity()));
            }
            return specs;
        }

        @Override
        public DockerRepository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return commitAuthor.asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return DockerPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(DockerPackager.this.getSkipTemplates());
        }

        @Override
        public String getBaseImage() {
            return DockerPackager.this.getBaseImage();
        }

        @Override
        public Map<String, String> getLabels() {
            return unmodifiableMap(DockerPackager.this.getLabels());
        }

        @Override
        public Set<String> getImageNames() {
            return unmodifiableSet(DockerPackager.this.getImageNames());
        }

        @Override
        public List<String> getBuildArgs() {
            return unmodifiableList(DockerPackager.this.getBuildArgs());
        }

        @Override
        public List<String> getPreCommands() {
            return unmodifiableList(DockerPackager.this.getPreCommands());
        }

        @Override
        public List<String> getPostCommands() {
            return unmodifiableList(DockerPackager.this.getPostCommands());
        }

        @Override
        public Set<? extends org.jreleaser.model.api.packagers.DockerConfiguration.Registry> getRegistries() {
            if (null == registries) {
                registries = DockerPackager.this.getRegistries().stream()
                    .map(DockerConfiguration.Registry::asImmutable)
                    .collect(toSet());
            }
            return registries;
        }

        @Override
        public boolean isUseLocalArtifact() {
            return DockerPackager.this.isUseLocalArtifact();
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
            return DockerPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType) {
            return DockerPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType) {
            return DockerPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return DockerPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return DockerPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return DockerPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return DockerPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return DockerPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(DockerPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return DockerPackager.this.prefix();
        }

        @Override
        public Buildx getBuildx() {
            return DockerPackager.this.getBuildx().asImmutable();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(DockerPackager.this.getExtraProperties());
        }
    };

    @JsonIgnore
    private boolean failed;

    @Override
    public org.jreleaser.model.api.packagers.DockerPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(DockerPackager source) {
        super.merge(source);
        this.continueOnError = merge(this.continueOnError, source.continueOnError);
        this.downloadUrl = merge(this.downloadUrl, source.downloadUrl);
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
        return isFalse(artifact.getExtraProperties().get(SKIP_DOCKER));
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    @Override
    public void setCommitAuthor(CommitAuthor commitAuthor) {
        this.commitAuthor.merge(commitAuthor);
    }

    public List<DockerSpec> getActiveSpecs() {
        return specs.values().stream()
            .filter(DockerSpec::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, DockerSpec> getSpecs() {
        return specs;
    }

    public void setSpecs(Map<String, DockerSpec> specs) {
        this.specs.clear();
        this.specs.putAll(specs);
    }

    public void addSpecs(Map<String, DockerSpec> specs) {
        this.specs.putAll(specs);
    }

    public void addSpec(DockerSpec spec) {
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

    public void setRepository(DockerRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return getPackagerRepository();
    }

    public DockerRepository getPackagerRepository() {
        return repository;
    }

    public static final class DockerRepository extends AbstractRepositoryTap<DockerRepository> implements Domain {
        private static final long serialVersionUID = 6677470182457638257L;

        private Boolean versionedSubfolders;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.DockerPackager.DockerRepository immutable = new org.jreleaser.model.api.packagers.DockerPackager.DockerRepository() {
            private static final long serialVersionUID = 1452104357672519L;

            @Override
            public boolean isVersionedSubfolders() {
                return DockerRepository.this.isVersionedSubfolders();
            }

            @Override
            public String getBasename() {
                return DockerRepository.this.getBasename();
            }

            @Override
            public String getCanonicalRepoName() {
                return DockerRepository.this.getCanonicalRepoName();
            }

            @Override
            public String getName() {
                return DockerRepository.this.getName();
            }

            @Override
            public String getTagName() {
                return DockerRepository.this.getTagName();
            }

            @Override
            public String getBranch() {
                return DockerRepository.this.getBranch();
            }

            @Override
            public String getBranchPush() {
                return DockerRepository.this.getBranchPush();
            }

            @Override
            public String getUsername() {
                return DockerRepository.this.getUsername();
            }

            @Override
            public String getToken() {
                return DockerRepository.this.getToken();
            }

            @Override
            public String getCommitMessage() {
                return DockerRepository.this.getCommitMessage();
            }

            @Override
            public Active getActive() {
                return DockerRepository.this.getActive();
            }

            @Override
            public boolean isEnabled() {
                return DockerRepository.this.isEnabled();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(DockerRepository.this.asMap(full));
            }

            @Override
            public String getOwner() {
                return DockerRepository.this.getOwner();
            }

            @Override
            public String getPrefix() {
                return DockerRepository.this.prefix();
            }

            @Override
            public Map<String, Object> getExtraProperties() {
                return unmodifiableMap(DockerRepository.this.getExtraProperties());
            }
        };

        public DockerRepository() {
            super("docker", "docker");
        }

        public org.jreleaser.model.api.packagers.DockerPackager.DockerRepository asImmutable() {
            return immutable;
        }

        @Override
        public void merge(DockerRepository source) {
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
