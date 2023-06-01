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
package org.jreleaser.model.api.deploy.maven;

import org.jreleaser.model.api.common.Activatable;
import org.jreleaser.model.api.common.Domain;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public interface Maven extends Domain, Activatable {
    Pomchecker getPomchecker();

    Map<String, ? extends ArtifactoryMavenDeployer> getArtifactory();

    Map<String, ? extends AzureMavenDeployer> getAzure();

    Map<String, ? extends GiteaMavenDeployer> getGitea();

    Map<String, ? extends GithubMavenDeployer> getGithub();

    Map<String, ? extends GitlabMavenDeployer> getGitlab();

    Map<String, ? extends Nexus2MavenDeployer> getNexus2();

    interface Pomchecker extends Domain {
        String getVersion();
        boolean isFailOnWarning();
        boolean isFailOnError();
    }
}
