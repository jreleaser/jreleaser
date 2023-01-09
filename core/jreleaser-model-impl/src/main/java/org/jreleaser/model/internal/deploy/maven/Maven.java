/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.model.internal.deploy.maven;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class Maven extends AbstractModelObject<Maven> implements Domain, Activatable {
    private static final long serialVersionUID = 5399032127872975911L;

    private final Map<String, ArtifactoryMavenDeployer> artifactory = new LinkedHashMap<>();
    private final Map<String, GiteaMavenDeployer> gitea = new LinkedHashMap<>();
    private final Map<String, GithubMavenDeployer> github = new LinkedHashMap<>();
    private final Map<String, GitlabMavenDeployer> gitlab = new LinkedHashMap<>();
    private final Map<String, Nexus2MavenDeployer> nexus2 = new LinkedHashMap<>();
    private final Pomchecker pomchecker = new Pomchecker();

    private Active active;
    @JsonIgnore
    private boolean enabled = true;

    private final org.jreleaser.model.api.deploy.maven.Maven immutable = new org.jreleaser.model.api.deploy.maven.Maven() {
        private static final long serialVersionUID = -4093252379809403524L;

        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer> artifactory;
        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer> gitea;
        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.GithubMavenDeployer> github;
        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer> gitlab;
        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer> nexus2;

        @Override
        public Map<String, ? extends org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer> getArtifactory() {
            if (null == artifactory) {
                artifactory = Maven.this.artifactory.values().stream()
                    .map(ArtifactoryMavenDeployer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.deploy.maven.MavenDeployer::getName, identity()));
            }
            return artifactory;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer> getGitea() {
            if (null == gitea) {
                gitea = Maven.this.gitea.values().stream()
                    .map(GiteaMavenDeployer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.deploy.maven.MavenDeployer::getName, identity()));
            }
            return gitea;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.deploy.maven.GithubMavenDeployer> getGithub() {
            if (null == github) {
                github = Maven.this.github.values().stream()
                    .map(GithubMavenDeployer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.deploy.maven.MavenDeployer::getName, identity()));
            }
            return github;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer> getGitlab() {
            if (null == gitlab) {
                gitlab = Maven.this.gitlab.values().stream()
                    .map(GitlabMavenDeployer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.deploy.maven.MavenDeployer::getName, identity()));
            }
            return gitlab;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer> getNexus2() {
            if (null == nexus2) {
                nexus2 = Maven.this.nexus2.values().stream()
                    .map(Nexus2MavenDeployer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.deploy.maven.MavenDeployer::getName, identity()));
            }
            return nexus2;
        }

        @Override
        public Pomchecker getPomchecker() {
            return pomchecker.asImmutable();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return Maven.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Maven.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.deploy.maven.Maven asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Maven source) {
        this.active = merge(this.active, source.active);
        this.enabled = merge(this.enabled, source.enabled);
        setArtifactory(mergeModel(this.artifactory, source.artifactory));
        setGitea(mergeModel(this.gitea, source.gitea));
        setGithub(mergeModel(this.github, source.github));
        setGitlab(mergeModel(this.gitlab, source.gitlab));
        setNexus2(mergeModel(this.nexus2, source.nexus2));
        setPomchecker(source.pomchecker);
    }

    public boolean isSet() {
        return !artifactory.isEmpty() ||
            !gitea.isEmpty() ||
            !github.isEmpty() ||
            !gitlab.isEmpty() ||
            !nexus2.isEmpty();
    }

    @Override
    public boolean isEnabled() {
        return enabled && active != null;
    }

    @Override
    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            setActive(Env.resolveOrDefault("deploy.maven.active", "", "ALWAYS"));
        }
        enabled = active.check(project);
        return enabled;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        setActive(Active.of(str));
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    public Optional<ArtifactoryMavenDeployer> getActiveArtifactory(String name) {
        return artifactory.values().stream()
            .filter(MavenDeployer::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<GiteaMavenDeployer> getActiveGitea(String name) {
        return gitea.values().stream()
            .filter(MavenDeployer::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<GithubMavenDeployer> getActiveGithub(String name) {
        return github.values().stream()
            .filter(MavenDeployer::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<GitlabMavenDeployer> getActiveGitlab(String name) {
        return gitlab.values().stream()
            .filter(MavenDeployer::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<Nexus2MavenDeployer> getActiveNexus2(String name) {
        return nexus2.values().stream()
            .filter(MavenDeployer::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public List<ArtifactoryMavenDeployer> getActiveArtifactories() {
        return artifactory.values().stream()
            .filter(ArtifactoryMavenDeployer::isEnabled)
            .collect(toList());
    }

    public Map<String, ArtifactoryMavenDeployer> getArtifactory() {
        return artifactory;
    }

    public void setArtifactory(Map<String, ArtifactoryMavenDeployer> artifactory) {
        this.artifactory.clear();
        this.artifactory.putAll(artifactory);
    }

    public void addArtifactory(ArtifactoryMavenDeployer artifactory) {
        this.artifactory.put(artifactory.getName(), artifactory);
    }

    public List<GiteaMavenDeployer> getActiveGiteas() {
        return gitea.values().stream()
            .filter(GiteaMavenDeployer::isEnabled)
            .collect(toList());
    }

    public Map<String, GiteaMavenDeployer> getGitea() {
        return gitea;
    }

    public void setGitea(Map<String, GiteaMavenDeployer> gitea) {
        this.gitea.clear();
        this.gitea.putAll(gitea);
    }

    public void addGitea(GiteaMavenDeployer gitea) {
        this.gitea.put(gitea.getName(), gitea);
    }

    public List<GithubMavenDeployer> getActiveGithubs() {
        return github.values().stream()
            .filter(GithubMavenDeployer::isEnabled)
            .collect(toList());
    }

    public Map<String, GithubMavenDeployer> getGithub() {
        return github;
    }

    public void setGithub(Map<String, GithubMavenDeployer> github) {
        this.github.clear();
        this.github.putAll(github);
    }

    public void addGithub(GithubMavenDeployer github) {
        this.github.put(github.getName(), github);
    }

    public List<GitlabMavenDeployer> getActiveGitlabs() {
        return gitlab.values().stream()
            .filter(GitlabMavenDeployer::isEnabled)
            .collect(toList());
    }

    public Map<String, GitlabMavenDeployer> getGitlab() {
        return gitlab;
    }

    public void setGitlab(Map<String, GitlabMavenDeployer> gitlab) {
        this.gitlab.clear();
        this.gitlab.putAll(gitlab);
    }

    public void addGitlab(GitlabMavenDeployer gitlab) {
        this.gitlab.put(gitlab.getName(), gitlab);
    }

    public List<Nexus2MavenDeployer> getActiveNexus2s() {
        return nexus2.values().stream()
            .filter(Nexus2MavenDeployer::isEnabled)
            .collect(toList());
    }

    public Map<String, Nexus2MavenDeployer> getNexus2() {
        return nexus2;
    }

    public void setNexus2(Map<String, Nexus2MavenDeployer> nexus2) {
        this.nexus2.clear();
        this.nexus2.putAll(nexus2);
    }

    public void addNexus2(Nexus2MavenDeployer nexus2) {
        this.nexus2.put(nexus2.getName(), nexus2);
    }

    public Pomchecker getPomchecker() {
        return pomchecker;
    }

    public void setPomchecker(Pomchecker pomchecker) {
        this.pomchecker.merge(pomchecker);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", enabled);
        map.put("active", active);
        map.put("pomchecker", pomchecker.asMap(full));

        List<Map<String, Object>> artifactory = this.artifactory.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!artifactory.isEmpty()) map.put("artifactory", artifactory);

        List<Map<String, Object>> gitea = this.gitea.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!gitea.isEmpty()) map.put("gitea", gitea);

        List<Map<String, Object>> github = this.github.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!github.isEmpty()) map.put("github", github);

        List<Map<String, Object>> gitlab = this.gitlab.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!gitlab.isEmpty()) map.put("gitlab", gitlab);

        List<Map<String, Object>> nexus2 = this.nexus2.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!nexus2.isEmpty()) map.put("nexus2", nexus2);

        return map;
    }

    public <A extends MavenDeployer<?>> Map<String, A> findMavenDeployersByType(String deployerType) {
        switch (deployerType) {
            case org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer.TYPE:
                return (Map<String, A>) artifactory;
            case org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer.TYPE:
                return (Map<String, A>) gitea;
            case org.jreleaser.model.api.deploy.maven.GithubMavenDeployer.TYPE:
                return (Map<String, A>) github;
            case org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer.TYPE:
                return (Map<String, A>) gitlab;
            case org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.TYPE:
                return (Map<String, A>) nexus2;
            default:
                return Collections.emptyMap();
        }
    }

    public <A extends MavenDeployer<?>> List<A> findAllActiveMavenDeployers() {
        List<A> deployers = new ArrayList<>();
        deployers.addAll((List<A>) getActiveArtifactories());
        deployers.addAll((List<A>) getActiveGiteas());
        deployers.addAll((List<A>) getActiveGithubs());
        deployers.addAll((List<A>) getActiveGitlabs());
        deployers.addAll((List<A>) getActiveNexus2s());
        return deployers;
    }

    public static final class Pomchecker extends AbstractModelObject<Pomchecker> implements Domain {
        private static final long serialVersionUID = 8467928554400937980L;

        private String version;

        private final org.jreleaser.model.api.deploy.maven.Maven.Pomchecker immutable = new org.jreleaser.model.api.deploy.maven.Maven.Pomchecker() {
            private static final long serialVersionUID = -7691641757680849149L;

            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Pomchecker.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.deploy.maven.Maven.Pomchecker asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Pomchecker source) {
            this.version = merge(this.version, source.version);
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("version", version);
            return map;
        }
    }
}
