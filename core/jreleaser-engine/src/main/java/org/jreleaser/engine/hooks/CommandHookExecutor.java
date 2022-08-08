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
package org.jreleaser.engine.hooks;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.CommandHook;
import org.jreleaser.model.CommandHooks;
import org.jreleaser.model.ExecutionEvent;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.JReleaserException;
import org.jreleaser.util.command.Command;
import org.jreleaser.util.command.CommandException;
import org.jreleaser.util.command.CommandExecutor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class CommandHookExecutor {
    private final JReleaserContext context;

    public CommandHookExecutor(JReleaserContext context) {
        this.context = context;
    }

    public void execute(String step, Runnable runnable) {
        executeHooks(ExecutionEvent.before(step));

        try {
            runnable.run();
        } catch (RuntimeException e) {
            executeHooks(ExecutionEvent.failure(step));
            throw e;
        }
        executeHooks(ExecutionEvent.success(step));
    }

    private void executeHooks(ExecutionEvent event) {
        if (!context.getModel().getHooks().isEnabled()) return;

        CommandHooks commandHooks = context.getModel().getHooks().getCommand();
        if (!context.getModel().getHooks().getCommand().isEnabled()) return;

        final List<CommandHook> hooks = new ArrayList<>();

        switch (event.getType()) {
            case BEFORE:
                hooks.addAll(filter(commandHooks.getBefore(), event));
                break;
            case SUCCESS:
                hooks.addAll(filter(commandHooks.getSuccess(), event));
                break;
            case FAILURE:
                hooks.addAll(filter(commandHooks.getFailure(), event));
                break;
        }

        if (hooks.size() > 0) {
            context.getLogger().info(RB.$("hooks.execution"), event.getType().name().toLowerCase(Locale.ENGLISH), hooks.size());
        }

        context.getLogger().setPrefix("hooks");
        context.getLogger().increaseIndent();

        try {
            for (CommandHook hook : hooks) {
                String resolvedCmd = hook.getResolvedCmd(context, event);

                List<String> commandLine = null;

                try {
                    commandLine = parseCommand(resolvedCmd);
                } catch (IllegalStateException e) {
                    throw new JReleaserException(RB.$("ERROR_command_hook_parser_error", hook.getCmd()), e);
                }

                try {
                    Command command = new Command(commandLine);
                    executeCommand(context.getBasedir(), command);
                } catch (CommandException e) {
                    if (!hook.isContinueOnError()) {
                        throw new JReleaserException(RB.$("ERROR_command_hook_unexpected_error"), e);
                    } else {
                        if (e.getCause() != null) {
                            context.getLogger().warn(e.getCause().getMessage());
                        } else {
                            context.getLogger().warn(e.getMessage());
                        }
                        context.getLogger().trace(RB.$("ERROR_command_hook_unexpected_error"), e);
                    }
                }
            }
        } finally {
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
        }
    }

    private Collection<? extends CommandHook> filter(List<CommandHook> hooks, ExecutionEvent event) {
        List<CommandHook> tmp = new ArrayList<>();

        for (CommandHook hook : hooks) {
            if (!hook.isEnabled()) continue;

            if (!hook.getFilter().getResolvedIncludes().isEmpty()) {
                if (hook.getFilter().getResolvedIncludes().contains(event.getName())) {
                    tmp.add(hook);
                }
            } else {
                tmp.add(hook);
            }

            if (hook.getFilter().getResolvedExcludes().contains(event.getName())) {
                tmp.remove(hook);
            }
        }

        return tmp;
    }

    private void executeCommand(Path directory, Command command) throws CommandException {
        int exitValue = new CommandExecutor(context.getLogger())
            .executeCommand(directory, command);
        if (exitValue != 0) {
            throw new CommandException(RB.$("ERROR_command_execution_exit_value", exitValue));
        }
    }

    // adjusted from org.apache.tools.ant.types.Commandline#translateCommandLine
    public static List<String> parseCommand(String str) {
        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(str, "\"' ", true);
        final ArrayList<String> result = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
                case inQuote:
                    if ("'".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                case inDoubleQuote:
                    if ("\"".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                default:
                    if ("'".equals(nextTok)) {
                        state = inQuote;
                    } else if ("\"".equals(nextTok)) {
                        state = inDoubleQuote;
                    } else if (" ".equals(nextTok)) {
                        if (lastTokenHasBeenQuoted || current.length() > 0) {
                            result.add(current.toString());
                            current.setLength(0);
                        }
                    } else {
                        current.append(nextTok);
                    }
                    lastTokenHasBeenQuoted = false;
                    break;
            }
        }

        if (lastTokenHasBeenQuoted || current.length() > 0) {
            result.add(current.toString());
        }

        if (state == inQuote || state == inDoubleQuote) {
            throw new IllegalStateException(RB.$("ERROR_unbalanced_quotes", str));
        }

        return result;
    }
}
