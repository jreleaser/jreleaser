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
import org.jreleaser.model.internal.hooks.NamedScriptHooks;
import org.jreleaser.model.internal.hooks.ScriptHook;
import org.jreleaser.model.internal.hooks.ScriptHooks;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.MatrixValidator.validateMatrix;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public final class ScriptHooksValidator {
    private ScriptHooksValidator() {
        // noop
    }

    public static void validateScriptHooks(JReleaserContext context, Errors errors) {
        context.getLogger().debug("hooks.script");

        ScriptHooks hooks = context.getModel().getHooks().getScript();
        boolean activeSet = hooks.isActiveSet();
        resolveActivatable(context, hooks, "hooks.script", "ALWAYS");
        hooks.resolveEnabled(context.getModel().getProject());

        if (hooks.getMatrix().isEmpty()) {
            hooks.setMatrix(context.getModel().getHooks().getMatrix());
        }
        if (hooks.isApplyDefaultMatrix()) {
            hooks.setMatrix(context.getModel().getMatrix());
        }

        validateMatrix(context, hooks.getMatrix(), "hooks.script.matrix", errors);

        for (int i = 0; i < hooks.getBefore().size(); i++) {
            validateScriptHook(context, hooks, hooks.getBefore().get(i), "before", i, errors);
        }
        for (int i = 0; i < hooks.getSuccess().size(); i++) {
            validateScriptHook(context, hooks, hooks.getSuccess().get(i), "success", i, errors);
        }
        for (int i = 0; i < hooks.getFailure().size(); i++) {
            validateScriptHook(context, hooks, hooks.getFailure().get(i), "failure", i, errors);
        }

        for (Map.Entry<String, NamedScriptHooks> e : hooks.getGroups().entrySet()) {
            e.getValue().setName(e.getKey());
            validateNamedScriptHooks(context, hooks, e.getValue(), errors);
        }

        if (hooks.isEnabled()) {
            boolean enabled = hooks.getBefore().stream().anyMatch(ScriptHook::isEnabled) ||
                hooks.getSuccess().stream().anyMatch(ScriptHook::isEnabled) ||
                hooks.getFailure().stream().anyMatch(ScriptHook::isEnabled) ||
                hooks.getGroups().values().stream().anyMatch(NamedScriptHooks::isEnabled);

            if (!activeSet && !enabled) {
                context.getLogger().debug(RB.$("validation.disabled"));
                hooks.disable();
            }
        }
    }

    public static void validateNamedScriptHooks(JReleaserContext context, ScriptHooks parentHooks, NamedScriptHooks hooks, Errors errors) {
        context.getLogger().debug("hooks.script." + hooks.getName());

        boolean activeSet = hooks.isActiveSet();
        resolveActivatable(context, hooks, "hooks.script." + hooks.getName(), "ALWAYS");
        hooks.resolveEnabled(context.getModel().getProject());

        if (hooks.getMatrix().isEmpty()) {
            hooks.setMatrix(parentHooks.getMatrix());
        }
        if (hooks.isApplyDefaultMatrix()) {
            hooks.setMatrix(context.getModel().getMatrix());
        }

        validateMatrix(context, hooks.getMatrix(), "hooks.script.matrix", errors);

        for (int i = 0; i < hooks.getBefore().size(); i++) {
            hooks.getBefore().get(i).setName(hooks.getName());
            validateScriptHook(context, hooks, hooks.getBefore().get(i), hooks.getName() + ".before", i, errors);
        }
        for (int i = 0; i < hooks.getSuccess().size(); i++) {
            hooks.getSuccess().get(i).setName(hooks.getName());
            validateScriptHook(context, hooks, hooks.getSuccess().get(i), hooks.getName() + ".success", i, errors);
        }
        for (int i = 0; i < hooks.getFailure().size(); i++) {
            hooks.getFailure().get(i).setName(hooks.getName());
            validateScriptHook(context, hooks, hooks.getFailure().get(i), hooks.getName() + ".failure", i, errors);
        }

        if (hooks.isEnabled()) {
            boolean enabled = hooks.getBefore().stream().anyMatch(ScriptHook::isEnabled) ||
                hooks.getSuccess().stream().anyMatch(ScriptHook::isEnabled) ||
                hooks.getFailure().stream().anyMatch(ScriptHook::isEnabled);

            if (!activeSet && !enabled) {
                context.getLogger().debug(RB.$("validation.disabled"));
                hooks.disable();
            }
        }
    }

    private static void validateScriptHook(JReleaserContext context, MatrixAware matrixProvider, ScriptHook hook, String type, int index, Errors errors) {
        context.getLogger().debug("hooks.script.{}[{}]", type, index);

        resolveActivatable(context, hook, "hooks.script." + type + "." + index, "ALWAYS");
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

        validateMatrix(context, hook.getMatrix(), "hooks.script." + type + "[" + index + "].matrix", errors);

        if (isBlank(hook.getRun())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "hook.script"));
        }
    }
}