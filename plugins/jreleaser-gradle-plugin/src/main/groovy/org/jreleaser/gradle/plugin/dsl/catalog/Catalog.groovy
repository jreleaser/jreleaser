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
package org.jreleaser.gradle.plugin.dsl.catalog

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.jreleaser.gradle.plugin.dsl.catalog.sbom.Sbom
import org.jreleaser.gradle.plugin.dsl.catalog.swid.SwidTag
import org.jreleaser.gradle.plugin.dsl.common.Activatable

/**
 *
 * @author Andres Almiray
 * @since 1.5.0
 */
@CompileStatic
interface Catalog extends Activatable {
    Sbom getSbom()

    GithubCataloger getGithub()

    SlsaCataloger getSlsa()

    NamedDomainObjectContainer<SwidTag> getSwid()

    void sbom(Action<? super Sbom> action)

    void github(Action<? super GithubCataloger> action)

    void slsa(Action<? super SlsaCataloger> action)

    void swid(Action<? super NamedDomainObjectContainer<SwidTag>> action)

    void sbom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Sbom) Closure<Void> action)

    void github(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GithubCataloger) Closure<Void> action)

    void slsa(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SlsaCataloger) Closure<Void> action)

    void swid(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<SwidTag>) Closure<Void> action)
}