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
package org.jreleaser.engine.hooks;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Matrix;
import org.jreleaser.model.internal.hooks.CommandHook;
import org.jreleaser.model.internal.hooks.CommandHookProvider;
import org.jreleaser.model.internal.hooks.CommandHooks;
import org.jreleaser.model.internal.hooks.Hook;
import org.jreleaser.model.internal.hooks.Hooks;
import org.jreleaser.model.internal.hooks.JbangHook;
import org.jreleaser.model.internal.hooks.JbangHookProvider;
import org.jreleaser.model.internal.hooks.JbangHooks;
import org.jreleaser.model.internal.hooks.NamedCommandHooks;
import org.jreleaser.model.internal.hooks.NamedJbangHooks;
import org.jreleaser.model.internal.hooks.NamedScriptHooks;
import org.jreleaser.model.internal.hooks.ScriptHook;
import org.jreleaser.model.internal.hooks.ScriptHookProvider;
import org.jreleaser.model.internal.hooks.ScriptHooks;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.command.CommandExecutor;
import org.jreleaser.sdk.tool.JBang;
import org.jreleaser.sdk.tool.ToolException;
import org.jreleaser.util.DefaultVersions;
import org.jreleaser.util.PlatformUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.model.Constants.KEY_PLATFORM;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isFalse;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class HookExecutor {
    private static final String JRELEASER_OUTPUT = "JRELEASER_OUTPUT:";
    private final JReleaserContext context;

    public HookExecutor(JReleaserContext context) {
        this.context = context;
    }

    public void execute(String step, Runnable runnable) {
        executeHooks(ExecutionEvent.before(step));

        try {
            runnable.run();
        } catch (RuntimeException e) {
            executeHooks(ExecutionEvent.failure(step, e));
            throw e;
        }
        executeHooks(ExecutionEvent.success(step));
    }

    public void executeHooks(ExecutionEvent event) {
        Hooks hooks = context.getModel().getHooks();
        if (!hooks.isEnabled() || evaluateCondition(hooks.getCondition())) {
            return;
        }

        Map<String, String> rootEnv = resolveEnvironment(hooks.getEnvironment());
        executeScriptHooks(event, rootEnv);
        executeCommandHooks(event, rootEnv);
        executeJbangHooks(event, rootEnv);
    }

    private boolean evaluateCondition(String condition) {
        return isNotBlank(condition) && isFalse(context.eval(condition));
    }

    private Map<String, String> resolveEnvironment(Map<String, String> src) {
        return resolveEnvironment(src, null);
    }

    private Map<String, String> resolveEnvironment(Map<String, String> src, TemplateContext additionalContext) {
        Map<String, String> env = new LinkedHashMap<>();
        TemplateContext props = context.props().setAll(additionalContext);
        src.forEach((k, v) -> {
            String value = resolveTemplate(v, props);
            if (isNotBlank(value)) env.put(k, value);
        });
        return env;
    }

    private Map<String, String> mergeEnvironment(Map<String, String> src, Map<String, String>... others) {
        Map<String, String> tmp = new LinkedHashMap<>(src);
        if (null != others && others.length > 0) {
            for (Map<String, String> env : others) {
                tmp.putAll(env);
            }
        }
        return tmp;
    }

    private void executeScriptHooks(ExecutionEvent event, Map<String, String> rootEnv) {
        ScriptHooks scriptHooks = context.getModel().getHooks().getScript();
        if (!scriptHooks.isEnabled() || evaluateCondition(scriptHooks.getCondition())) {
            return;
        }

        final List<ScriptHook> hooks = collectScriptHooks(event, scriptHooks);
        if (!hooks.isEmpty()) {
            context.getLogger().info(RB.$("hooks.script.execution"), event.getType().name().toLowerCase(Locale.ENGLISH), hooks.size());
        }

        context.getLogger().setPrefix("hooks");
        context.getLogger().increaseIndent();

        try {
            for (ScriptHook hook : hooks) {
                String prefix = "hooks";
                if (isNotBlank(hook.getName())) {
                    prefix += "." + hook.getName();
                }
                context.getLogger().replacePrefix(prefix);

                if (!hook.getMatrix().isEmpty()) {
                    for (Map<String, String> matrixRow : hook.getMatrix().resolve()) {
                        if (matrixRow.containsKey(KEY_PLATFORM)) {
                            String srcPlatform = matrixRow.get(KEY_PLATFORM);
                            if (!context.isPlatformSelected(srcPlatform)) {
                                continue;
                            }
                        }

                        executeScriptHook(event, hook, mergeEnvironment(rootEnv, scriptHooks.getEnvironment()), matrixRow);
                    }
                } else {
                    executeScriptHook(event, hook, mergeEnvironment(rootEnv, scriptHooks.getEnvironment()), null);
                }
            }
        } finally {
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
        }

        executeNamedScriptHooks(event, rootEnv, scriptHooks);
    }

    private void executeNamedScriptHooks(ExecutionEvent event, Map<String, String> rootEnv, ScriptHooks scriptHooks) {
        if (!scriptHooks.isEnabled() || evaluateCondition(scriptHooks.getCondition())) {
            return;
        }

        for (NamedScriptHooks group : scriptHooks.getGroups().values()) {
            if (!group.isEnabled() || evaluateCondition(group.getCondition())) {
                continue;
            }

            final List<ScriptHook> hooks = collectScriptHooks(event, group);
            if (!hooks.isEmpty()) {
                context.getLogger().info(RB.$("hooks.script.execution"), event.getType().name().toLowerCase(Locale.ENGLISH), hooks.size());
            }

            context.getLogger().setPrefix("hooks");
            context.getLogger().increaseIndent();

            try {
                for (ScriptHook hook : hooks) {
                    String prefix = "hooks";
                    if (isNotBlank(hook.getName())) {
                        prefix += "." + hook.getName();
                    }
                    context.getLogger().replacePrefix(prefix);

                    if (!hook.getMatrix().isEmpty()) {
                        for (Map<String, String> matrixRow : hook.getMatrix().resolve()) {
                            if (matrixRow.containsKey(KEY_PLATFORM)) {
                                String srcPlatform = matrixRow.get(KEY_PLATFORM);
                                if (!context.isPlatformSelected(srcPlatform)) {
                                    continue;
                                }
                            }

                            executeScriptHook(event, hook, mergeEnvironment(rootEnv, scriptHooks.getEnvironment(), group.getEnvironment()), matrixRow);
                        }
                    } else {
                        executeScriptHook(event, hook, mergeEnvironment(rootEnv, scriptHooks.getEnvironment(), group.getEnvironment()), null);
                    }
                }
            } finally {
                context.getLogger().decreaseIndent();
                context.getLogger().restorePrefix();
            }
        }
    }

    private List<ScriptHook> collectScriptHooks(ExecutionEvent event, ScriptHookProvider scriptHookProvider) {
        final List<ScriptHook> hooks = new ArrayList<>();

        switch (event.getType()) {
            case BEFORE:
                hooks.addAll((Collection<ScriptHook>) filter(scriptHookProvider.getBefore(), event));
                break;
            case SUCCESS:
                hooks.addAll((Collection<ScriptHook>) filter(scriptHookProvider.getSuccess(), event));
                break;
            case FAILURE:
                hooks.addAll((Collection<ScriptHook>) filter(scriptHookProvider.getFailure(), event));
                break;
        }
        return hooks;
    }

    private void executeScriptHook(ExecutionEvent event, ScriptHook hook, Map<String, String> env, Map<String, String> matrixRow) {
        TemplateContext additionalContext = Matrix.asTemplateContext(matrixRow);
        Map<String, String> localEnv = new LinkedHashMap<>(env);
        localEnv = resolveEnvironment(localEnv, additionalContext);
        Path scriptFile = null;

        try {
            scriptFile = createScriptFile(context, hook, additionalContext, event);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_script_hook_create_error"), e);
        }

        String resolvedCmd = hook.getShell().expression().replace("{{script}}", scriptFile.toAbsolutePath().toString());
        executeCommandLine(localEnv, additionalContext, hook, resolvedCmd, resolvedCmd, "ERROR_script_hook_unexpected_error");
    }

    private Path createScriptFile(JReleaserContext context, ScriptHook hook, TemplateContext additionalContext, ExecutionEvent event) throws IOException {
        String scriptContents = hook.getResolvedRun(context, additionalContext, event);
        Path scriptFile = Files.createTempFile("jreleaser", hook.getShell().extension());

        if (hook.getShell() == org.jreleaser.model.api.hooks.ScriptHook.Shell.PWSH ||
            hook.getShell() == org.jreleaser.model.api.hooks.ScriptHook.Shell.POWERSHELL) {
            scriptContents = "$ErrorActionPreference = 'stop'" + lineSeparator() + scriptContents;
            scriptContents += lineSeparator() + "if ((Test-Path -LiteralPath variable:\\LASTEXITCODE)) { exit $LASTEXITCODE }";
        }

        Files.write(scriptFile, scriptContents.getBytes(UTF_8), WRITE);
        return scriptFile;
    }

    private void executeCommandHooks(ExecutionEvent event, Map<String, String> rootEnv) {
        CommandHooks commandHooks = context.getModel().getHooks().getCommand();
        if (!commandHooks.isEnabled() || evaluateCondition(commandHooks.getCondition())) {
            return;
        }

        final List<CommandHook> hooks = collectCommandHooks(event, commandHooks);
        if (!hooks.isEmpty()) {
            context.getLogger().info(RB.$("hooks.command.execution"), event.getType().name().toLowerCase(Locale.ENGLISH), hooks.size());
        }

        context.getLogger().setPrefix("hooks");
        context.getLogger().increaseIndent();

        try {
            for (CommandHook hook : hooks) {
                String prefix = "hooks";
                if (isNotBlank(hook.getName())) {
                    prefix += "." + hook.getName();
                }
                context.getLogger().replacePrefix(prefix);

                if (!hook.getMatrix().isEmpty()) {
                    for (Map<String, String> matrixRow : hook.getMatrix().resolve()) {
                        if (matrixRow.containsKey(KEY_PLATFORM)) {
                            String srcPlatform = matrixRow.get(KEY_PLATFORM);
                            if (!context.isPlatformSelected(srcPlatform)) {
                                continue;
                            }
                        }

                        executeCommandHook(event, hook, mergeEnvironment(rootEnv, commandHooks.getEnvironment()), matrixRow);
                    }
                } else {
                    executeCommandHook(event, hook, mergeEnvironment(rootEnv, commandHooks.getEnvironment()), null);
                }
            }
        } finally {
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
        }

        executeNamedCommandHooks(event, rootEnv, commandHooks);
    }

    private void executeNamedCommandHooks(ExecutionEvent event, Map<String, String> rootEnv, CommandHooks commandHooks) {
        if (!commandHooks.isEnabled() || evaluateCondition(commandHooks.getCondition())) {
            return;
        }

        for (NamedCommandHooks group : commandHooks.getGroups().values()) {
            if (!group.isEnabled() || evaluateCondition(group.getCondition())) {
                continue;
            }

            final List<CommandHook> hooks = collectCommandHooks(event, group);
            if (!hooks.isEmpty()) {
                context.getLogger().info(RB.$("hooks.command.execution"), event.getType().name().toLowerCase(Locale.ENGLISH), hooks.size());
            }

            context.getLogger().setPrefix("hooks");
            context.getLogger().increaseIndent();

            try {
                for (CommandHook hook : hooks) {
                    String prefix = "hooks";
                    if (isNotBlank(hook.getName())) {
                        prefix += "." + hook.getName();
                    }
                    context.getLogger().replacePrefix(prefix);

                    if (!hook.getMatrix().isEmpty()) {
                        for (Map<String, String> matrixRow : hook.getMatrix().resolve()) {
                            if (matrixRow.containsKey(KEY_PLATFORM)) {
                                String srcPlatform = matrixRow.get(KEY_PLATFORM);
                                if (!context.isPlatformSelected(srcPlatform)) {
                                    continue;
                                }
                            }

                            executeCommandHook(event, hook, mergeEnvironment(rootEnv, commandHooks.getEnvironment(), group.getEnvironment()), matrixRow);
                        }
                    } else {
                        executeCommandHook(event, hook, mergeEnvironment(rootEnv, commandHooks.getEnvironment(), group.getEnvironment()), null);
                    }
                }
            } finally {
                context.getLogger().decreaseIndent();
                context.getLogger().restorePrefix();
            }
        }
    }

    private List<CommandHook> collectCommandHooks(ExecutionEvent event, CommandHookProvider commandHookProvider) {
        final List<CommandHook> hooks = new ArrayList<>();

        switch (event.getType()) {
            case BEFORE:
                hooks.addAll((Collection<CommandHook>) filter(commandHookProvider.getBefore(), event));
                break;
            case SUCCESS:
                hooks.addAll((Collection<CommandHook>) filter(commandHookProvider.getSuccess(), event));
                break;
            case FAILURE:
                hooks.addAll((Collection<CommandHook>) filter(commandHookProvider.getFailure(), event));
                break;
        }
        return hooks;
    }

    private void executeCommandHook(ExecutionEvent event, CommandHook hook, Map<String, String> env, Map<String, String> matrixRow) {
        TemplateContext additionalContext = Matrix.asTemplateContext(matrixRow);
        Map<String, String> localEnv = new LinkedHashMap<>(env);
        localEnv = resolveEnvironment(localEnv, additionalContext);
        String resolvedCmd = hook.getResolvedCmd(context, additionalContext, event);
        executeCommandLine(localEnv, additionalContext, hook, hook.getCmd(), resolvedCmd, "ERROR_command_hook_unexpected_error");
    }

    private void executeCommandLine(Map<String, String> localEnv, TemplateContext additionalContext, Hook hook, String cmd, String resolvedCmd, String errorKey) {
        List<String> commandLine = null;

        Map<String, String> hookEnv = new LinkedHashMap<>(localEnv);
        hookEnv.putAll(hook.getEnvironment());
        hookEnv = resolveEnvironment(hookEnv, additionalContext);

        try {
            commandLine = parseCommand(resolvedCmd);
        } catch (IllegalStateException e) {
            throw new JReleaserException(RB.$("ERROR_command_hook_parser_error", cmd), e);
        }

        try {
            Command command = new Command(commandLine);
            processOutput(executeCommand(context.getBasedir(), command, hookEnv, hook.isVerbose()));
        } catch (CommandException e) {
            if (!hook.isContinueOnError()) {
                throw new JReleaserException(RB.$(errorKey), e);
            } else {
                if (null != e.getCause()) {
                    context.getLogger().warn(e.getCause().getMessage());
                } else {
                    context.getLogger().warn(e.getMessage());
                }
                context.getLogger().trace(RB.$(errorKey), e);
            }
        }
    }

    private void processOutput(Command.Result result) {
        if (!result.getOut().contains(JRELEASER_OUTPUT)) return;
        for (String line : result.getOut().split(lineSeparator())) {
            if (!line.startsWith(JRELEASER_OUTPUT)) continue;
            line = line.substring(JRELEASER_OUTPUT.length());
            int p = line.indexOf("=");
            String key = line.substring(0, p);
            String value = line.substring(p + 1);
            context.getModel().getEnvironment().getProperties().put(key, value);
        }
    }

    private void executeJbangHooks(ExecutionEvent event, Map<String, String> rootEnv) {
        JbangHooks jbangHooks = context.getModel().getHooks().getJbang();
        if (!jbangHooks.isEnabled() || evaluateCondition(jbangHooks.getCondition())) {
            return;
        }

        final List<JbangHook> hooks = collectJbangHooks(event, jbangHooks);
        if (!hooks.isEmpty()) {
            context.getLogger().info(RB.$("hooks.jbang.execution"), event.getType().name().toLowerCase(Locale.ENGLISH), hooks.size());
        }

        context.getLogger().setPrefix("hooks");
        context.getLogger().increaseIndent();

        try {
            for (JbangHook hook : hooks) {
                String prefix = "hooks";
                if (isNotBlank(hook.getName())) {
                    prefix += "." + hook.getName();
                }
                context.getLogger().replacePrefix(prefix);

                if (!hook.getMatrix().isEmpty()) {
                    for (Map<String, String> matrixRow : hook.getMatrix().resolve()) {
                        if (matrixRow.containsKey(KEY_PLATFORM)) {
                            String srcPlatform = matrixRow.get(KEY_PLATFORM);
                            if (!context.isPlatformSelected(srcPlatform)) {
                                continue;
                            }
                        }

                        executeJbangHook(event, hook, mergeEnvironment(rootEnv, jbangHooks.getEnvironment()), matrixRow);
                    }
                } else {
                    executeJbangHook(event, hook, mergeEnvironment(rootEnv, jbangHooks.getEnvironment()), null);
                }
            }
        } finally {
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
        }

        executeNamedJbangHooks(event, rootEnv, jbangHooks);
    }

    private void executeNamedJbangHooks(ExecutionEvent event, Map<String, String> rootEnv, JbangHooks jbangHooks) {
        if (!jbangHooks.isEnabled() || evaluateCondition(jbangHooks.getCondition())) {
            return;
        }

        for (NamedJbangHooks group : jbangHooks.getGroups().values()) {
            if (!group.isEnabled() || evaluateCondition(group.getCondition())) {
                continue;
            }

            final List<JbangHook> hooks = collectJbangHooks(event, group);
            if (!hooks.isEmpty()) {
                context.getLogger().info(RB.$("hooks.jbang.execution"), event.getType().name().toLowerCase(Locale.ENGLISH), hooks.size());
            }

            context.getLogger().setPrefix("hooks");
            context.getLogger().increaseIndent();

            try {
                for (JbangHook hook : hooks) {
                    String prefix = "hooks";
                    if (isNotBlank(hook.getName())) {
                        prefix += "." + hook.getName();
                    }
                    context.getLogger().replacePrefix(prefix);

                    if (!hook.getMatrix().isEmpty()) {
                        for (Map<String, String> matrixRow : hook.getMatrix().resolve()) {
                            if (matrixRow.containsKey(KEY_PLATFORM)) {
                                String srcPlatform = matrixRow.get(KEY_PLATFORM);
                                if (!context.isPlatformSelected(srcPlatform)) {
                                    continue;
                                }
                            }

                            executeJbangHook(event, hook, mergeEnvironment(rootEnv, jbangHooks.getEnvironment(), group.getEnvironment()), matrixRow);
                        }
                    } else {
                        executeJbangHook(event, hook, mergeEnvironment(rootEnv, jbangHooks.getEnvironment(), group.getEnvironment()), null);
                    }
                }
            } finally {
                context.getLogger().decreaseIndent();
                context.getLogger().restorePrefix();
            }
        }
    }

    private List<JbangHook> collectJbangHooks(ExecutionEvent event, JbangHookProvider jbangHookProvider) {
        final List<JbangHook> hooks = new ArrayList<>();

        switch (event.getType()) {
            case BEFORE:
                hooks.addAll((Collection<JbangHook>) filter(jbangHookProvider.getBefore(), event));
                break;
            case SUCCESS:
                hooks.addAll((Collection<JbangHook>) filter(jbangHookProvider.getSuccess(), event));
                break;
            case FAILURE:
                hooks.addAll((Collection<JbangHook>) filter(jbangHookProvider.getFailure(), event));
                break;
        }
        return hooks;
    }

    private void executeJbangHook(ExecutionEvent event, JbangHook hook, Map<String, String> env, Map<String, String> matrixRow) {
        TemplateContext additionalContext = Matrix.asTemplateContext(matrixRow);
        Map<String, String> localEnv = new LinkedHashMap<>(env);
        localEnv = resolveEnvironment(localEnv, additionalContext);

        String jbangVersion = DefaultVersions.getInstance().getJbangVersion();
        if (isNotBlank(hook.getVersion())) {
            jbangVersion = hook.getVersion();
        }
        JBang jbang = new JBang(context.asImmutable(), jbangVersion);

        try {
            if (!jbang.setup()) {
                throw new JReleaserException(RB.$("tool_unavailable", "jbang"));
            }
        } catch (ToolException e) {
            throw new JReleaserException(RB.$("tool_unavailable", "jbang"), e);
        }

        try {
            List<String> args = new ArrayList<>();
            args.add("run");
            args.addAll(hook.getResolvedJbangArgs(context));
            args.add(hook.getResolvedScript(context));
            args.addAll(hook.getResolvedArgs(context));

            jbang.invokeVerbose(context.getBasedir(), args);
        } catch (CommandException e) {
            throw new JReleaserException(RB.$("ERROR_jbang_hook_unexpected_error"), e);
        }
    }

    private Collection<? extends Hook> filter(List<? extends Hook> hooks, ExecutionEvent event) {
        List<Hook> tmp = new ArrayList<>();

        for (Hook hook : hooks) {
            if (!hook.isEnabled() || evaluateCondition(hook.getCondition())) {
                continue;
            }

            if (!hook.getFilter().getResolvedIncludes().isEmpty()) {
                if (hook.getFilter().getResolvedIncludes().contains(event.getName()) && filterByPlatform(hook)) {
                    tmp.add(hook);
                }
            } else if (filterByPlatform(hook)) {
                tmp.add(hook);
            }

            if (hook.getFilter().getResolvedExcludes().contains(event.getName())) {
                tmp.remove(hook);
            }
        }

        return tmp;
    }

    private boolean filterByPlatform(Hook hook) {
        if (hook.getPlatforms().isEmpty()) return true;

        boolean success = true;
        for (String platform : hook.getPlatforms()) {
            boolean exclude = false;
            if (platform.startsWith("!")) {
                exclude = true;
                platform = platform.substring(1);
            }

            success &= exclude != PlatformUtils.isCompatible(PlatformUtils.getCurrentFull(), platform);
        }

        return success;
    }

    private Command.Result executeCommand(Path directory, Command command, Map<String, String> env, boolean verbose) throws CommandException {
        Command.Result result = new CommandExecutor(context.getLogger(), verbose ? CommandExecutor.Output.VERBOSE : CommandExecutor.Output.DEBUG)
            .environment(env)
            .executeCommand(directory, command);
        if (result.getExitValue() != 0) {
            throw new CommandException(RB.$("ERROR_command_execution_exit_value", result.getExitValue()));
        }
        return result;
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
