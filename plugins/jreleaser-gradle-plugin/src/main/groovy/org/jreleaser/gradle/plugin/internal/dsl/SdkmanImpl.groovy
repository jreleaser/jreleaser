/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.jreleaser.gradle.plugin.dsl.Sdkman

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class SdkmanImpl extends AbstractAnnouncer implements Sdkman {
    final Property<String> consumerKey
    final Property<String> consumerToken
    final Property<String> candidate
    final Property<Boolean> major

    @Inject
    SdkmanImpl(ObjectFactory objects) {
        super(objects)
        consumerKey = objects.property(String).convention(Providers.notDefined())
        consumerToken = objects.property(String).convention(Providers.notDefined())
        candidate = objects.property(String).convention(Providers.notDefined())
        major = objects.property(Boolean).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        return super.isSet() ||
            consumerKey.present ||
            consumerToken.present ||
            candidate.present ||
            major.present
    }

    org.jreleaser.model.Sdkman toModel() {
        org.jreleaser.model.Sdkman sdkman = new org.jreleaser.model.Sdkman()
        sdkman.enabled = enabled.getOrElse(isSet())
        sdkman.consumerKey = consumerKey.orNull
        sdkman.consumerToken = consumerToken.orNull
        sdkman.candidate = candidate.orNull
        sdkman.major = major.orElse(true)
        sdkman
    }
}
