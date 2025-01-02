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
package org.jreleaser.sdk.github;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.maven.MavenDeployerFactory;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
@ServiceProviderFor(MavenDeployerFactory.class)
public class GithubMavenDeployerFactory implements MavenDeployerFactory<org.jreleaser.model.api.deploy.maven.GithubMavenDeployer,
    org.jreleaser.model.internal.deploy.maven.GithubMavenDeployer, GithubMavenDeployer> {
    @Override
    public String getName() {
        return org.jreleaser.model.api.deploy.maven.GithubMavenDeployer.TYPE;
    }

    @Override
    public GithubMavenDeployer getMavenDeployer(JReleaserContext context) {
        return new GithubMavenDeployer(context);
    }
}