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
package org.jreleaser.cli;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.tools.Distributions;
import org.jreleaser.tools.ToolProcessingFunction;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command
public abstract class AbstractProcessorCommand extends AbstractModelCommand {
    @CommandLine.Option(names = {"--fail-fast"},
        description = "Fail fast")
    boolean failFast = true;

    protected static void processContext(JReleaserContext context, boolean failFast, String action, ToolProcessingFunction function) {
        Distributions.process(context, failFast, action, function);
    }
}
