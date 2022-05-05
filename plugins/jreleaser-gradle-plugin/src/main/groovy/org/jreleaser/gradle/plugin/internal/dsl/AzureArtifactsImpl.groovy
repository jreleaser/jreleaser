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
import javax.inject.Inject
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.AzureArtifacts

/**
 *
 * @author JIHUN KIM
 * @since 1.1.0
 */
@CompileStatic
class AzureArtifactsImpl extends AbstractUploader implements AzureArtifacts {
    String name
    final Property<String> host
    final Property<String> username
    final Property<String> personalAccessToken
    final Property<String> project
    final Property<String> organization
    final Property<String> feed
    final Property<String> path

    @Inject
    AzureArtifactsImpl(ObjectFactory objects) {
        super(objects)
        host = objects.property(String).convention(Providers.notDefined())
        username = objects.property(String).convention(Providers.notDefined())
        personalAccessToken = objects.property(String).convention(Providers.notDefined())
        project = objects.property(String).convention(Providers.notDefined())
        organization = objects.property(String).convention(Providers.notDefined())
        feed = objects.property(String).convention(Providers.notDefined())
        path = objects.property(String).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            host.present ||
            username.present ||
            personalAccessToken.present ||
            project.present ||
            organization.present ||
            feed.present ||
            path.present
    }

    org.jreleaser.model.AzureArtifacts toModel() {
        org.jreleaser.model.AzureArtifacts azureArtifacts = new org.jreleaser.model.AzureArtifacts()
        azureArtifacts.name = name
        fillProperties(azureArtifacts)
        if (host.present) azureArtifacts.host = host.get()
        if (username.present) azureArtifacts.username = username.get()
        if (personalAccessToken.present) azureArtifacts.personalAccessToken = personalAccessToken.get()
        if (project.present) azureArtifacts.project = project.get()
        if (organization.present) azureArtifacts.organization = organization.get()
        if (feed.present) azureArtifacts.feed = feed.get()
        if (path.present) azureArtifacts.path = path.get()
        azureArtifacts
    }
}
