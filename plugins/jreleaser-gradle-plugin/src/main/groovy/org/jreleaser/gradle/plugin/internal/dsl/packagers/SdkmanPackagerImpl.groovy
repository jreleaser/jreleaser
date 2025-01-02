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
package org.jreleaser.gradle.plugin.internal.dsl.packagers

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.packagers.SdkmanPackager
import org.jreleaser.model.Sdkman

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.6.0
 */
@CompileStatic
class SdkmanPackagerImpl extends AbstractPackager implements SdkmanPackager {
    final Property<String> consumerKey
    final Property<String> consumerToken
    final Property<String> candidate
    final Property<String> releaseNotesUrl
    final Property<Integer> connectTimeout
    final Property<Integer> readTimeout
    final Property<Sdkman.Command> command

    @Inject
    SdkmanPackagerImpl(ObjectFactory objects) {
        super(objects)
        consumerKey = objects.property(String).convention(Providers.<String> notDefined())
        consumerToken = objects.property(String).convention(Providers.<String> notDefined())
        candidate = objects.property(String).convention(Providers.<String> notDefined())
        releaseNotesUrl = objects.property(String).convention(Providers.<String> notDefined())
        connectTimeout = objects.property(Integer).convention(Providers.<Integer> notDefined())
        readTimeout = objects.property(Integer).convention(Providers.<Integer> notDefined())
        command = objects.property(Sdkman.Command).convention(Providers.<Sdkman.Command> notDefined())
    }

    @Override
    void setCommand(String str) {
        if (isNotBlank(str)) {
            command.set(Sdkman.Command.of(str.trim()))
        }
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            consumerKey.present ||
            consumerToken.present ||
            candidate.present ||
            releaseNotesUrl.present ||
            connectTimeout.present ||
            readTimeout.present ||
            command.present
    }

    org.jreleaser.model.internal.packagers.SdkmanPackager toModel() {
        org.jreleaser.model.internal.packagers.SdkmanPackager sdkman = new org.jreleaser.model.internal.packagers.SdkmanPackager()
        fillPackagerProperties(sdkman)
        if (consumerKey.present) sdkman.consumerKey = consumerKey.get()
        if (consumerToken.present) sdkman.consumerToken = consumerToken.get()
        if (candidate.present) sdkman.candidate = candidate.get()
        if (releaseNotesUrl.present) sdkman.releaseNotesUrl = releaseNotesUrl.get()
        if (command.present) sdkman.command = command.get()
        sdkman
    }
}
