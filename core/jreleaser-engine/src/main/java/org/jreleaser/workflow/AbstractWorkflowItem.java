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
package org.jreleaser.workflow;

import org.jreleaser.engine.hooks.HookExecutor;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.internal.JReleaserContext;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public abstract class AbstractWorkflowItem implements WorkflowItem {
    private final JReleaserCommand command;

    protected AbstractWorkflowItem(JReleaserCommand command) {
        this.command = command;
    }

    @Override
    public JReleaserCommand getCommand() {
        return command;
    }

    @Override
    public void invoke(JReleaserContext context) {
        HookExecutor executor = new HookExecutor(context);
        executor.execute(command.toStep(), () -> doInvoke(context));
    }

    protected abstract void doInvoke(JReleaserContext context);
}
