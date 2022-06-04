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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Release extends AbstractModelObject<Release> implements Domain {
    private Github github;
    private Gitlab gitlab;
    private Gitea gitea;
    private Codeberg codeberg;
    private GenericGit generic;

    @Override
    public void freeze() {
        super.freeze();
        if (null != github) github.freeze();
        if (null != gitlab) gitlab.freeze();
        if (null != gitea) gitea.freeze();
        if (null != codeberg) codeberg.freeze();
        if (null != generic) generic.freeze();
    }

    @Override
    public void merge(Release release) {
        freezeCheck();
        this.github = merge(this.github, release.github);
        this.gitlab = merge(this.gitlab, release.gitlab);
        this.gitea = merge(this.gitea, release.gitea);
        this.codeberg = merge(this.codeberg, release.codeberg);
        this.generic = merge(this.generic, release.generic);
    }

    public Github getGithub() {
        return github;
    }

    public void setGithub(Github github) {
        freezeCheck();
        this.github = github;
    }

    public Gitlab getGitlab() {
        return gitlab;
    }

    public void setGitlab(Gitlab gitlab) {
        freezeCheck();
        this.gitlab = gitlab;
    }

    public Gitea getGitea() {
        return gitea;
    }

    public void setGitea(Gitea gitea) {
        freezeCheck();
        this.gitea = gitea;
    }

    public Codeberg getCodeberg() {
        return codeberg;
    }

    public void setCodeberg(Codeberg codeberg) {
        freezeCheck();
        this.codeberg = codeberg;
    }

    public GenericGit getGeneric() {
        return generic;
    }

    public void setGeneric(GenericGit generic) {
        freezeCheck();
        this.generic = generic;
    }

    public GitService getGitService() {
        if (null != github) return github;
        if (null != gitlab) return gitlab;
        if (null != gitea) return gitea;
        if (null != codeberg) return codeberg;
        return generic;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (null != github) map.put(Github.NAME, github.asMap(full));
        if (null != gitlab) map.put(Gitlab.NAME, gitlab.asMap(full));
        if (null != gitea) map.put(Gitea.NAME, gitea.asMap(full));
        if (null != codeberg) map.put(Codeberg.NAME, codeberg.asMap(full));
        if (null != generic) map.put(GenericGit.NAME, generic.asMap(full));
        return map;
    }
}
