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
package org.jreleaser.model.internal.validation.hooks;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.hooks.CommandHook;
import org.jreleaser.model.internal.hooks.CommandHooks;
import org.jreleaser.util.Errors;

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
            validateCommandHook(context, hooks.getBefore().get(i), "before", i, errors);
        }
        for (int i = 0; i < hooks.getSuccess().size(); i++) {
            validateCommandHook(context, hooks.getSuccess().get(i), "success", i, errors);
        }
        for (int i = 0; i < hooks.getFailure().size(); i++) {
            validateCommandHook(context, hooks.getFailure().get(i), "failure", i, errors);
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

    private static void validateCommandHook(JReleaserContext context, CommandHook hook, String type, int index, Errors errors) {
        context.getLogger().debug("hooks.command.{}[{}]", type, index);

        resolveActivatable(context, hook, "hooks.command." + type + "." + index, "ALWAYS");
        if (!hook.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (hook.getMatrix().isEmpty()) {
            hook.setMatrix(context.getModel().getHooks().getCommand().getMatrix());
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