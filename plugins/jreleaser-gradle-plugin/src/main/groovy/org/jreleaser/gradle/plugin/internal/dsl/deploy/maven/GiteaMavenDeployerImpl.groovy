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
package org.jreleaser.gradle.plugin.internal.dsl.deploy.maven

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.jreleaser.gradle.plugin.dsl.deploy.maven.GiteaMavenDeployer

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
class GiteaMavenDeployerImpl extends AbstractMavenDeployer implements GiteaMavenDeployer {
    @Inject
    GiteaMavenDeployerImpl(ObjectFactory objects) {
        super(objects)
    }

    org.jreleaser.model.internal.deploy.maven.GiteaMavenDeployer toModel() {
        org.jreleaser.model.internal.deploy.maven.GiteaMavenDeployer deployer = new org.jreleaser.model.internal.deploy.maven.GiteaMavenDeployer()
        fillProperties(deployer)
        deployer
    }
}
