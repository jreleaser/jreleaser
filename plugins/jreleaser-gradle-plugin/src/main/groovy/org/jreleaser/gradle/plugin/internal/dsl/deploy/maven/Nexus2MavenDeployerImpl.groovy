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
import org.jreleaser.gradle.plugin.dsl.deploy.maven.Nexus2MavenDeployer

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
class Nexus2MavenDeployerImpl extends AbstractMavenDeployer implements Nexus2MavenDeployer {
    final Property<String> snapshotUrl
    final Property<String> verifyUrl
    final Property<Boolean> closeRepository
    final Property<Boolean> releaseRepository
    final Property<Integer> transitionDelay
    final Property<Integer> transitionMaxRetries
    final Property<String> stagingProfileId
    final Property<String> stagingRepositoryId
    final Property<org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage> startStage
    final Property<org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage> endStage

    @Inject
    Nexus2MavenDeployerImpl(ObjectFactory objects) {
        super(objects)
        snapshotUrl = objects.property(String).convention(Providers.<String> notDefined())
        verifyUrl = objects.property(String).convention(Providers.<String> notDefined())
        closeRepository = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        releaseRepository = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        transitionDelay = objects.property(Integer).convention(Providers.<Integer> notDefined())
        transitionMaxRetries = objects.property(Integer).convention(Providers.<Integer> notDefined())
        stagingProfileId = objects.property(String).convention(Providers.<String> notDefined())
        stagingRepositoryId = objects.property(String).convention(Providers.<String> notDefined())
        startStage = objects.property(org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage).convention(Providers.<org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage> notDefined())
        endStage = objects.property(org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage).convention(Providers.<org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage> notDefined())
    }

    @Override
    void setStartStage(String stage) {
        if (isNotBlank(stage)) {
            startStage.set(org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage.of(stage.trim()))
        }
    }

    @Override
    void setEndStage(String stage) {
        if (isNotBlank(stage)) {
            endStage.set(org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage.of(stage.trim()))
        }
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            snapshotUrl.present ||
            verifyUrl.present ||
            closeRepository.present ||
            releaseRepository.present ||
            transitionDelay.present ||
            transitionMaxRetries.present ||
            stagingProfileId.present ||
            stagingRepositoryId.present ||
            startStage.present ||
            endStage.present
    }

    org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer toModel() {
        org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer deployer = new org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer()
        fillProperties(deployer)
        if (snapshotUrl.present) deployer.snapshotUrl = snapshotUrl.get()
        if (verifyUrl.present) deployer.verifyUrl = verifyUrl.get()
        if (closeRepository.present) deployer.closeRepository = closeRepository.get()
        if (releaseRepository.present) deployer.releaseRepository = releaseRepository.get()
        if (transitionDelay.present) deployer.transitionDelay = transitionDelay.get()
        if (transitionMaxRetries.present) deployer.transitionMaxRetries = transitionMaxRetries.get()
        if (stagingProfileId.present) deployer.stagingProfileId = stagingProfileId.get()
        if (stagingRepositoryId.present) deployer.stagingRepositoryId = stagingRepositoryId.get()
        if (startStage.present) deployer.startStage = startStage.get()
        if (endStage.present) deployer.endStage = endStage.get()
        deployer
    }
}
