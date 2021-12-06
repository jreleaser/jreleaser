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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
public class Spec extends AbstractRepositoryTool {
    public static final String NAME = "spec";
    private final List<String> requires = new ArrayList<>();
    private final SpecRepository repository = new SpecRepository();

    private String release;

    public Spec() {
        super(NAME);
    }

    void setAll(Spec spec) {
        super.setAll(spec);
        this.release = spec.release;
        setRepository(spec.repository);
        setRequires(spec.requires);
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public SpecRepository getRepository() {
        return repository;
    }

    public void setRepository(SpecRepository repository) {
        this.repository.setAll(repository);
    }

    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(List<String> requires) {
        this.requires.clear();
        this.requires.addAll(requires);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
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
        return isBlank(platform);
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return distribution.getType() == Distribution.DistributionType.JAVA_BINARY;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        Set<String> set = new LinkedHashSet<>();
        set.add(".tar.bz2");
        set.add(".tar.gz");
        set.add(".tgz");
        set.add(".tar");
        return set;
    }
}
