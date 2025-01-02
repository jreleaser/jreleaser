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
package org.jreleaser.model.internal.deploy.maven;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;

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
public final class Maven extends AbstractActivatable<Maven> implements Domain, Activatable {
    private static final long serialVersionUID = 1126431134090848347L;

    private final Map<String, ArtifactoryMavenDeployer> artifactory = new LinkedHashMap<>();
    private final Map<String, AzureMavenDeployer> azure = new LinkedHashMap<>();
    private final Map<String, GiteaMavenDeployer> gitea = new LinkedHashMap<>();
    private final Map<String, GithubMavenDeployer> github = new LinkedHashMap<>();
    private final Map<String, GitlabMavenDeployer> gitlab = new LinkedHashMap<>();
    private final Map<String, Nexus2MavenDeployer> nexus2 = new LinkedHashMap<>();
    private final Map<String, MavenCentralMavenDeployer> mavenCentral = new LinkedHashMap<>();
    private final Pomchecker pomchecker = new Pomchecker();

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.maven.Maven immutable = new org.jreleaser.model.api.deploy.maven.Maven() {
        private static final long serialVersionUID = 6345180810995769120L;

        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer> artifactory;
        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.AzureMavenDeployer> azure;
        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer> gitea;
        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.GithubMavenDeployer> github;
        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer> gitlab;
        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer> nexus2;
        private Map<String, ? extends org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer> mavenCentral;

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
        public Map<String, ? extends org.jreleaser.model.api.deploy.maven.AzureMavenDeployer> getAzure() {
            if (null == azure) {
                azure = Maven.this.azure.values().stream()
                    .map(AzureMavenDeployer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.deploy.maven.MavenDeployer::getName, identity()));
            }
            return azure;
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
        public Map<String, ? extends org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer> getMavenCentral() {
            if (null == mavenCentral) {
                mavenCentral = Maven.this.mavenCentral.values().stream()
                    .map(MavenCentralMavenDeployer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.deploy.maven.MavenDeployer::getName, identity()));
            }
            return mavenCentral;
        }

        @Override
        public Pomchecker getPomchecker() {
            return pomchecker.asImmutable();
        }

        @Override
        public Active getActive() {
            return Maven.this.getActive();
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

    public Maven() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.deploy.maven.Maven asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Maven source) {
        super.merge(source);
        setArtifactory(mergeModel(this.artifactory, source.artifactory));
        setAzure(mergeModel(this.azure, source.azure));
        setGitea(mergeModel(this.gitea, source.gitea));
        setGithub(mergeModel(this.github, source.github));
        setGitlab(mergeModel(this.gitlab, source.gitlab));
        setNexus2(mergeModel(this.nexus2, source.nexus2));
        setMavenCentral(mergeModel(this.mavenCentral, source.mavenCentral));
        setPomchecker(source.pomchecker);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            !artifactory.isEmpty() ||
            !azure.isEmpty() ||
            !gitea.isEmpty() ||
            !github.isEmpty() ||
            !gitlab.isEmpty() ||
            !nexus2.isEmpty() ||
            !mavenCentral.isEmpty();
    }

    public Optional<ArtifactoryMavenDeployer> getActiveArtifactory(String name) {
        return artifactory.values().stream()
            .filter(MavenDeployer::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public Optional<AzureMavenDeployer> getActiveAzure(String name) {
        return azure.values().stream()
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

    public Optional<MavenCentralMavenDeployer> getActiveMavenCentral(String name) {
        return mavenCentral.values().stream()
            .filter(MavenDeployer::isEnabled)
            .filter(a -> name.equals(a.getName()))
            .findFirst();
    }

    public List<ArtifactoryMavenDeployer> getActiveArtifactories() {
        return artifactory.values().stream()
            .filter(ArtifactoryMavenDeployer::isEnabled)
            .collect(toList());
    }

    public List<AzureMavenDeployer> getActiveAzures() {
        return azure.values().stream()
            .filter(AzureMavenDeployer::isEnabled)
            .collect(toList());
    }

    public List<? extends MavenDeployer> getActiveDeployers() {
        List list = new ArrayList<>();
        list.addAll(getActiveArtifactories());
        list.addAll(getActiveAzures());
        list.addAll(getActiveGiteas());
        list.addAll(getActiveGithubs());
        list.addAll(getActiveGitlabs());
        list.addAll(getActiveNexus2s());
        list.addAll(getActiveMavenCentrals());
        return list;
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

    public Map<String, AzureMavenDeployer> getAzure() {
        return azure;
    }

    public void setAzure(Map<String, AzureMavenDeployer> azure) {
        this.azure.clear();
        this.azure.putAll(azure);
    }

    public void addAzure(AzureMavenDeployer azure) {
        this.azure.put(azure.getName(), azure);
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

    public List<MavenCentralMavenDeployer> getActiveMavenCentrals() {
        return mavenCentral.values().stream()
            .filter(MavenCentralMavenDeployer::isEnabled)
            .collect(toList());
    }

    public Map<String, MavenCentralMavenDeployer> getMavenCentral() {
        return mavenCentral;
    }

    public void setMavenCentral(Map<String, MavenCentralMavenDeployer> mavenCentral) {
        this.mavenCentral.clear();
        this.mavenCentral.putAll(mavenCentral);
    }

    public void addMavenCentral(MavenCentralMavenDeployer mavenCentral) {
        this.mavenCentral.put(mavenCentral.getName(), mavenCentral);
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
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.put("pomchecker", pomchecker.asMap(full));

        List<Map<String, Object>> artifactory = this.artifactory.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!artifactory.isEmpty()) map.put("artifactory", artifactory);

        List<Map<String, Object>> azure = this.azure.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!azure.isEmpty()) map.put("azure", azure);

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

        List<Map<String, Object>> mavenCentral = this.mavenCentral.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!mavenCentral.isEmpty()) map.put("mavenCentral", mavenCentral);

        return map;
    }

    public <A extends MavenDeployer<?>> Map<String, A> findMavenDeployersByType(String deployerType) {
        switch (deployerType) {
            case org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer.TYPE:
                return (Map<String, A>) artifactory;
            case org.jreleaser.model.api.deploy.maven.AzureMavenDeployer.TYPE:
                return (Map<String, A>) azure;
            case org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer.TYPE:
                return (Map<String, A>) gitea;
            case org.jreleaser.model.api.deploy.maven.GithubMavenDeployer.TYPE:
                return (Map<String, A>) github;
            case org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer.TYPE:
                return (Map<String, A>) gitlab;
            case org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.TYPE:
                return (Map<String, A>) nexus2;
            case org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.TYPE:
                return (Map<String, A>) mavenCentral;
            default:
                return Collections.emptyMap();
        }
    }

    public <A extends MavenDeployer<?>> List<A> findAllActiveMavenDeployers() {
        List<A> deployers = new ArrayList<>();
        deployers.addAll((List<A>) getActiveArtifactories());
        deployers.addAll((List<A>) getActiveAzures());
        deployers.addAll((List<A>) getActiveGiteas());
        deployers.addAll((List<A>) getActiveGithubs());
        deployers.addAll((List<A>) getActiveGitlabs());
        deployers.addAll((List<A>) getActiveNexus2s());
        deployers.addAll((List<A>) getActiveMavenCentrals());
        return deployers;
    }

    public static final class Pomchecker extends AbstractModelObject<Pomchecker> implements Domain {
        private static final long serialVersionUID = 5118388507019755650L;
        private String version;
        private Boolean failOnWarning;
        private Boolean failOnError;
        private Boolean strict;

        @JsonIgnore
        private final org.jreleaser.model.api.deploy.maven.Maven.Pomchecker immutable = new org.jreleaser.model.api.deploy.maven.Maven.Pomchecker() {
            private static final long serialVersionUID = 4509080707508578373L;

            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public boolean isFailOnWarning() {
                return failOnWarning;
            }

            @Override
            public boolean isFailOnError() {
                return failOnError;
            }

            @Override
            public boolean isStrict() {
                return strict;
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
            this.failOnWarning = merge(this.failOnWarning, source.failOnWarning);
            this.failOnError = merge(this.failOnError, source.failOnError);
            this.strict = merge(this.strict, source.strict);
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public boolean isFailOnWarning() {
            return failOnWarning != null && failOnWarning;
        }

        public boolean isFailOnWarningSet() {
            return failOnWarning != null;
        }

        public void setFailOnWarning(Boolean failOnWarning) {
            this.failOnWarning = failOnWarning;
        }

        public boolean isFailOnError() {
            return failOnError != null && failOnError;
        }

        public boolean isFailOnErrorSet() {
            return failOnError != null;
        }

        public void setFailOnError(Boolean failOnError) {
            this.failOnError = failOnError;
        }

        public boolean isStrict() {
            return strict != null && strict;
        }

        public boolean isStrictSet() {
            return strict != null;
        }

        public void setStrict(Boolean strict) {
            this.strict = strict;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("version", version);
            map.put("failOnWarning", isFailOnWarning());
            map.put("failOnError", isFailOnError());
            map.put("strict", isStrict());
            return map;
        }
    }
}
