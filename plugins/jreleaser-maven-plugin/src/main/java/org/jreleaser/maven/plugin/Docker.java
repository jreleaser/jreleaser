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
package org.jreleaser.maven.plugin;

import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Docker extends AbstractDockerConfiguration implements RepositoryTool {
    private final List<DockerSpec> specs = new ArrayList<>();
    private final CommitAuthor commitAuthor = new CommitAuthor();
    private final DockerRepository repository = new DockerRepository();

    private Boolean continueOnError;
    private String downloadUrl;

    void setAll(Docker docker) {
        super.setAll(docker);
        this.continueOnError = docker.continueOnError;
        this.downloadUrl = docker.downloadUrl;
        setSpecs(docker.specs);
        setCommitAuthor(docker.commitAuthor);
        setRepository(docker.repository);
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
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public boolean isContinueOnErrorSet() {
        return continueOnError != null;
    }

    @Override
    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    @Override
    public void setCommitAuthor(CommitAuthor commitAuthor) {
        this.commitAuthor.setAll(commitAuthor);
    }

    public DockerRepository getRepository() {
        return repository;
    }

    public void setRepository(DockerRepository repository) {
        this.repository.setAll(repository);
    }

    public List<DockerSpec> getSpecs() {
        return specs;
    }

    public void setSpecs(List<DockerSpec> specs) {
        this.specs.clear();
        this.specs.addAll(specs);
    }

    public boolean isSet() {
        return super.isSet() ||
            continueOnError != null ||
            isNotBlank(downloadUrl) ||
            commitAuthor.isSet() ||
            repository.isSet() ||
            !specs.isEmpty();
    }

    public static class DockerRepository extends Tap {
        private Boolean versionedSubfolders;

        void setAll(AbstractRepositoryTap tap) {
            super.setAll(tap);
            this.versionedSubfolders = ((DockerRepository) tap).versionedSubfolders;
        }

        public boolean isVersionedSubfolders() {
            return versionedSubfolders != null && versionedSubfolders;
        }

        public void setVersionedSubfolders(Boolean versionedSubfolders) {
            this.versionedSubfolders = versionedSubfolders;
        }

        public boolean isVersionedSubfoldersSet() {
            return versionedSubfolders != null;
        }
    }
}
