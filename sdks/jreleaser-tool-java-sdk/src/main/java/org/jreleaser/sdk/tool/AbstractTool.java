/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.sdk.tool;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.util.PlatformUtils;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class AbstractTool {
    protected final JReleaserContext context;
    protected final DownloadableTool tool;
    protected final String name;
    protected final boolean verifyErrorOutput;

    public AbstractTool(JReleaserContext context, String name, String version) {
        this(context, name, version, false);
    }

    public AbstractTool(JReleaserContext context, String name, String version, boolean verifyErrorOutput) {
        requireNonBlank(version, "'version' must not be blank");
        this.name = requireNonBlank(name, "'name' must not be blank");
        this.context = context;
        this.verifyErrorOutput = verifyErrorOutput;
        this.tool = new DownloadableTool(context.getLogger(), name, version, PlatformUtils.getCurrentFull(), true);
    }

    public boolean setup() throws ToolException {
        if (!tool.verify()) {
            if (tool.isEnabled()) {
                try {
                    tool.download();
                } catch (Exception e) {
                    throw new ToolException(RB.$("ERROR_unexpected_error"), e);
                }
                if (tool.verify()) return true;
            }

            context.getLogger().warn(RB.$("tool_verify_error", name, tool.getVersion()));
            return false;
        }

        return true;
    }

    protected void executeCommand(CommandExecution execution) throws CommandException {
        int exitValue = execution.execute();
        if (exitValue != 0) {
            throw new CommandException(RB.$("ERROR_command_execution_exit_value", exitValue));
        }
    }

    interface CommandExecution {
        int execute() throws CommandException;
    }
}
