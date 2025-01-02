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
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.FileType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractPackager<A extends org.jreleaser.model.api.packagers.Packager, S extends AbstractPackager<A, S>> extends AbstractActivatable<S> implements Packager<A> {
    private static final long serialVersionUID = 7585826887268273790L;

    @JsonIgnore
    private final String type;
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    protected Boolean continueOnError;
    private String downloadUrl;
    @JsonIgnore
    private boolean failed;

    protected AbstractPackager(String type) {
        this.type = type;
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.continueOnError = merge(this.continueOnError, source.continueOnError);
        this.downloadUrl = merge(this.downloadUrl, source.getDownloadUrl());
        this.failed = source.isFailed();
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
    }

    @Override
    public Set<Stereotype> getSupportedStereotypes() {
        return EnumSet.allOf(Stereotype.class);
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

    protected abstract boolean isNotSkipped(Artifact artifact);

    @Override
    public boolean isSnapshotSupported() {
        return false;
    }

    @Override
    public String prefix() {
        return getType();
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
    public boolean resolveEnabled(Project project, Distribution distribution) {
        resolveEnabled(project);
        if (!supportsDistribution(distribution.getType())) {
            disable();
        }
        return isEnabled();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
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
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", getActive());
        props.put("continueOnError", isContinueOnError());
        props.put("downloadUrl", downloadUrl);
        asMap(full, props);
        props.put("extraProperties", getExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getType(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);
}
