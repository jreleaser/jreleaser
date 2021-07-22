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

import org.jreleaser.util.PlatformUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Docker extends AbstractDockerConfiguration implements RepositoryTool {
    private final Map<String, DockerSpec> specs = new LinkedHashMap<>();
    private final CommitAuthor commitAuthor = new CommitAuthor();
    private final DockerRepository repository = new DockerRepository();

    private Boolean continueOnError;
    private boolean failed;

    void setAll(Docker docker) {
        super.setAll(docker);
        this.continueOnError = docker.continueOnError;
        this.failed = docker.failed;
        setSpecs(docker.specs);
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
        this.continueOnError = continueOnError;
    }

    @Override
    public boolean isContinueOnErrorSet() {
        return continueOnError != null;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || PlatformUtils.isUnix(platform);
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    @Override
    public void setCommitAuthor(CommitAuthor commitAuthor) {
        this.commitAuthor.setAll(commitAuthor);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        Set<String> extensions = new LinkedHashSet<>();
        extensions.add(".zip");
        extensions.add(".jar");
        return extensions;
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
        map.put(getName(), super.asMap(full));
        return map;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("commitAuthor", commitAuthor.asMap(full));
        props.put("repository", repository.asMap(full));
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
        this.repository.setAll(repository);
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return repository;
    }
}
