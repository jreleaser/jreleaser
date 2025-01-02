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
package org.jreleaser.model.internal.release;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.JReleaserOutput.nag;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Release extends AbstractModelObject<Release> implements Domain {
    private static final long serialVersionUID = -7382956682399917298L;

    private GithubReleaser github;
    private GitlabReleaser gitlab;
    private GiteaReleaser gitea;
    private CodebergReleaser codeberg;
    private GenericGitReleaser generic;

    @JsonIgnore
    private final org.jreleaser.model.api.release.Release immutable = new org.jreleaser.model.api.release.Release() {
        private static final long serialVersionUID = 8607297611597648860L;

        @Override
        public org.jreleaser.model.api.release.GithubReleaser getGithub() {
            return null != github ? github.asImmutable() : null;
        }

        @Override
        public org.jreleaser.model.api.release.GitlabReleaser getGitlab() {
            return null != gitlab ? gitlab.asImmutable() : null;
        }

        @Override
        public org.jreleaser.model.api.release.GiteaReleaser getGitea() {
            return null != gitea ? gitea.asImmutable() : null;
        }

        @Override
        public org.jreleaser.model.api.release.CodebergReleaser getCodeberg() {
            return null != codeberg ? codeberg.asImmutable() : null;
        }

        @Override
        public org.jreleaser.model.api.release.GenericGitReleaser getGeneric() {
            return null != generic ? generic.asImmutable() : null;
        }

        @Override
        public org.jreleaser.model.api.release.Releaser getReleaser() {
            return Release.this.releaser();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Release.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.release.Release asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Release source) {
        this.github = merge(this.github, source.github);
        this.gitlab = merge(this.gitlab, source.gitlab);
        this.gitea = merge(this.gitea, source.gitea);
        this.codeberg = merge(this.codeberg, source.codeberg);
        this.generic = merge(this.generic, source.generic);
    }

    public GithubReleaser getGithub() {
        return github;
    }

    public void setGithub(GithubReleaser github) {
        this.github = github;
    }

    public GitlabReleaser getGitlab() {
        return gitlab;
    }

    public void setGitlab(GitlabReleaser gitlab) {
        this.gitlab = gitlab;
    }

    public GiteaReleaser getGitea() {
        return gitea;
    }

    public void setGitea(GiteaReleaser gitea) {
        this.gitea = gitea;
    }

    public CodebergReleaser getCodeberg() {
        return codeberg;
    }

    public void setCodeberg(CodebergReleaser codeberg) {
        this.codeberg = codeberg;
        nag("release.codeberg is deprecated since 1.6.0 and will be removed in 2.0.0. Use release.gitea instead");
    }

    public GenericGitReleaser getGeneric() {
        return generic;
    }

    public void setGeneric(GenericGitReleaser generic) {
        this.generic = generic;
    }

    public BaseReleaser<?, ?> getReleaser() {
        if (null != github) return github;
        if (null != gitlab) return gitlab;
        if (null != gitea) return gitea;
        if (null != codeberg) return codeberg;
        return generic;
    }

    public org.jreleaser.model.api.release.Releaser releaser() {
        if (null != github) return github.asImmutable();
        if (null != gitlab) return gitlab.asImmutable();
        if (null != gitea) return gitea.asImmutable();
        if (null != codeberg) return codeberg.asImmutable();
        if (null != generic) return generic.asImmutable();
        return null;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (null != github) map.put(org.jreleaser.model.api.release.GithubReleaser.TYPE, github.asMap(full));
        if (null != gitlab) map.put(org.jreleaser.model.api.release.GitlabReleaser.TYPE, gitlab.asMap(full));
        if (null != gitea) map.put(org.jreleaser.model.api.release.GiteaReleaser.TYPE, gitea.asMap(full));
        if (null != codeberg) map.put(org.jreleaser.model.api.release.CodebergReleaser.TYPE, codeberg.asMap(full));
        if (null != generic) map.put(org.jreleaser.model.api.release.GenericGitReleaser.TYPE, generic.asMap(full));
        return map;
    }
}
