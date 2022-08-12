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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
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
public class Asdf extends AbstractRepositoryPackager<Asdf> {
    public static final String TYPE = "asdf";
    public static final String SKIP_ASDF = "skipAsdf";

    private static final Map<Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

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

    private final AsdfRepository repository = new AsdfRepository();

    private String toolCheck;

    public Asdf() {
        super(TYPE);
    }

    @Override
    public void merge(Asdf source) {
        freezeCheck();
        super.merge(source);
        this.toolCheck = merge(this.toolCheck, source.toolCheck);
        setRepository(source.repository);
    }

    public String getToolCheck() {
        return toolCheck;
    }

    public void setToolCheck(String toolCheck) {
        freezeCheck();
        this.toolCheck = toolCheck;
    }

    public AsdfRepository getRepository() {
        return repository;
    }

    public void setRepository(AsdfRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("toolcheck", toolCheck);
        props.put("repository", repository.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return repository;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) ||
            PlatformUtils.isMac(platform) ||
            PlatformUtils.isLinux(platform);
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
        return isFalse(artifact.getExtraProperties().get(SKIP_ASDF));
    }

    public static class AsdfRepository extends AbstractRepositoryTap<AsdfRepository> {
        public AsdfRepository() {
            super("asdf", "asdf");
        }
    }
}
