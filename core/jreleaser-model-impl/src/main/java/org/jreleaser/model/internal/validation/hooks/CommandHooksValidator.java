/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.model.internal.validation.hooks;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.MatrixAware;
import org.jreleaser.model.internal.hooks.CommandHook;
import org.jreleaser.model.internal.hooks.CommandHooks;
import org.jreleaser.model.internal.hooks.NamedCommandHooks;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.MatrixValidator.validateMatrix;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class CommandHooksValidator {
    private CommandHooksValidator() {
        // noop
    }

    public static void validateCommandHooks(JReleaserContext context, Errors errors) {
        context.getLogger().debug("hooks.command");

        CommandHooks hooks = context.getModel().getHooks().getCommand();
        boolean activeSet = hooks.isActiveSet();
        resolveActivatable(context, hooks, "hooks.command", "ALWAYS");
        hooks.resolveEnabled(context.getModel().getProject());

        if (hooks.getMatrix().isEmpty()) {
            hooks.setMatrix(context.getModel().getHooks().getMatrix());
        }
        if (hooks.isApplyDefaultMatrix()) {
            hooks.setMatrix(context.getModel().getMatrix());
        }

        validateMatrix(context, hooks.getMatrix(), "hooks.command.matrix", errors);

        for (int i = 0; i < hooks.getBefore().size(); i++) {
            validateCommandHook(context, hooks, hooks.getBefore().get(i), "before", i, errors);
        }
        for (int i = 0; i < hooks.getSuccess().size(); i++) {
            validateCommandHook(context, hooks, hooks.getSuccess().get(i), "success", i, errors);
        }
        for (int i = 0; i < hooks.getFailure().size(); i++) {
            validateCommandHook(context, hooks, hooks.getFailure().get(i), "failure", i, errors);
        }

        for (Map.Entry<String, NamedCommandHooks> e : hooks.getGroups().entrySet()) {
            e.getValue().setName(e.getKey());
            validateNamedCommandHooks(context, hooks, e.getValue(), errors);
        }

        if (hooks.isEnabled()) {
            boolean enabled = hooks.getBefore().stream().anyMatch(CommandHook::isEnabled) ||
                hooks.getSuccess().stream().anyMatch(CommandHook::isEnabled) ||
                hooks.getFailure().stream().anyMatch(CommandHook::isEnabled) ||
                hooks.getGroups().values().stream().anyMatch(NamedCommandHooks::isEnabled);

            if (!activeSet && !enabled) {
                context.getLogger().debug(RB.$("validation.disabled"));
                hooks.disable();
            }
        }
    }

    private static void validateNamedCommandHooks(JReleaserContext context, CommandHooks parentHooks, NamedCommandHooks hooks, Errors errors) {
        context.getLogger().debug("hooks.command." + hooks.getName());

        boolean activeSet = hooks.isActiveSet();
        resolveActivatable(context, hooks, "hooks.command." + hooks.getName(), "ALWAYS");
        hooks.resolveEnabled(context.getModel().getProject());

        if (hooks.getMatrix().isEmpty()) {
            hooks.setMatrix(parentHooks.getMatrix());
        }
        if (hooks.isApplyDefaultMatrix()) {
            hooks.setMatrix(context.getModel().getMatrix());
        }

        validateMatrix(context, hooks.getMatrix(), "hooks.command.matrix", errors);

        for (int i = 0; i < hooks.getBefore().size(); i++) {
            hooks.getBefore().get(i).setName(hooks.getName());
            validateCommandHook(context, hooks, hooks.getBefore().get(i), hooks.getName() + ".before", i, errors);
        }
        for (int i = 0; i < hooks.getSuccess().size(); i++) {
            hooks.getSuccess().get(i).setName(hooks.getName());
            validateCommandHook(context, hooks, hooks.getSuccess().get(i), hooks.getName() + ".success", i, errors);
        }
        for (int i = 0; i < hooks.getFailure().size(); i++) {
            hooks.getFailure().get(i).setName(hooks.getName());
            validateCommandHook(context, hooks, hooks.getFailure().get(i), hooks.getName() + ".failure", i, errors);
        }

        if (hooks.isEnabled()) {
            boolean enabled = hooks.getBefore().stream().anyMatch(CommandHook::isEnabled) ||
                hooks.getSuccess().stream().anyMatch(CommandHook::isEnabled) ||
                hooks.getFailure().stream().anyMatch(CommandHook::isEnabled);

            if (!activeSet && !enabled) {
                context.getLogger().debug(RB.$("validation.disabled"));
                hooks.disable();
            }
        }
    }

    private static void validateCommandHook(JReleaserContext context, MatrixAware matrixProvider, CommandHook hook, String type, int index, Errors errors) {
        context.getLogger().debug("hooks.command.{}[{}]", type, index);

        resolveActivatable(context, hook, "hooks.command." + type + "." + index, "ALWAYS");
        if (!hook.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (hook.getMatrix().isEmpty()) {
            hook.setMatrix(matrixProvider.getMatrix());
        }
        if (hook.isApplyDefaultMatrix()) {
            hook.setMatrix(context.getModel().getMatrix());
        }

        validateMatrix(context, hook.getMatrix(), "hooks.command." + type + "[" + index + "].matrix", errors);

        if (isBlank(hook.getCmd())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "hook.cmd"));
        }
    }
}