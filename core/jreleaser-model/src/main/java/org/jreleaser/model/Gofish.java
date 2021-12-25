/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public class Gofish extends AbstractRepositoryTool {
    public static final String NAME = "gofish";
    public static final String SKIP_GOFISH = "skipGofish";

    private final GofishRepository repository = new GofishRepository();

    public Gofish() {
        super(NAME);
    }

    void setAll(Gofish spec) {
        super.setAll(spec);
        setRepository(spec.repository);
    }

    public GofishRepository getRepository() {
        return repository;
    }

    public void setRepository(GofishRepository repository) {
        this.repository.setAll(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
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
            PlatformUtils.isWindows(platform) ||
            (PlatformUtils.isLinux(platform) && !PlatformUtils.isAlpineLinux(platform));
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return distribution.getType() == Distribution.DistributionType.JAVA_BINARY ||
            distribution.getType() == Distribution.DistributionType.JLINK ||
            distribution.getType() == Distribution.DistributionType.NATIVE_IMAGE ||
            distribution.getType() == Distribution.DistributionType.BINARY;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        Set<String> set = new LinkedHashSet<>();
        set.add(FileType.TAR_GZ.extension());
        set.add(FileType.TAR_XZ.extension());
        set.add(FileType.TGZ.extension());
        set.add(FileType.TXZ.extension());
        set.add(FileType.TAR.extension());
        set.add(FileType.ZIP.extension());
        return set;
    }

    public static class GofishRepository extends AbstractRepositoryTap {
        public GofishRepository() {
            super("gofish", "fish-food");
        }
    }
}
