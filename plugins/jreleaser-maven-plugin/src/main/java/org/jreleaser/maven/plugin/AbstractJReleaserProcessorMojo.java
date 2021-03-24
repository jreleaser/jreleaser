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
package org.jreleaser.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.tools.Distributions;
import org.jreleaser.tools.ToolProcessingFunction;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractJReleaserProcessorMojo extends AbstractJReleaserMojo {
    /**
     * Stops on the first error.
     */
    @Parameter(property = "jreleaser.failfast", defaultValue = "true")
    protected boolean failFast;

    protected static void processContext(JReleaserContext context, boolean failFast, String action, ToolProcessingFunction function)
        throws MojoExecutionException, MojoFailureException {
        Distributions.process(context, failFast, action, function);
    }
}
