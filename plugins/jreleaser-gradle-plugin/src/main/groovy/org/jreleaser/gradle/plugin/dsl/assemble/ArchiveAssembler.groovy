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
package org.jreleaser.gradle.plugin.dsl.assemble

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.catalog.swid.SwidTagAware
import org.jreleaser.gradle.plugin.dsl.common.ArchiveOptions
import org.jreleaser.gradle.plugin.dsl.common.Matrix
import org.jreleaser.model.Archive.Format
import org.jreleaser.model.Distribution.DistributionType

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
interface ArchiveAssembler extends Assembler, SwidTagAware {
    Property<String> getArchiveName()

    Property<DistributionType> getDistributionType()

    void setDistributionType(String distributionType)

    Property<Boolean> getApplyDefaultMatrix()

    Property<Boolean> getAttachPlatform()

    SetProperty<Format> getFormats()

    void format(String format)

    void options(Action<? super ArchiveOptions> action)

    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArchiveOptions) Closure<Void> action)

    void matrix(Action<? super Matrix> action)

    void matrix(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Matrix) Closure<Void> action)
}