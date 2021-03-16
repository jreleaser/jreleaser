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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.model.Distribution
import org.jreleaser.model.JReleaserContext
import org.jreleaser.model.JReleaserException
import org.jreleaser.tools.DistributionProcessor
import org.jreleaser.model.tool.spi.ToolProcessingException

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserPackageTask extends AbstractJReleaserTask {
    @Input
    final Property<Boolean> failFast

    @Option(option = 'fail-fast', description = 'Stops on first error (OPTIONAL).')
    void setFailFast(boolean failFast) {
        this.failFast.set(failFast)
    }

    @Inject
    JReleaserPackageTask(ObjectFactory objects) {
        super(objects)
        failFast = objects.property(Boolean).convention(false)
    }

    @TaskAction
    void packageTool() {
        JReleaserContext context = createContext()

        List<Exception> exceptions = new ArrayList<>()
        for (Distribution distribution : context.model.distributions.values()) {
            for (String toolName : Distribution.supportedTools()) {
                try {
                    DistributionProcessor processor = createDistributionProcessor(context,
                        distribution,
                        toolName);

                    if (processor.packageDistribution()) {
                        context.logger.info('Packaged ' + processor.distributionName +
                            ' distribution with ' + processor.toolName)
                    }
                } catch (JReleaserException | ToolProcessingException e) {
                    if (failFast) throw new IllegalStateException('Unexpected error', e)
                    exceptions.add(e)
                    logger.warn('Unexpected error', e)
                }
            }
        }

        if (!exceptions.isEmpty()) {
            throw new IllegalStateException('There were ' + exceptions.size() + ' failure(s)')
        }
    }

    private DistributionProcessor createDistributionProcessor(JReleaserContext context,
                                                              Distribution distribution,
                                                              String toolName) {
        return DistributionProcessor.builder()
            .context(context)
            .distributionName(distribution.getName())
            .toolName(toolName)
            .build()
    }
}
