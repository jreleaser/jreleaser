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
 * @since 1.6.0
 */
public interface JibConfiguration extends Domain, ExtraProperties, Activatable {
    String TYPE = "jib";

    String getTemplateDirectory();

    void setTemplateDirectory(String templateDirectory);

    List<String> getSkipTemplates();

    void setSkipTemplates(List<String> skipTemplates);

    void addSkipTemplates(List<String> skipTemplates);

    void addSkipTemplate(String skipTemplate);

    String getBaseImage();

    void setBaseImage(String baseImage);

    Set<String> getImageNames();

    void setImageNames(Set<String> imageNames);

    void addImageName(String imageName);

    String getWorkingDirectory();

    void setWorkingDirectory(String workingDirectory);

    String getUser();

    void setUser(String user);

    String getCreationTime();

    void setCreationTime(String creationTime);

    org.jreleaser.model.api.packagers.JibConfiguration.Format getFormat();

    void setFormat(org.jreleaser.model.api.packagers.JibConfiguration.Format format);

    void setFormat(String format);

    Map<String, String> getEnvironment();

    void setEnvironment(Map<String, String> environment);

    void addEnvironment(Map<String, String> environment);

    void addEnvironment(String key, String value);

    Map<String, String> getLabels();

    void setLabels(Map<String, String> labels);

    void addLabels(Map<String, String> labels);

    void addLabel(String key, String value);

    Set<String> getVolumes();

    void setVolumes(Set<String> volumes);

    void addVolumes(Set<String> volumes);

    void addVolume(String value);

    Set<String> getExposedPorts();

    void setExposedPorts(Set<String> exposedPorts);

    void addExposedPorts(Set<String> exposedPorts);

    void addExposedPort(String value);

    Set<? extends Registry> getRegistries();

    void setRegistries(Set<? extends Registry> registries);

    void addRegistry(Registry registry);

    final class Registry extends AbstractModelObject<Registry> implements Domain, Comparable<Registry> {
        private static final long serialVersionUID = -3827873114784273276L;

        private String name;
        private String server;
        private String username;
        private String toUsername;
        private String fromUsername;
        private String password;
        private String toPassword;
        private String fromPassword;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.JibConfiguration.Registry immutable = new org.jreleaser.model.api.packagers.JibConfiguration.Registry() {
            private static final long serialVersionUID = -7511037078052130015L;

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getServer() {
                return server;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getToUsername() {
                return toUsername;
            }

            @Override
            public String getFromUsername() {
                return fromUsername;
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public String getToPassword() {
                return toPassword;
            }

            @Override
            public String getFromPassword() {
                return fromPassword;
            }

            @Override
            public int compareTo(org.jreleaser.model.api.packagers.JibConfiguration.Registry o) {
                if (null == o) return -1;
                return name.compareTo(o.getName());
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(JibConfiguration.Registry.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.packagers.JibConfiguration.Registry asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Registry source) {
            this.name = merge(this.name, source.name);
            this.server = merge(this.server, source.server);
            this.username = merge(this.username, source.username);
            this.toUsername = merge(this.toUsername, source.toUsername);
            this.fromUsername = merge(this.fromUsername, source.fromUsername);
            this.password = merge(this.password, source.password);
            this.toPassword = merge(this.toPassword, source.toPassword);
            this.fromPassword = merge(this.fromPassword, source.fromPassword);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getToUsername() {
            return toUsername;
        }

        public void setToUsername(String toUsername) {
            this.toUsername = toUsername;
        }

        public String getFromUsername() {
            return fromUsername;
        }

        public void setFromUsername(String fromUsername) {
            this.fromUsername = fromUsername;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getToPassword() {
            return toPassword;
        }

        public void setToPassword(String toPassword) {
            this.toPassword = toPassword;
        }

        public String getFromPassword() {
            return fromPassword;
        }

        public void setFromPassword(String fromPassword) {
            this.fromPassword = fromPassword;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", name);
            map.put("server", server);
            map.put("username", isNotBlank(username) ? HIDE : UNSET);
            map.put("toUsername", isNotBlank(toUsername) ? HIDE : UNSET);
            map.put("fromUsername", isNotBlank(fromUsername) ? HIDE : UNSET);
            map.put("password", isNotBlank(password) ? HIDE : UNSET);
            map.put("toPassword", isNotBlank(toPassword) ? HIDE : UNSET);
            map.put("fromPassword", isNotBlank(fromPassword) ? HIDE : UNSET);
            return map;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (null == o || getClass() != o.getClass()) return false;
            Registry that = (Registry) o;
            return server.equals(that.server);
        }

        @Override
        public int hashCode() {
            return Objects.hash(server);
        }

        @Override
        public int compareTo(Registry o) {
            if (null == o) return -1;
            return server.compareTo(o.server);
        }
    }
}
