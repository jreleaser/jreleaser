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

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Release {
    private Github github;
    private Gitlab gitlab;
    private Gitea gitea;
    private Codeberg codeberg;
    private GenericGit generic;

    void setAll(Release release) {
        this.github = release.github;
        this.gitlab = release.gitlab;
        this.gitea = release.gitea;
        this.codeberg = release.codeberg;
        this.generic = release.generic;
    }

    public Github getGithub() {
        return github;
    }

    public void setGithub(Github github) {
        this.github = github;
    }

    public Gitlab getGitlab() {
        return gitlab;
    }

    public void setGitlab(Gitlab gitlab) {
        this.gitlab = gitlab;
    }

    public Gitea getGitea() {
        return gitea;
    }

    public void setGitea(Gitea gitea) {
        this.gitea = gitea;
    }

    public Codeberg getCodeberg() {
        return codeberg;
    }

    public void setCodeberg(Codeberg codeberg) {
        this.codeberg = codeberg;
    }

    public GenericGit getGeneric() {
        return generic;
    }

    public void setGeneric(GenericGit generic) {
        this.generic = generic;
    }
}
