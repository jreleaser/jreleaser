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
package org.jreleaser.model.internal.packagers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.ExtraProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.DOCKER_IO;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public interface DockerConfiguration extends Domain, ExtraProperties, Activatable {
    String TYPE = "docker";

    String getTemplateDirectory();

    void setTemplateDirectory(String templateDirectory);

    List<String> getSkipTemplates();

    void setSkipTemplates(List<String> skipTemplates);

    void addSkipTemplates(List<String> skipTemplates);

    void addSkipTemplate(String skipTemplate);

    String getBaseImage();

    void setBaseImage(String baseImage);

    Map<String, String> getLabels();

    void setLabels(Map<String, String> labels);

    void addLabels(Map<String, String> labels);

    void addLabel(String key, String value);

    Set<String> getImageNames();

    void setImageNames(Set<String> imageNames);

    void addImageName(String imageName);

    List<String> getBuildArgs();

    void setBuildArgs(List<String> buildArgs);

    void addBuildArg(String buildArg);

    List<String> getPreCommands();

    void setPreCommands(List<String> preCommands);

    List<String> getPostCommands();

    void setPostCommands(List<String> postCommands);

    Set<? extends Registry> getRegistries();

    void setRegistries(Set<? extends Registry> registries);

    void addRegistry(Registry registry);

    boolean isUseLocalArtifact();

    void setUseLocalArtifact(Boolean useLocalArtifact);

    boolean isUseLocalArtifactSet();

    Buildx getBuildx();

    void setBuildx(Buildx buildx);

    final class Registry extends AbstractModelObject<Registry> implements Domain, Comparable<Registry> {
        public static final String DEFAULT_NAME = "DEFAULT";
        private static final long serialVersionUID = -1522955594088189796L;

        private String server;
        private String serverName = DEFAULT_NAME;
        private String repositoryName;
        private String username;
        private String password;
        private Boolean externalLogin;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.DockerConfiguration.Registry immutable = new org.jreleaser.model.api.packagers.DockerConfiguration.Registry() {
            private static final long serialVersionUID = -1273111436252150810L;

            @Override
            public String getServer() {
                return server;
            }

            @Override
            public String getServerName() {
                return serverName;
            }

            @Override
            public String getRepositoryName() {
                return repositoryName;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public boolean isExternalLogin() {
                return Registry.this.isExternalLogin();
            }

            @Override
            public int compareTo(org.jreleaser.model.api.packagers.DockerConfiguration.Registry o) {
                if (null == o) return -1;
                return serverName.compareTo(o.getServerName());
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(DockerConfiguration.Registry.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.packagers.DockerConfiguration.Registry asImmutable() {
            return immutable;
        }

        @Override
        public void merge(DockerConfiguration.Registry source) {
            this.server = merge(this.server, source.server);
            this.serverName = merge(this.serverName, source.serverName);
            this.repositoryName = merge(this.repositoryName, source.repositoryName);
            this.username = merge(this.username, source.username);
            this.password = merge(this.password, source.password);
            this.externalLogin = merge(this.externalLogin, source.externalLogin);
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isExternalLogin() {
            return null != externalLogin && externalLogin;
        }

        public void setExternalLogin(Boolean externalLogin) {
            this.externalLogin = externalLogin;
        }

        public boolean isExternalLoginSet() {
            return null != externalLogin;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("server", server);
            map.put("serverName", serverName);
            map.put("repositoryName", repositoryName);
            map.put("username", username);
            map.put("password", isNotBlank(password) ? HIDE : UNSET);
            map.put("externalLogin", isExternalLogin());
            return map;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (null == o || getClass() != o.getClass()) return false;
            DockerConfiguration.Registry that = (DockerConfiguration.Registry) o;
            String sn1 = serverName.equals(DEFAULT_NAME) ? DOCKER_IO : serverName;
            String sn2 = that.serverName.equals(DEFAULT_NAME) ? DOCKER_IO : that.serverName;
            return sn1.equals(sn2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serverName.equals(DEFAULT_NAME) ? DOCKER_IO : serverName);
        }

        @Override
        public int compareTo(Registry o) {
            if (null == o) return -1;
            String sn1 = serverName.equals(DEFAULT_NAME) ? DOCKER_IO : serverName;
            String sn2 = o.serverName.equals(DEFAULT_NAME) ? DOCKER_IO : o.serverName;
            return sn1.compareTo(sn2);
        }
    }

    final class Buildx extends AbstractModelObject<Buildx> implements Domain {
        private static final long serialVersionUID = -1508943969111212467L;

        private final List<String> createBuilderFlags = new ArrayList<>();
        private final List<String> platforms = new ArrayList<>();
        private Boolean enabled;
        private Boolean createBuilder;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.DockerConfiguration.Buildx immutable = new org.jreleaser.model.api.packagers.DockerConfiguration.Buildx() {
            private static final long serialVersionUID = -6178190371465420854L;

            @Override
            public boolean isEnabled() {
                return Buildx.this.isEnabled();
            }

            @Override
            public boolean isCreateBuilder() {
                return Buildx.this.isCreateBuilder();
            }

            @Override
            public List<String> getCreateBuilderFlags() {
                return unmodifiableList(Buildx.this.createBuilderFlags);
            }

            @Override
            public List<String> getPlatforms() {
                return unmodifiableList(Buildx.this.platforms);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(DockerConfiguration.Buildx.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.packagers.DockerConfiguration.Buildx asImmutable() {
            return immutable;
        }

        @Override
        public void merge(DockerConfiguration.Buildx source) {
            this.enabled = merge(this.enabled, source.enabled);
            this.createBuilder = merge(this.createBuilder, source.createBuilder);
            setCreateBuilderFlags(merge(this.createBuilderFlags, source.createBuilderFlags));
            setPlatforms(merge(this.platforms, source.platforms));
        }

        public List<String> getCreateBuilderFlags() {
            return createBuilderFlags;
        }

        public void setCreateBuilderFlags(List<String> createBuilderFlags) {
            this.createBuilderFlags.clear();
            this.createBuilderFlags.addAll(createBuilderFlags);
        }

        public List<String> getPlatforms() {
            return platforms;
        }

        public void setPlatforms(List<String> platforms) {
            this.platforms.clear();
            this.platforms.addAll(platforms);
        }

        public boolean isEnabled() {
            return null != enabled && enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabledSet() {
            return null != enabled;
        }

        public boolean isCreateBuilder() {
            return null == createBuilder || createBuilder;
        }

        public void setCreateBuilder(Boolean createBuilder) {
            this.createBuilder = createBuilder;
        }

        public boolean isCreateBuilderSet() {
            return null != createBuilder;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            if (!full && !isEnabled()) return Collections.emptyMap();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", isEnabled());
            map.put("createBuilder", isCreateBuilder());
            map.put("createBuilderFlags", createBuilderFlags);
            map.put("platforms", platforms);
            return map;
        }
    }
}
