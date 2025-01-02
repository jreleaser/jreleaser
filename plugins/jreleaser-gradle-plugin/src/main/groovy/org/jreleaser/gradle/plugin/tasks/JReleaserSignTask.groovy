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
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.model.api.JReleaserCommand
import org.jreleaser.model.internal.JReleaserContext
import org.jreleaser.workflow.Workflows

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserSignTask extends AbstractPlatformAwareJReleaserTask {
    static final String NAME = 'jreleaserSign'

    @Input
    @Optional
    final ListProperty<String> distributions

    @Input
    @Optional
    final ListProperty<String> excludedDistributions

    @Inject
    JReleaserSignTask(ObjectFactory objects) {
        super(objects)
        distributions = objects.listProperty(String).convention([])
        excludedDistributions = objects.listProperty(String).convention([])
        command = JReleaserCommand.SIGN
    }

    @Option(option = 'distribution', description = 'Include a distribution (OPTIONAL).')
    void setDistribution(List<String> distributions) {
        this.distributions.set(distributions)
    }

    @Option(option = 'exclude-distribution', description = 'Exclude a distribution (OPTIONAL).')
    void setExcludeDistribution(List<String> excludedDistributions) {
        this.excludedDistributions.set(excludedDistributions)
    }

    @TaskAction
    void performAction() {
        Workflows.sign(setupContext()).execute()
    }

    protected JReleaserContext setupContext() {
        JReleaserContext ctx = createContext()
        ctx.includedDistributions = distributions.orNull
        ctx.excludedDistributions = excludedDistributions.orNull
        ctx
    }
}
