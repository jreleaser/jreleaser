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
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.jreleaser.gradle.plugin.dsl.Sdkman
import org.jreleaser.gradle.plugin.dsl.Twitter
import org.jreleaser.gradle.plugin.dsl.Zulip
import org.jreleaser.model.Announce

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class AnnounceImpl implements org.jreleaser.gradle.plugin.dsl.Announce {
    final SdkmanImpl sdkman
    final TwitterImpl twitter
    final ZulipImpl zulip

    @Inject
    AnnounceImpl(ObjectFactory objects) {
        sdkman = objects.newInstance(SdkmanImpl, objects)
        twitter = objects.newInstance(TwitterImpl, objects)
        zulip = objects.newInstance(ZulipImpl, objects)
    }

    @Override
    void sdkman(Action<? super Sdkman> action) {
        action.execute(sdkman)
    }

    @Override
    void twitter(Action<? super Twitter> action) {
        action.execute(twitter)
    }

    @Override
    void zulip(Action<? super Zulip> action) {
        action.execute(zulip)
    }

    Announce toModel() {
        Announce announcers = new Announce()
        if (sdkman.isSet()) announcers.sdkman = sdkman.toModel()
        if (twitter.isSet()) announcers.twitter = twitter.toModel()
        if (zulip.isSet()) announcers.zulip = zulip.toModel()
        announcers
    }
}
