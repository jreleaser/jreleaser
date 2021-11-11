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
package org.jreleaser.ant.tasks;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.workflow.Workflows;

import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class JReleaserAssembleTask extends AbstractJReleaserTask {
    private List<String> assemblers;
    private List<String> excludedAssemblers;
    private List<String> distributions;
    private List<String> excludedDistributions;

    public void setAssemblers(List<String> assemblers) {
        this.assemblers = assemblers;
    }

    public void setExcludedAssemblers(List<String> excludedAssemblers) {
        this.excludedAssemblers = excludedAssemblers;
    }

    public void setDistributions(List<String> distributions) {
        this.distributions = distributions;
    }

    public void setExcludedDistributions(List<String> excludedDistributions) {
        this.excludedDistributions = excludedDistributions;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        context.setIncludedAssemblers(assemblers);
        context.setExcludedAssemblers(excludedAssemblers);
        context.setIncludedDistributions(distributions);
        context.setExcludedDistributions(excludedDistributions);
        Workflows.assemble(context).execute();
    }

    @Override
    protected JReleaserContext.Mode getMode() {
        return JReleaserContext.Mode.ASSEMBLE;
    }
}
