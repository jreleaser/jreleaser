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
 * @since 0.2.0
 */
@CompileStatic
abstract class JReleaserAssembleTask extends AbstractJReleaserTask {
    @Input
    @Optional
    final Property<String> distributionName

    @Input
    @Optional
    final Property<String> assemblerName

    @Option(option = 'distribution-name', description = 'The name of the distribution (OPTIONAL).')
    void setDistributionName(String distributionName) {
        this.distributionName.set(distributionName)
    }

    @Option(option = 'assembler-name', description = 'The name of the assembler (OPTIONAL).')
    void setAssemblerName(String assemblerName) {
        this.assemblerName.set(assemblerName)
    }

    @Inject
    JReleaserAssembleTask(ObjectFactory objects) {
        super(objects)
        distributionName = objects.property(String).convention(Providers.notDefined())
        assemblerName = objects.property(String).convention(Providers.notDefined())
    }

    @TaskAction
    void performAction() {
        Workflows.assemble(setupContext()).execute()
    }

    protected JReleaserContext setupContext() {
        mode = JReleaserContext.Mode.ASSEMBLE
        JReleaserContext ctx = createContext()
        ctx.distributionName = distributionName.orNull
        ctx.assemblerName = assemblerName.orNull
        ctx
    }
}
