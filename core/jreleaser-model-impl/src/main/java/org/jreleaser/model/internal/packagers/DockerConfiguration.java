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
package org.jreleaser.model.internal.packagers;

import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.util.Env;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public interface DockerConfiguration extends Domain, ExtraProperties, Activatable {
    String TYPE = "docker";
    String LABEL_OCI_IMAGE_TITLE = "org.opencontainers.image.title";
    String LABEL_OCI_IMAGE_DESCRIPTION = "org.opencontainers.image.description";
    String LABEL_OCI_IMAGE_REVISION = "org.opencontainers.image.revision";
    String LABEL_OCI_IMAGE_VERSION = "org.opencontainers.image.version";
    String LABEL_OCI_IMAGE_LICENSES = "org.opencontainers.image.licenses";
    String LABEL_OCI_IMAGE_URL = "org.opencontainers.image.url";

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

    final class Registry extends AbstractModelObject<Registry> implements Domain, Comparable<Registry> {
        public static final String DEFAULT_NAME = "DEFAULT";

        private String server;
        private String serverName = DEFAULT_NAME;
        private String repositoryName;
        private String username;
        private String password;

        private final org.jreleaser.model.api.packagers.DockerConfiguration.Registry immutable = new org.jreleaser.model.api.packagers.DockerConfiguration.Registry() {
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
        public void merge(DockerConfiguration.Registry registry) {
            this.server = merge(this.server, registry.server);
            this.serverName = merge(this.serverName, registry.serverName);
            this.repositoryName = merge(this.repositoryName, registry.repositoryName);
            this.username = merge(this.username, registry.username);
            this.password = merge(this.password, registry.password);
        }

        public String getResolvedPassword() {
            return Env.env("DOCKER_" + Env.toVar(serverName) + "_PASSWORD", password);
        }

        public String getResolvedUsername() {
            return Env.env("DOCKER_" + Env.toVar(serverName) + "_USERNAME", username);
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

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("server", server);
            map.put("serverName", serverName);
            map.put("repositoryName", repositoryName);
            map.put("username", getResolvedUsername());
            map.put("password", isNotBlank(password) ? HIDE : UNSET);
            return map;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DockerConfiguration.Registry that = (DockerConfiguration.Registry) o;
            return serverName.equals(that.serverName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serverName);
        }

        @Override
        public int compareTo(Registry o) {
            if (null == o) return -1;
            return serverName.compareTo(o.getServerName());
        }
    }
}
