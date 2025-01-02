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
package org.jreleaser.engine.deploy.maven;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.maven.MavenDeployer;
import org.jreleaser.model.spi.deploy.maven.MavenDeployerFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class ArtifactDeployers {
    private ArtifactDeployers() {
        // noop
    }

    public static <A extends org.jreleaser.model.api.deploy.maven.MavenDeployer, D extends org.jreleaser.model.internal.deploy.maven.MavenDeployer<A>> MavenDeployer<A, D> findMavenDeployer(JReleaserContext context, D deployer) {
        Map<String, MavenDeployer<?, ?>> deployers = StreamSupport.stream(ServiceLoader.load(MavenDeployerFactory.class,
                ArtifactDeployers.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(MavenDeployerFactory::getName, factory -> factory.getMavenDeployer(context)));

        if (deployers.containsKey(deployer.getType())) {
            MavenDeployer artifactMavenDeployer = deployers.get(deployer.getType());
            artifactMavenDeployer.setDeployer(deployer);
            return artifactMavenDeployer;
        }

        throw new JReleaserException(RB.$("ERROR_unsupported_deployer", deployer.getType()));
    }
}
