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
package org.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import org.jreleaser.model.internal.JReleaserContext

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractJReleaserPackagerTask extends AbstractJReleaserDistributionTask {
    @Input
    @Optional
    final ListProperty<String> packagers

    @Input
    @Optional
    final ListProperty<String> excludedPackagers

    @Inject
    AbstractJReleaserPackagerTask(ObjectFactory objects) {
        super(objects)
        packagers = objects.listProperty(String).convention([])
        excludedPackagers = objects.listProperty(String).convention([])
    }

    @Option(option = 'packager', description = 'Include a packager (OPTIONAL).')
    void setPackager(List<String> packagers) {
        this.packagers.set(packagers)
    }

    @Option(option = 'exclude-packager', description = 'Exclude a packager (OPTIONAL).')
    void setExcludePackager(List<String> excludedPackagers) {
        this.excludedPackagers.set(excludedPackagers)
    }

    protected JReleaserContext setupContext() {
        JReleaserContext ctx = super.setupContext()
        ctx.includedPackagers = packagers.orNull
        ctx.excludedPackagers = excludedPackagers.orNull
        ctx
    }
}
