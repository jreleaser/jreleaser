/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
import org.gradle.api.tasks.options.Option
import org.jreleaser.model.Distribution
import org.jreleaser.model.JReleaserContext
import org.jreleaser.model.JReleaserException
import org.jreleaser.model.tool.spi.ToolProcessingException
import org.jreleaser.tools.DistributionProcessor

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractJReleaserProcessorTask extends AbstractJReleaserTask {
    @Input
    final Property<Boolean> failFast

    @Option(option = 'fail-fast', description = 'Stops on first error (OPTIONAL).')
    void setFailFast(boolean failFast) {
        this.failFast.set(failFast)
    }

    @Inject
    AbstractJReleaserProcessorTask(ObjectFactory objects) {
        super(objects)
        failFast = objects.property(Boolean).convention(false)
    }

    protected static void processContext(JReleaserContext context, boolean failFast, String action, ToolProcessingFunction function) {
        context.logger.info('{} distributions', action)
        List<Exception> exceptions = []
        for (Distribution distribution : context.model.distributions.values()) {
            for (String toolName : Distribution.supportedTools()) {
                try {
                    DistributionProcessor processor = createDistributionProcessor(context,
                        distribution,
                        toolName)

                    function.consume(processor)
                } catch (ToolProcessingException e) {
                    if (failFast) throw new JReleaserException('Unexpected error', e)
                    exceptions.add(e)
                    context.logger.warn('Unexpected error', e)
                } catch (JReleaserException e) {
                    if (failFast) throw e
                    exceptions.add(e)
                    context.logger.warn('Unexpected error', e)
                }
            }
        }

        if (!exceptions.isEmpty()) {
            throw new JReleaserException('There were ' + exceptions.size() + ' failure(s)' +
                System.lineSeparator() +
                exceptions
                    .collect({ e -> e.message })
                    .join(System.lineSeparator()))
        }
    }

    protected static DistributionProcessor createDistributionProcessor(JReleaserContext context,
                                                                       Distribution distribution,
                                                                       String toolName) {
        return DistributionProcessor.builder()
            .context(context)
            .distributionName(distribution.getName())
            .toolName(toolName)
            .build()
    }

    interface ToolProcessingFunction {
        void consume(DistributionProcessor processor) throws ToolProcessingException
    }
}
