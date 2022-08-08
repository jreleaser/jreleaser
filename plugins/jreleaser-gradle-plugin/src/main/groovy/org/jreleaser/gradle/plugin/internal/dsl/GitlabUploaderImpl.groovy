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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.GitlabUploader

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
class GitlabUploaderImpl extends AbstractUploader implements GitlabUploader {
    String name
    final Property<String> host
    final Property<String> token
    final Property<String> packageName
    final Property<String> packageVersion
    final Property<String> projectIdentifier

    @Inject
    GitlabUploaderImpl(ObjectFactory objects) {
        super(objects)
        host = objects.property(String).convention(Providers.notDefined())
        token = objects.property(String).convention(Providers.notDefined())
        packageName = objects.property(String).convention(Providers.notDefined())
        packageVersion = objects.property(String).convention(Providers.notDefined())
        projectIdentifier = objects.property(String).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            host.present ||
            token.present ||
            packageName.present ||
            packageVersion.present ||
            projectIdentifier.present
    }

    org.jreleaser.model.GitlabUploader toModel() {
        org.jreleaser.model.GitlabUploader gitlab = new org.jreleaser.model.GitlabUploader()
        gitlab.name = name
        fillProperties(gitlab)
        if (host.present) gitlab.host = host.get()
        if (token.present) gitlab.token = token.get()
        if (packageName.present) gitlab.packageName = packageName.get()
        if (packageVersion.present) gitlab.packageVersion = packageVersion.get()
        if (projectIdentifier.present) gitlab.projectIdentifier = projectIdentifier.get()
        gitlab
    }
}
