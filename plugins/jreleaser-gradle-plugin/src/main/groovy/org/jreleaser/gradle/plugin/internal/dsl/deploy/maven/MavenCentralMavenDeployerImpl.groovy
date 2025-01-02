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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.deploy.maven.MavenCentralMavenDeployer

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.12.0
 */
@CompileStatic
class MavenCentralMavenDeployerImpl extends AbstractMavenDeployer implements MavenCentralMavenDeployer {
    final Property<org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage> stage
    final Property<String> namespace
    final Property<String> deploymentId
    final Property<String> verifyUrl
    final Property<Integer> retryDelay
    final Property<Integer> maxRetries

    @Inject
    MavenCentralMavenDeployerImpl(ObjectFactory objects) {
        super(objects)
        stage = objects.property(org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage).convention(Providers.<org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage> notDefined())
        namespace = objects.property(String).convention(Providers.<String> notDefined())
        deploymentId = objects.property(String).convention(Providers.<String> notDefined())
        verifyUrl = objects.property(String).convention(Providers.<String> notDefined())
        retryDelay = objects.property(Integer).convention(Providers.<Integer> notDefined())
        maxRetries = objects.property(Integer).convention(Providers.<Integer> notDefined())
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            stage.present ||
            namespace.present ||
            deploymentId.present ||
            verifyUrl.present ||
            retryDelay.present ||
            maxRetries.present
    }

    @Override
    void setStage(String str) {
        if (isNotBlank(str)) {
            stage.set(org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage.of(str.trim()))
        }
    }

    org.jreleaser.model.internal.deploy.maven.MavenCentralMavenDeployer toModel() {
        org.jreleaser.model.internal.deploy.maven.MavenCentralMavenDeployer deployer = new org.jreleaser.model.internal.deploy.maven.MavenCentralMavenDeployer()
        fillProperties(deployer)
        if (stage.present) deployer.stage = stage.get()
        if (namespace.present) deployer.namespace = namespace.get()
        if (deploymentId.present) deployer.deploymentId = deploymentId.get()
        if (verifyUrl.present) deployer.verifyUrl = verifyUrl.get()
        if (retryDelay.present) deployer.retryDelay = retryDelay.get()
        if (maxRetries.present) deployer.maxRetries = maxRetries.get()
        deployer
    }
}
