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
package org.jreleaser.maven.plugin;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;

/**
 * Sign release artifacts.
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@Mojo(threadSafe = true, name = "sign")
public class JReleaserSignMojo extends AbstractDistributionMojo {
    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.sign.skip")
    private boolean skip;

    @Override
    protected void doExecute(JReleaserContext context) {
        Workflows.sign(context).execute();
    }

    @Override
    protected boolean isSkip() {
        return skip;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.SIGN;
    }
}
