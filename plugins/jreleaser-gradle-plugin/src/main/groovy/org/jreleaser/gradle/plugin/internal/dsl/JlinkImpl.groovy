/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.dsl.Jlink
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JlinkImpl extends AbstractJavaAssembler implements Jlink {
    String name
    final Property<String> imageName
    final Property<String> imageNameTransform
    final Property<Boolean> copyJars
    final ListProperty<String> args
    final SetProperty<String> moduleNames
    final SetProperty<String> additionalModuleNames
    final JavaImpl java
    final PlatformImpl platform

    private final JdepsImpl jdeps
    private final ArtifactImpl jdk
    final NamedDomainObjectContainer<ArtifactImpl> targetJdks

    @Inject
    JlinkImpl(ObjectFactory objects) {
        super(objects)

        imageName = objects.property(String).convention(Providers.notDefined())
        imageNameTransform = objects.property(String).convention(Providers.notDefined())
        copyJars = objects.property(Boolean).convention(Providers.notDefined())
        args = objects.listProperty(String).convention(Providers.notDefined())
        moduleNames = objects.setProperty(String).convention(Providers.notDefined())
        additionalModuleNames = objects.setProperty(String).convention(Providers.notDefined())
        java = objects.newInstance(JavaImpl, objects)
        platform = objects.newInstance(PlatformImpl, objects)
        jdeps = objects.newInstance(JdepsImpl, objects)
        jdk = objects.newInstance(ArtifactImpl, objects)
        jdk.setName('jdk')

        targetJdks = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
            }
        })
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            imageName.present ||
            imageNameTransform.present ||
            copyJars.present ||
            args.present ||
            java.isSet() ||
            jdeps.isSet() ||
            jdk.isSet() ||
            moduleNames.present ||
            additionalModuleNames.present ||
            !targetJdks.isEmpty() ||
            platform.isSet()
    }

    @Override
    void arg(String arg) {
        if (isNotBlank(arg)) {
            args.add(arg.trim())
        }
    }

    @Override
    void jdeps(Action<? super Jdeps> action) {
        action.execute(jdeps)
    }

    @Override
    void jdk(Action<? super Artifact> action) {
        action.execute(jdk)
    }

    @Override
    void targetJdk(Action<? super Artifact> action) {
        action.execute(targetJdks.maybeCreate("targetJdk-${targetJdks.size()}".toString()))
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void jdeps(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jdeps) Closure<Void> action) {
        ConfigureUtil.configure(action, jdeps)
    }

    @Override
    void jdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, jdk)
    }

    @Override
    void targetJdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, targetJdks.maybeCreate("targetJdk-${targetJdks.size()}".toString()))
    }

    org.jreleaser.model.Jlink toModel() {
        org.jreleaser.model.Jlink jlink = new org.jreleaser.model.Jlink()
        jlink.name = name
        fillProperties(jlink)
        jlink.args = (List<String>) args.getOrElse([])
        if (jdeps.isSet()) jlink.jdeps = jdeps.toModel()
        if (jdk.isSet()) jlink.jdk = jdk.toModel()
        jlink.java = java.toModel()
        jlink.platform = platform.toModel()
        if (imageName.present) jlink.imageName = imageName.get()
        if (imageNameTransform.present) jlink.imageNameTransform = imageNameTransform.get()
        if (copyJars.present) jlink.copyJars = copyJars.get()
        jlink.moduleNames = (Set<String>) moduleNames.getOrElse([] as Set)
        jlink.additionalModuleNames = (Set<String>) additionalModuleNames.getOrElse([] as Set)
        for (ArtifactImpl artifact : targetJdks) {
            jlink.addTargetJdk(artifact.toModel())
        }
        jlink
    }

    @CompileStatic
    static class JdepsImpl implements Jdeps {
        final Property<Boolean> enabled
        final Property<String> multiRelease
        final Property<Boolean> ignoreMissingDeps
        final Property<Boolean> useWildcardInPath
        final SetProperty<String> targets

        @Inject
        JdepsImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(Providers.notDefined())
            multiRelease = objects.property(String).convention(Providers.notDefined())
            ignoreMissingDeps = objects.property(Boolean).convention(Providers.notDefined())
            useWildcardInPath = objects.property(Boolean).convention(Providers.notDefined())
            targets = objects.setProperty(String).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            multiRelease.present ||
                ignoreMissingDeps.present ||
                useWildcardInPath.present ||
                targets.present
        }

        org.jreleaser.model.Jlink.Jdeps toModel() {
            org.jreleaser.model.Jlink.Jdeps jdeps = new org.jreleaser.model.Jlink.Jdeps()
            if (enabled.present) {
                jdeps.enabled = enabled.get()
            } else {
                jdeps.enabled = true
            }
            if (multiRelease.present) jdeps.multiRelease = multiRelease.get()
            if (ignoreMissingDeps.present) jdeps.ignoreMissingDeps = ignoreMissingDeps.get()
            if (useWildcardInPath.present) jdeps.useWildcardInPath = useWildcardInPath.get()
            jdeps.targets = (Set<String>) targets.getOrElse([] as Set)
            jdeps
        }
    }
}
