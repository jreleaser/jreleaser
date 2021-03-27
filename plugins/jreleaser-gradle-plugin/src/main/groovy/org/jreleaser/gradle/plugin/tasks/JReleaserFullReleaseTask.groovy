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
package org.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskAction
import org.jreleaser.model.JReleaserContext

import javax.inject.Inject

import static org.jreleaser.gradle.plugin.tasks.JReleaserAnnounceTask.announce
import static org.jreleaser.gradle.plugin.tasks.JReleaserChecksumTask.checksum
import static org.jreleaser.gradle.plugin.tasks.JReleaserPackageTask.packageTools
import static org.jreleaser.gradle.plugin.tasks.JReleaserPrepareTask.prepare
import static org.jreleaser.gradle.plugin.tasks.JReleaserReleaseTask.release
import static org.jreleaser.gradle.plugin.tasks.JReleaserSignTask.sign
import static org.jreleaser.gradle.plugin.tasks.JReleaserUploadTask.upload

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserFullReleaseTask extends AbstractJReleaserTask {
    @Inject
    JReleaserFullReleaseTask(ObjectFactory objects) {
        super(objects)
    }

    @TaskAction
    void fullRelease() {
        JReleaserContext ctx = context.get()
        println "jreleaser.dryrun set to ${ctx.dryrun}"

        checksum(ctx)
        sign(ctx)
        release(ctx)
        prepare(ctx, true)
        packageTools(ctx, true)
        upload(ctx, true)
        announce(ctx)
    }
}
