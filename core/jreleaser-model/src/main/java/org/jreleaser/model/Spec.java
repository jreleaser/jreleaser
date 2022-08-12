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
public class Spec extends AbstractRepositoryPackager<Spec> {
    public static final String TYPE = "spec";
    public static final String SKIP_SPEC = "skipSpec";

    private static final Map<Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

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

        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_IMAGE, extensions);
    }

    private final List<String> requires = new ArrayList<>();
    private final SpecRepository repository = new SpecRepository();

    private String packageName;
    private String release;

    public Spec() {
        super(TYPE);
    }

    @Override
    public void freeze() {
        super.freeze();
        repository.freeze();
    }

    @Override
    public void merge(Spec spec) {
        freezeCheck();
        super.merge(spec);
        this.packageName = merge(this.packageName, spec.packageName);
        this.release = merge(this.release, spec.release);
        setRepository(spec.repository);
        setRequires(merge(this.requires, spec.requires));
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        freezeCheck();
        this.packageName = packageName;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        freezeCheck();
        this.release = release;
    }

    public SpecRepository getRepository() {
        return repository;
    }

    public void setRepository(SpecRepository repository) {
        this.repository.merge(repository);
    }

    public List<String> getRequires() {
        return freezeWrap(requires);
    }

    public void setRequires(List<String> requires) {
        freezeCheck();
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
        return repository;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) ||
            (PlatformUtils.isLinux(platform) && PlatformUtils.isIntel(platform) && !PlatformUtils.isAlpineLinux(platform));
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
        return isFalse(artifact.getExtraProperties().get(SKIP_SPEC));
    }

    public static class SpecRepository extends AbstractRepositoryTap<SpecRepository> {
        public SpecRepository() {
            super("spec", "spec");
        }
    }
}
