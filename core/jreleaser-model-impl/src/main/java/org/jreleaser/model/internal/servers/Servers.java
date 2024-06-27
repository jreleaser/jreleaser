/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
package org.jreleaser.model.internal.servers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
public final class Servers extends AbstractActivatable<Servers> implements Domain {
    private static final long serialVersionUID = -5281019569983844830L;

    private final Map<String, GenericServer> generic = new LinkedHashMap<>();
    private final Map<String, FtpServer> ftp = new LinkedHashMap<>();
    private final Map<String, HttpServer> http = new LinkedHashMap<>();
    private final Map<String, SshServer> ssh = new LinkedHashMap<>();
    private final Map<String, GithubServer> github = new LinkedHashMap<>();
    private final Map<String, GitlabServer> gitlab = new LinkedHashMap<>();
    private final Map<String, GiteaServer> gitea = new LinkedHashMap<>();
    private final Map<String, ForgejoServer> forgejo = new LinkedHashMap<>();

    @JsonIgnore
    private final org.jreleaser.model.api.servers.Servers immutable = new org.jreleaser.model.api.servers.Servers() {
        private static final long serialVersionUID = 6130011822388944677L;

        private Map<String, ? extends org.jreleaser.model.api.servers.GenericServer> generic;
        private Map<String, ? extends org.jreleaser.model.api.servers.FtpServer> ftp;
        private Map<String, ? extends org.jreleaser.model.api.servers.HttpServer> http;
        private Map<String, ? extends org.jreleaser.model.api.servers.SshServer> ssh;
        private Map<String, ? extends org.jreleaser.model.api.servers.GithubServer> github;
        private Map<String, ? extends org.jreleaser.model.api.servers.GitlabServer> gitlab;
        private Map<String, ? extends org.jreleaser.model.api.servers.GiteaServer> gitea;
        private Map<String, ? extends org.jreleaser.model.api.servers.ForgejoServer> forgejo;

        @Override
        public Map<String, ? extends org.jreleaser.model.api.servers.GenericServer> getGeneric() {
            if (null == generic) {
                generic = Servers.this.generic.values().stream()
                    .map(GenericServer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.servers.Server::getName, identity()));
            }
            return generic;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.servers.FtpServer> getFtp() {
            if (null == ftp) {
                ftp = Servers.this.ftp.values().stream()
                    .map(FtpServer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.servers.Server::getName, identity()));
            }
            return ftp;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.servers.HttpServer> getHttp() {
            if (null == http) {
                http = Servers.this.http.values().stream()
                    .map(HttpServer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.servers.Server::getName, identity()));
            }
            return http;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.servers.SshServer> getSsh() {
            if (null == ssh) {
                ssh = Servers.this.ssh.values().stream()
                    .map(SshServer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.servers.Server::getName, identity()));
            }
            return ssh;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.servers.GithubServer> getGithub() {
            if (null == github) {
                github = Servers.this.github.values().stream()
                    .map(GithubServer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.servers.Server::getName, identity()));
            }
            return github;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.servers.GitlabServer> getGitlab() {
            if (null == gitlab) {
                gitlab = Servers.this.gitlab.values().stream()
                    .map(GitlabServer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.servers.Server::getName, identity()));
            }
            return gitlab;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.servers.GiteaServer> getGitea() {
            if (null == gitea) {
                gitea = Servers.this.gitea.values().stream()
                    .map(GiteaServer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.servers.Server::getName, identity()));
            }
            return gitea;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.servers.ForgejoServer> getForgejo() {
            if (null == forgejo) {
                forgejo = Servers.this.forgejo.values().stream()
                    .map(ForgejoServer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.servers.Server::getName, identity()));
            }
            return forgejo;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Servers.this.asMap(full));
        }
    };

    public Servers() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.servers.Servers asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Servers source) {
        super.merge(source);
        setGeneric(mergeModel(this.generic, source.generic));
        setFtp(mergeModel(this.ftp, source.ftp));
        setHttp(mergeModel(this.http, source.http));
        setSsh(mergeModel(this.ssh, source.ssh));
        setGithub(mergeModel(this.github, source.github));
        setGitlab(mergeModel(this.gitlab, source.gitlab));
        setGitea(mergeModel(this.gitea, source.gitea));
        setForgejo(mergeModel(this.forgejo, source.forgejo));
    }

    public Map<String, GenericServer> getGeneric() {
        return generic;
    }

    public void setGeneric(Map<String, GenericServer> generic) {
        this.generic.clear();
        this.generic.putAll(generic);
    }

    public void addGeneric(GenericServer generic) {
        this.generic.put(generic.getName(), generic);
    }

    public Map<String, FtpServer> getFtp() {
        return ftp;
    }

    public void setFtp(Map<String, FtpServer> ftp) {
        this.ftp.clear();
        this.ftp.putAll(ftp);
    }

    public void addFtp(FtpServer ftp) {
        this.ftp.put(ftp.getName(), ftp);
    }

    public Map<String, HttpServer> getHttp() {
        return http;
    }

    public void setHttp(Map<String, HttpServer> http) {
        this.http.clear();
        this.http.putAll(http);
    }

    public void addHttp(HttpServer http) {
        this.http.put(http.getName(), http);
    }

    public Map<String, SshServer> getSsh() {
        return ssh;
    }

    public void setSsh(Map<String, SshServer> ssh) {
        this.ssh.clear();
        this.ssh.putAll(ssh);
    }

    public void addSsh(SshServer ssh) {
        this.ssh.put(ssh.getName(), ssh);
    }

    public Map<String, GithubServer> getGithub() {
        return github;
    }

    public void setGithub(Map<String, GithubServer> github) {
        this.github.clear();
        this.github.putAll(github);
    }

    public void addGithub(GithubServer github) {
        this.github.put(github.getName(), github);
    }

    public Map<String, GitlabServer> getGitlab() {
        return gitlab;
    }

    public void setGitlab(Map<String, GitlabServer> gitlab) {
        this.gitlab.clear();
        this.gitlab.putAll(gitlab);
    }

    public void addGitlab(GitlabServer gitlab) {
        this.gitlab.put(gitlab.getName(), gitlab);
    }

    public Map<String, GiteaServer> getGitea() {
        return gitea;
    }

    public void setGitea(Map<String, GiteaServer> gitea) {
        this.gitea.clear();
        this.gitea.putAll(gitea);
    }

    public void addGitea(GiteaServer gitea) {
        this.gitea.put(gitea.getName(), gitea);
    }

    public Map<String, ForgejoServer> getForgejo() {
        return forgejo;
    }

    public void setForgejo(Map<String, ForgejoServer> forgejo) {
        this.forgejo.clear();
        this.forgejo.putAll(forgejo);
    }

    public void addForgejo(ForgejoServer forgejo) {
        this.forgejo.put(forgejo.getName(), forgejo);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();

        List<Map<String, Object>> ftp = this.ftp.values()
            .stream()
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!ftp.isEmpty()) map.put("ftp", ftp);

        List<Map<String, Object>> generic = this.generic.values()
            .stream()
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!generic.isEmpty()) map.put("generic", generic);

        List<Map<String, Object>> http = this.http.values()
            .stream()
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!http.isEmpty()) map.put("http", http);

        List<Map<String, Object>> ssh = this.ssh.values()
            .stream()
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!ssh.isEmpty()) map.put("ssh", ssh);

        List<Map<String, Object>> github = this.github.values()
            .stream()
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!github.isEmpty()) map.put("github", github);

        List<Map<String, Object>> gitlab = this.gitlab.values()
            .stream()
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!gitlab.isEmpty()) map.put("gitlab", gitlab);

        List<Map<String, Object>> gitea = this.gitea.values()
            .stream()
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!gitea.isEmpty()) map.put("gitea", gitea);

        List<Map<String, Object>> forgejo = this.forgejo.values()
            .stream()
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!gitea.isEmpty()) map.put("forgejo", forgejo);

        return map;
    }

    public <A extends Server<?>> Map<String, A> findServersByType(String serverType) {
        switch (serverType) {
            case org.jreleaser.model.api.servers.FtpServer.TYPE:
                return (Map<String, A>) ftp;
            case org.jreleaser.model.api.servers.GenericServer.TYPE:
                return (Map<String, A>) generic;
            case org.jreleaser.model.api.servers.HttpServer.TYPE:
                return (Map<String, A>) http;
            case org.jreleaser.model.api.servers.SshServer.TYPE:
                return (Map<String, A>) ssh;
            case org.jreleaser.model.api.servers.GithubServer.TYPE:
                return (Map<String, A>) github;
            case org.jreleaser.model.api.servers.GitlabServer.TYPE:
                return (Map<String, A>) gitlab;
            case org.jreleaser.model.api.servers.GiteaServer.TYPE:
                return (Map<String, A>) gitea;
            case org.jreleaser.model.api.servers.ForgejoServer.TYPE:
                return (Map<String, A>) forgejo;
            default:
                return Collections.emptyMap();
        }
    }

    public FtpServer ftpFor(String name) {
        if (isBlank(name)) return null;

        boolean found = false;
        FtpServer merged = new FtpServer();

        if (generic.containsKey(name)) {
            found = true;
            GenericServer s = generic.get(name);
            merged.mergeWith(s);
        }

        if (ftp.containsKey(name)) {
            found = true;
            FtpServer s = ftp.get(name);
            merged.mergeWith(s);
        }

        return found ? merged : null;
    }

    public HttpServer httpFor(String name) {
        if (isBlank(name)) return null;

        boolean found = false;
        HttpServer merged = new HttpServer();

        if (generic.containsKey(name)) {
            found = true;
            GenericServer s = generic.get(name);
            merged.mergeWith(s);
        }

        if (http.containsKey(name)) {
            found = true;
            HttpServer s = http.get(name);
            merged.mergeWith(s);
        }

        return found ? merged : null;
    }

    public SshServer sshFor(String name) {
        if (isBlank(name)) return null;

        boolean found = false;
        SshServer merged = new SshServer();

        if (generic.containsKey(name)) {
            found = true;
            GenericServer s = generic.get(name);
            merged.mergeWith(s);
        }

        if (ssh.containsKey(name)) {
            found = true;
            SshServer s = ssh.get(name);
            merged.mergeWith(s);
        }

        return found ? merged : null;
    }

    public GithubServer githubFor(String name) {
        if (isBlank(name)) return null;

        boolean found = false;
        GithubServer merged = new GithubServer();

        if (generic.containsKey(name)) {
            found = true;
            GenericServer s = generic.get(name);
            merged.mergeWith(s);
        }

        if (http.containsKey(name)) {
            found = true;
            HttpServer s = http.get(name);
            merged.mergeWith(s);
        }

        if (github.containsKey(name)) {
            found = true;
            GithubServer s = github.get(name);
            merged.mergeWith(s);
        }

        return found ? merged : null;
    }

    public GitlabServer gitlabFor(String name) {
        if (isBlank(name)) return null;

        boolean found = false;
        GitlabServer merged = new GitlabServer();

        if (generic.containsKey(name)) {
            found = true;
            GenericServer s = generic.get(name);
            merged.mergeWith(s);
        }

        if (http.containsKey(name)) {
            found = true;
            HttpServer s = http.get(name);
            merged.mergeWith(s);
        }

        if (gitlab.containsKey(name)) {
            found = true;
            GitlabServer s = gitlab.get(name);
            merged.mergeWith(s);
        }

        return found ? merged : null;
    }

    public GiteaServer giteaFor(String name) {
        if (isBlank(name)) return null;

        boolean found = false;
        GiteaServer merged = new GiteaServer();

        if (generic.containsKey(name)) {
            found = true;
            GenericServer s = generic.get(name);
            merged.mergeWith(s);
        }

        if (http.containsKey(name)) {
            found = true;
            HttpServer s = http.get(name);
            merged.mergeWith(s);
        }

        if (gitea.containsKey(name)) {
            found = true;
            GiteaServer s = gitea.get(name);
            merged.mergeWith(s);
        }

        return found ? merged : null;
    }

    public ForgejoServer forgejoFor(String name) {
        if (isBlank(name)) return null;

        boolean found = false;
        ForgejoServer merged = new ForgejoServer();

        if (generic.containsKey(name)) {
            found = true;
            GenericServer s = generic.get(name);
            merged.mergeWith(s);
        }

        if (http.containsKey(name)) {
            found = true;
            HttpServer s = http.get(name);
            merged.mergeWith(s);
        }

        if (forgejo.containsKey(name)) {
            found = true;
            ForgejoServer s = forgejo.get(name);
            merged.mergeWith(s);
        }

        return found ? merged : null;
    }
}
