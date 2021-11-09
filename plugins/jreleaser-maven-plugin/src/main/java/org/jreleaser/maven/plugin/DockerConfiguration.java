/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public interface DockerConfiguration extends ExtraProperties, Activatable {
    String getTemplateDirectory();

    void setTemplateDirectory(String templateDirectory);

    String getBaseImage();

    void setBaseImage(String baseImage);

    Map<String, String> getLabels();

    void setLabels(Map<String, String> labels);

    Set<String> getImageNames();

    void setImageNames(Set<String> imageNames);

    List<String> getBuildArgs();

    void setBuildArgs(List<String> buildArgs);

    List<String> getPreCommands();

    void setPreCommands(List<String> preCommands);

    List<String> getPostCommands();

    void setPostCommands(List<String> postCommands);

    Set<Registry> getRegistries();

    void setRegistries(Set<Registry> registries);

    void setUseLocalArtifact(Boolean useLocalArtifact);

    boolean isUseLocalArtifact();

    boolean isUseLocalArtifactSet();
}
