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
package org.jreleaser.gradle.plugin.internal.dsl.assemble

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.assemble.ArchiveAssembler
import org.jreleaser.gradle.plugin.dsl.common.ArchiveOptions
import org.jreleaser.gradle.plugin.internal.dsl.common.ArchiveOptionsImpl
import org.jreleaser.gradle.plugin.internal.dsl.platform.PlatformImpl
import org.jreleaser.model.Archive
import org.jreleaser.model.Distribution.DistributionType
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ArchiveAssemblerImpl extends AbstractAssembler implements ArchiveAssembler {
    String name
    final Property<String> archiveName
    final Property<DistributionType> distributionType
    final Property<Boolean> attachPlatform
    final SetProperty<Archive.Format> formats
    final PlatformImpl platform
    final ArchiveOptionsImpl options

    @Inject
    ArchiveAssemblerImpl(ObjectFactory objects) {
        super(objects)
        archiveName = objects.property(String).convention(Providers.<String> notDefined())
        distributionType = objects.property(DistributionType).convention(DistributionType.JAVA_BINARY)
        attachPlatform = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        formats = objects.setProperty(Archive.Format).convention(Providers.<Set<Archive.Format>> notDefined())
        platform = objects.newInstance(PlatformImpl, objects)
        options = objects.newInstance(ArchiveOptionsImpl, objects)
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            archiveName.present ||
            distributionType.present ||
            attachPlatform.present ||
            formats.present ||
            options.isSet()
    }

    @Override
    void setDistributionType(String distributionType) {
        this.distributionType.set(DistributionType.of(distributionType))
    }

    @Override
    void format(String format) {
        if (isNotBlank(format)) {
            formats.add(Archive.Format.of(format))
        }
    }

    @Override
    void options(Action<? super ArchiveOptions> action) {
        action.execute(options)
    }

    @Override
    @CompileDynamic
    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArchiveOptions) Closure<Void> action) {
        ConfigureUtil.configure(action, options)
    }

    org.jreleaser.model.internal.assemble.ArchiveAssembler toModel() {
        org.jreleaser.model.internal.assemble.ArchiveAssembler assembler = new org.jreleaser.model.internal.assemble.ArchiveAssembler()
        assembler.name = name
        fillProperties(assembler)
        if (archiveName.present) assembler.archiveName = archiveName.get()
        if (attachPlatform.present) assembler.attachPlatform = attachPlatform.get()
        assembler.platform = platform.toModel()
        assembler.distributionType = distributionType.get()
        assembler.formats = (Set<Archive.Format>) formats.getOrElse([] as Set<Archive.Format>)
        if (options.isSet()) assembler.options = options.toModel()
        assembler
    }
}
