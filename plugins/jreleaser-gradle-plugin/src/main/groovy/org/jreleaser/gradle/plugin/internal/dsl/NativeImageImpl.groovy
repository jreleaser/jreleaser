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
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.dsl.NativeImage
import org.jreleaser.model.Active
import org.jreleaser.model.Archive
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.JReleaserOutput.nag
import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class NativeImageImpl extends AbstractJavaAssembler implements NativeImage {
    String name
    final Property<String> imageName
    final Property<String> imageNameTransform
    final Property<Archive.Format> archiveFormat
    final ListProperty<String> args
    final JavaImpl java
    final PlatformImpl platform

    private final ArtifactImpl graal
    private final UpxImpl upx
    final NamedDomainObjectContainer<ArtifactImpl> graalJdks

    @Inject
    NativeImageImpl(ObjectFactory objects) {
        super(objects)

        imageName = objects.property(String).convention(Providers.notDefined())
        imageNameTransform = objects.property(String).convention(Providers.notDefined())
        archiveFormat = objects.property(Archive.Format).convention(Archive.Format.ZIP)
        args = objects.listProperty(String).convention(Providers.notDefined())
        java = objects.newInstance(JavaImpl, objects)
        platform = objects.newInstance(PlatformImpl, objects)
        graal = objects.newInstance(ArtifactImpl, objects)
        graal.setName('graal')
        upx = objects.newInstance(UpxImpl, objects)

        graalJdks = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
            }
        })
    }

    @Override
    void setArchiveFormat(String str) {
        if (isNotBlank(str)) {
            this.archiveFormat.set(Archive.Format.of(str.trim()))
        }
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            imageName.present ||
            imageNameTransform.present ||
            args.present ||
            java.isSet() ||
            graal.isSet() ||
            upx.isSet() ||
            !graalJdks.isEmpty()
    }

    @Override
    @Deprecated
    void addArg(String arg) {
        nag('nativeImage.addArg() has been deprecated since 1.0.0-M2 and will be removed in the future. Use nativeImage.arg() instead')
        this.arg(arg)
    }

    @Override
    void arg(String arg) {
        if (isNotBlank(arg)) {
            args.add(arg.trim())
        }
    }

    @Override
    void graal(Action<? super Artifact> action) {
        action.execute(graal)
    }

    @Override
    void upx(Action<? super Upx> action) {
        action.execute(upx)
    }

    @Override
    void graalJdk(Action<? super Artifact> action) {
        action.execute(graalJdks.maybeCreate("graalJdk-${graalJdks.size()}".toString()))
    }

    @Override
    void graal(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, graal)
    }

    @Override
    void upx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, upx)
    }

    @Override
    void graalJdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, graalJdks.maybeCreate("graalJdk-${graalJdks.size()}".toString()))
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    org.jreleaser.model.NativeImage toModel() {
        org.jreleaser.model.NativeImage nativeImage = new org.jreleaser.model.NativeImage()
        nativeImage.name = name
        fillProperties(nativeImage)
        nativeImage.java = java.toModel()
        nativeImage.platform = platform.toModel()
        if (imageName.present) nativeImage.imageName = imageName.get()
        if (imageNameTransform.present) nativeImage.imageNameTransform = imageNameTransform.get()
        nativeImage.archiveFormat = archiveFormat.get()
        nativeImage.args = (List<String>) args.getOrElse([])
        if (graal.isSet()) nativeImage.graal = graal.toModel()
        if (upx.isSet()) nativeImage.upx = upx.toModel()
        for (ArtifactImpl artifact : graalJdks) {
            nativeImage.addGraalJdk(artifact.toModel())
        }
        nativeImage
    }

    @CompileStatic
    static class UpxImpl implements NativeImage.Upx {
        final Property<Active> active
        final Property<String> version
        final ListProperty<String> args

        @Inject
        UpxImpl(ObjectFactory objects) {
            active = objects.property(Active).convention(Providers.notDefined())
            version = objects.property(String).convention(Providers.notDefined())
            args = objects.listProperty(String).convention(Providers.notDefined())
        }

        @Override
        void setActive(String str) {
            if (isNotBlank(str)) {
                active.set(Active.of(str.trim()))
            }
        }

        @Override
        @Deprecated
        void addArg(String arg) {
            nag('upx.addArg() has been deprecated since 1.0.0-M2 and will be removed in the future. Use upx.arg() instead')
            this.arg(arg)
        }

        @Override
        void arg(String arg) {
            if (isNotBlank(arg)) {
                args.add(arg.trim())
            }
        }

        @Internal
        boolean isSet() {
            active.present ||
                version.present ||
                args.present
        }

        org.jreleaser.model.NativeImage.Upx toModel() {
            org.jreleaser.model.NativeImage.Upx upx = new org.jreleaser.model.NativeImage.Upx()
            if (active.present) upx.active = active.get()
            if (version.present) upx.version = version.get()
            upx.args = (List<String>) args.getOrElse([])
            upx
        }
    }
}
