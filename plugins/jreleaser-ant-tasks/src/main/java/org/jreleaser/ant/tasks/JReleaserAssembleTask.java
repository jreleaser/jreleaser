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
package org.jreleaser.ant.tasks;

import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class JReleaserAssembleTask extends AbstractDistributionAwareJReleaserTask {
    private final List<String> assemblers = new ArrayList<>();
    private final List<String> excludedAssemblers = new ArrayList<>();

    public void setAssemblers(String assemblers) {
        this.assemblers.addAll(expandAndCollect(assemblers));
    }

    public void setExcludedAssemblers(String excludedAssemblers) {
        this.excludedAssemblers.addAll(expandAndCollect(excludedAssemblers));
    }

    public void setAssemblers(List<String> assemblers) {
        if (null != assemblers) {
            this.assemblers.addAll(assemblers);
        }
    }

    public void setExcludedAssemblers(List<String> excludedAssemblers) {
        if (null != excludedAssemblers) {
            this.excludedAssemblers.addAll(excludedAssemblers);
        }
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        context.setIncludedAssemblers(collectEntries(assemblers, true));
        context.setExcludedAssemblers(collectEntries(excludedAssemblers, true));
        Workflows.assemble(setupContext(context)).execute();
    }

    @Override
    protected Mode getMode() {
        return Mode.ASSEMBLE;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.ASSEMBLE;
    }
}
