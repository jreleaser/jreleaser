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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
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
            graal.isSet()
    }

    @Override
    void addArg(String arg) {
        if (isNotBlank(arg)) {
            args.add(arg.trim())
        }
    }

    @Override
    void graal(Action<? super Artifact> action) {
        action.execute(graal)
    }

    @Override
    void graal(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, graal)
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
        nativeImage
    }
}
