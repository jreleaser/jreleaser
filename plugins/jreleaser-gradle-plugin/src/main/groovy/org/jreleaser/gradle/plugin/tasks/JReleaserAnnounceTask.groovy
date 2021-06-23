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
package org.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.model.JReleaserContext
import org.jreleaser.workflow.Workflows

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserAnnounceTask extends AbstractJReleaserTask {
    @Input
    @Optional
    final Property<String> announcerName

    @Inject
    JReleaserAnnounceTask(ObjectFactory objects) {
        super(objects)
        announcerName = objects.property(String).convention(Providers.notDefined())
    }

    @Option(option = 'announcer-name', description = 'The name of the announcer (OPTIONAL).')
    void setAnnouncerName(String toolName) {
        this.announcerName.set(toolName)
    }

    @TaskAction
    void performAction() {
        JReleaserContext ctx = createContext()
        ctx.distributionName = announcerName.orNull
        Workflows.announce(ctx).execute()
    }
}
