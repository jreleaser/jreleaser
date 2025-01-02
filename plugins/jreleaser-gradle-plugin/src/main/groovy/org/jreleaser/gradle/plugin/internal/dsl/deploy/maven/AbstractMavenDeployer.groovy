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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.deploy.maven.MavenDeployer
import org.jreleaser.model.Active
import org.jreleaser.model.Http
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
abstract class AbstractMavenDeployer implements MavenDeployer {
    String name
    final Property<Active> active
    final Property<Integer> connectTimeout
    final Property<Integer> readTimeout
    final Property<Boolean> sign
    final Property<Boolean> checksums
    final Property<Boolean> sourceJar
    final Property<Boolean> javadocJar
    final Property<Boolean> verifyPom
    final Property<Boolean> applyMavenCentralRules
    final Property<String> url
    final Property<String> username
    final Property<String> password
    final Property<Boolean> snapshotSupported
    final Property<Http.Authorization> authorization
    final ListProperty<String> stagingRepositories
    final MapProperty<String, Object> extraProperties

    private final NamedDomainObjectContainer<ArtifactOverrideImpl> artifactOverrides

    @Inject
    AbstractMavenDeployer(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        connectTimeout = objects.property(Integer).convention(Providers.<Integer> notDefined())
        readTimeout = objects.property(Integer).convention(Providers.<Integer> notDefined())
        sign = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        checksums = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        sourceJar = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        javadocJar = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        verifyPom = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        applyMavenCentralRules = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        url = objects.property(String).convention(Providers.<String> notDefined())
        username = objects.property(String).convention(Providers.<String> notDefined())
        password = objects.property(String).convention(Providers.<String> notDefined())
        authorization = objects.property(Http.Authorization).convention(Providers.<Http.Authorization> notDefined())
        stagingRepositories = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
        snapshotSupported = objects.property(Boolean).convention(false)
        artifactOverrides = objects.domainObjectContainer(ArtifactOverrideImpl, new NamedDomainObjectFactory<ArtifactOverrideImpl>() {
            @Override
            ArtifactOverrideImpl create(String name) {
                ArtifactOverrideImpl artifact = objects.newInstance(ArtifactOverrideImpl, objects)
                artifact.name = name
                artifact
            }
        })
    }

    @Internal
    boolean isSet() {
        active.present ||
            connectTimeout.present ||
            readTimeout.present ||
            extraProperties.present ||
            sign.present ||
            checksums.present ||
            sourceJar.present ||
            javadocJar.present ||
            verifyPom.present ||
            applyMavenCentralRules.present ||
            url.present ||
            username.present ||
            password.present ||
            authorization.present ||
            stagingRepositories.present ||
            !artifactOverrides.isEmpty()
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void setAuthorization(String authorization) {
        if (isNotBlank(authorization)) {
            this.authorization.set(Http.Authorization.of(authorization))
        }
    }

    @Override
    @CompileDynamic
    void stagingRepository(String str) {
        if (isNotBlank(str)) {
            stagingRepositories.add(str.trim())
        }
    }

    @Override
    void artifactOverride(Action<? super ArtifactOverride> action) {
        action.execute(artifactOverrides.maybeCreate("artifact-${artifactOverrides.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void artifactOverride(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArtifactOverride) Closure<Void> action) {
        ConfigureUtil.configure(action, artifactOverrides.maybeCreate("artifact-${artifactOverrides.size()}".toString()))
    }

    protected <D extends org.jreleaser.model.internal.deploy.maven.MavenDeployer> void fillProperties(D deployer) {
        deployer.name = name
        if (active.present) deployer.active = active.get()
        if (connectTimeout.present) deployer.connectTimeout = connectTimeout.get()
        if (readTimeout.present) deployer.readTimeout = readTimeout.get()
        if (extraProperties.present) deployer.extraProperties.putAll(extraProperties.get())
        if (sign.present) deployer.sign = sign.get()
        if (checksums.present) deployer.checksums = checksums.get()
        if (sourceJar.present) deployer.sourceJar = sourceJar.get()
        if (javadocJar.present) deployer.javadocJar = javadocJar.get()
        if (verifyPom.present) deployer.verifyPom = verifyPom.get()
        if (snapshotSupported.present) deployer.snapshotSupported = snapshotSupported.get()
        if (applyMavenCentralRules.present) deployer.applyMavenCentralRules = applyMavenCentralRules.get()
        if (url.present) deployer.url = url.get()
        if (username.present) deployer.username = username.get()
        if (password.present) deployer.password = password.get()
        if (authorization.present) deployer.authorization = authorization.get()
        deployer.stagingRepositories = (List<String>) stagingRepositories.getOrElse([])
        for (ArtifactOverrideImpl artifact : artifactOverrides) {
            deployer.addArtifactOverride(artifact.toModel())
        }
    }

    static class ArtifactOverrideImpl implements ArtifactOverride {
        String name
        final Property<String> groupId
        final Property<String> artifactId
        final Property<Boolean> jar
        final Property<Boolean> sourceJar
        final Property<Boolean> javadocJar
        final Property<Boolean> verifyPom

        @Inject
        ArtifactOverrideImpl(ObjectFactory objects) {
            groupId = objects.property(String).convention(Providers.<String> notDefined())
            artifactId = objects.property(String).convention(Providers.<String> notDefined())
            jar = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            sourceJar = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            javadocJar = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            verifyPom = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        }

        org.jreleaser.model.internal.deploy.maven.MavenDeployer.ArtifactOverride toModel() {
            org.jreleaser.model.internal.deploy.maven.MavenDeployer.ArtifactOverride artifact = new org.jreleaser.model.internal.deploy.maven.MavenDeployer.ArtifactOverride()
            if (groupId.present) artifact.groupId = groupId.get()
            if (artifactId.present) artifact.artifactId = artifactId.get()
            if (jar.present) artifact.jar = jar.get()
            if (sourceJar.present) artifact.sourceJar = sourceJar.get()
            if (javadocJar.present) artifact.javadocJar = javadocJar.get()
            if (verifyPom.present) artifact.verifyPom = verifyPom.get()
            artifact
        }
    }
}
