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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Twitter

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class TwitterImpl extends AbstractAnnouncer implements Twitter {
    final Property<String> consumerKey
    final Property<String> consumerSecret
    final Property<String> accessToken
    final Property<String> accessTokenSecret
    final Property<String> status
    final Property<String> statusTemplate
    final ListProperty<String> statuses

    @Inject
    TwitterImpl(ObjectFactory objects) {
        super(objects)
        consumerKey = objects.property(String).convention(Providers.notDefined())
        consumerSecret = objects.property(String).convention(Providers.notDefined())
        accessToken = objects.property(String).convention(Providers.notDefined())
        accessTokenSecret = objects.property(String).convention(Providers.notDefined())
        status = objects.property(String).convention(Providers.notDefined())
        statusTemplate = objects.property(String).convention(Providers.notDefined())
        statuses = objects.listProperty(String).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            consumerKey.present ||
            consumerSecret.present ||
            accessToken.present ||
            accessTokenSecret.present ||
            status.present ||
            statusTemplate.present ||
            statuses.present
    }

    @Override
    void status(String message) {
        if(isNotBlank(message)){
            statuses.add(message.trim())
        }
    }

    org.jreleaser.model.Twitter toModel() {
        org.jreleaser.model.Twitter twitter = new org.jreleaser.model.Twitter()
        fillProperties(twitter)
        if (consumerKey.present) twitter.consumerKey = consumerKey.get()
        if (consumerSecret.present) twitter.consumerSecret = consumerSecret.get()
        if (accessToken.present) twitter.accessToken = accessToken.get()
        if (accessTokenSecret.present) twitter.accessTokenSecret = accessTokenSecret.get()
        if (status.present) twitter.status = status.get()
        if (statusTemplate.present) twitter.statusTemplate = statusTemplate.get()
        twitter.statuses = (List<String>) statuses.getOrElse([])
        twitter
    }
}
