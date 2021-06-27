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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.Upload

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
class UploadImpl implements Upload {
    final Property<Boolean> enabled
    final NamedDomainObjectContainer<ArtifactoryImpl> artifactory
    final NamedDomainObjectContainer<HttpImpl> http

    @Inject
    UploadImpl(ObjectFactory objects) {
        enabled = objects.property(Boolean).convention(true)

        artifactory = objects.domainObjectContainer(ArtifactoryImpl, new NamedDomainObjectFactory<ArtifactoryImpl>() {
            @Override
            ArtifactoryImpl create(String name) {
                ArtifactoryImpl a = objects.newInstance(ArtifactoryImpl, objects)
                a.name = name
                return a
            }
        })

        http = objects.domainObjectContainer(HttpImpl, new NamedDomainObjectFactory<HttpImpl>() {
            @Override
            HttpImpl create(String name) {
                HttpImpl h = objects.newInstance(HttpImpl, objects)
                h.name = name
                return h
            }
        })
    }

    @CompileDynamic
    org.jreleaser.model.Upload toModel() {
        org.jreleaser.model.Upload upload = new org.jreleaser.model.Upload()

        artifactory.each { upload.addArtifactory(it.toModel()) }
        http.each { upload.addHttp(it.toModel()) }

        upload
    }
}
