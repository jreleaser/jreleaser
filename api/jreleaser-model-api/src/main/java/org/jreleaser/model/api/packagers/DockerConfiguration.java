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
package org.jreleaser.model.api.packagers;

import org.jreleaser.model.api.common.Activatable;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.ExtraProperties;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public interface DockerConfiguration extends Domain, ExtraProperties, Activatable {
    String TYPE = "docker";

    String getTemplateDirectory();

    List<String> getSkipTemplates();

    String getBaseImage();

    Map<String, String> getLabels();

    Set<String> getImageNames();

    List<String> getBuildArgs();

    List<String> getPreCommands();

    List<String> getPostCommands();

    Set<? extends Registry> getRegistries();

    boolean isUseLocalArtifact();

    Buildx getBuildx();

    interface Registry extends Domain, Comparable<Registry> {
        String getServer();

        String getServerName();

        String getRepositoryName();

        String getUsername();

        String getPassword();

        boolean isExternalLogin();
    }

    interface Buildx extends Domain {
        boolean isEnabled();

        boolean isCreateBuilder();

        List<String> getCreateBuilderFlags();

        List<String> getPlatforms();
    }
}
