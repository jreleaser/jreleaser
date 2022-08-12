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

import com.fasterxml.jackson.annotation.JsonIgnore;
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

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Docker extends AbstractDockerConfiguration<Docker> implements RepositoryPackager {
    public static final String SKIP_DOCKER = "skipDocker";

    private static final Map<Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
    }

    private final Map<String, DockerSpec> specs = new LinkedHashMap<>();
    private final CommitAuthor commitAuthor = new CommitAuthor();
    private final DockerRepository repository = new DockerRepository();

    private Boolean continueOnError;
    private String downloadUrl;
    @JsonIgnore
    private boolean failed;

    @Override
    public void freeze() {
        super.freeze();
        specs.values().forEach(DockerSpec::freeze);
        commitAuthor.freeze();
        repository.freeze();
    }

    @Override
    public void merge(Docker docker) {
        freezeCheck();
        super.merge(docker);
        this.continueOnError = merge(this.continueOnError, docker.continueOnError);
        this.downloadUrl = merge(this.downloadUrl, docker.downloadUrl);
        this.failed = docker.failed;
        setSpecs(mergeModel(this.specs, docker.specs));
        setCommitAuthor(docker.commitAuthor);
        setRepository(docker.repository);
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
        return continueOnError != null && continueOnError;
    }

    @Override
    public void setContinueOnError(Boolean continueOnError) {
        freezeCheck();
        this.continueOnError = continueOnError;
    }

    @Override
    public boolean isContinueOnErrorSet() {
        return continueOnError != null;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public void setDownloadUrl(String downloadUrl) {
        freezeCheck();
        this.downloadUrl = downloadUrl;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || PlatformUtils.isUnix(platform);
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
    public Set<Stereotype> getSupportedStereotypes() {
        return EnumSet.allOf(Stereotype.class);
    }

    @Override
    public List<Artifact> resolveCandidateArtifacts(JReleaserContext context, Distribution distribution) {
        List<String> fileExtensions = new ArrayList<>(getSupportedExtensions(distribution));
        fileExtensions.sort(naturalOrder());

        return distribution.getArtifacts().stream()
            .filter(Artifact::isActive)
            .filter(artifact -> fileExtensions.stream().anyMatch(ext -> artifact.getResolvedPath(context, distribution).toString().endsWith(ext)))
            .filter(artifact -> supportsPlatform(artifact.getPlatform()))
            .filter(this::isNotSkipped)
            .sorted(Artifact.comparatorByPlatform().thenComparingInt(artifact -> {
                String ext = FileType.getExtension(artifact.getResolvedPath(context, distribution));
                return fileExtensions.indexOf(ext);
            }))
            .collect(toList());
    }

    protected boolean isNotSkipped(Artifact artifact) {
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
        return freezeWrap(specs);
    }

    public void setSpecs(Map<String, DockerSpec> specs) {
        freezeCheck();
        this.specs.clear();
        this.specs.putAll(specs);
    }

    public void addSpecs(Map<String, DockerSpec> specs) {
        freezeCheck();
        this.specs.putAll(specs);
    }

    public void addSpec(DockerSpec spec) {
        freezeCheck();
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

    public DockerRepository getRepository() {
        return repository;
    }

    public void setRepository(DockerRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return repository;
    }

    public static class DockerRepository extends AbstractRepositoryTap<DockerRepository> {
        private Boolean versionedSubfolders;

        public DockerRepository() {
            super("docker", "docker");
        }

        @Override
        public void merge(DockerRepository tap) {
            freezeCheck();
            super.merge(tap);
            this.versionedSubfolders = this.merge(this.versionedSubfolders, tap.versionedSubfolders);
        }

        public boolean isVersionedSubfolders() {
            return versionedSubfolders != null && versionedSubfolders;
        }

        public void setVersionedSubfolders(Boolean versionedSubfolders) {
            freezeCheck();
            this.versionedSubfolders = versionedSubfolders;
        }

        public boolean isVersionedSubfoldersSet() {
            return versionedSubfolders != null;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = super.asMap(full);
            map.put("versionedSubfolders", isVersionedSubfolders());
            return map;
        }
    }
}
