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
import org.jreleaser.model.internal.common.MatrixAware;
import org.jreleaser.model.internal.hooks.JbangHook;
import org.jreleaser.model.internal.hooks.JbangHooks;
import org.jreleaser.model.internal.hooks.NamedJbangHooks;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.MatrixValidator.validateMatrix;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.2o.0
 */
public final class JbangHooksValidator {
    private JbangHooksValidator() {
        // noop
    }

    public static void validateJbangHooks(JReleaserContext context, Errors errors) {
        context.getLogger().debug("hooks.jbang");

        JbangHooks hooks = context.getModel().getHooks().getJbang();
        boolean activeSet = hooks.isActiveSet();
        resolveActivatable(context, hooks, "hooks.jbang", "ALWAYS");
        hooks.resolveEnabled(context.getModel().getProject());

        if (hooks.getMatrix().isEmpty()) {
            hooks.setMatrix(context.getModel().getHooks().getMatrix());
        }
        if (hooks.isApplyDefaultMatrix()) {
            hooks.setMatrix(context.getModel().getMatrix());
        }

        validateMatrix(context, hooks.getMatrix(), "hooks.jbang.matrix", errors);

        for (int i = 0; i < hooks.getBefore().size(); i++) {
            JbangHook hook = hooks.getBefore().get(i);
            if (isBlank(hook.getVersion())) hook.setVersion(hooks.getVersion());
            validateJbangHook(context, hooks, hook, "before", i, errors);
        }
        for (int i = 0; i < hooks.getSuccess().size(); i++) {
            JbangHook hook = hooks.getSuccess().get(i);
            if (isBlank(hook.getVersion())) hook.setVersion(hooks.getVersion());
            validateJbangHook(context, hooks, hook, "success", i, errors);
        }
        for (int i = 0; i < hooks.getFailure().size(); i++) {
            JbangHook hook = hooks.getFailure().get(i);
            if (isBlank(hook.getVersion())) hook.setVersion(hooks.getVersion());
            validateJbangHook(context, hooks, hook, "failure", i, errors);
        }

        for (Map.Entry<String, NamedJbangHooks> e : hooks.getGroups().entrySet()) {
            NamedJbangHooks namedHooks = e.getValue();
            namedHooks.setName(e.getKey());
            if (isBlank(namedHooks.getVersion())) namedHooks.setVersion(hooks.getVersion());
            validateNamedJbangHooks(context, hooks, namedHooks, errors);
        }

        if (hooks.isEnabled()) {
            boolean enabled = hooks.getBefore().stream().anyMatch(JbangHook::isEnabled) ||
                hooks.getSuccess().stream().anyMatch(JbangHook::isEnabled) ||
                hooks.getFailure().stream().anyMatch(JbangHook::isEnabled) ||
                hooks.getGroups().values().stream().anyMatch(NamedJbangHooks::isEnabled);

            if (!activeSet && !enabled) {
                context.getLogger().debug(RB.$("validation.disabled"));
                hooks.disable();
            }
        }
    }

    private static void validateNamedJbangHooks(JReleaserContext context, JbangHooks parentHooks, NamedJbangHooks hooks, Errors errors) {
        context.getLogger().debug("hooks.jbang." + hooks.getName());

        boolean activeSet = hooks.isActiveSet();
        resolveActivatable(context, hooks, "hooks.jbang." + hooks.getName(), "ALWAYS");
        hooks.resolveEnabled(context.getModel().getProject());

        if (hooks.getMatrix().isEmpty()) {
            hooks.setMatrix(parentHooks.getMatrix());
        }
        if (hooks.isApplyDefaultMatrix()) {
            hooks.setMatrix(context.getModel().getMatrix());
        }

        validateMatrix(context, hooks.getMatrix(), "hooks.jbang.matrix", errors);

        for (int i = 0; i < hooks.getBefore().size(); i++) {
            JbangHook hook = hooks.getBefore().get(i);
            hook.setName(hooks.getName());
            if (isBlank(hook.getVersion())) hook.setVersion(hooks.getVersion());
            if (isBlank(hook.getScript())) hook.setScript(hooks.getScript());
            validateJbangHook(context, hooks, hook, hooks.getName() + ".before", i, errors);
        }
        for (int i = 0; i < hooks.getSuccess().size(); i++) {
            JbangHook hook = hooks.getSuccess().get(i);
            hook.setName(hooks.getName());
            if (isBlank(hook.getVersion())) hook.setVersion(hooks.getVersion());
            if (isBlank(hook.getScript())) hook.setScript(hooks.getScript());
            validateJbangHook(context, hooks, hook, hooks.getName() + ".success", i, errors);
        }
        for (int i = 0; i < hooks.getFailure().size(); i++) {
            JbangHook hook = hooks.getFailure().get(i);
            hook.setName(hooks.getName());
            if (isBlank(hook.getVersion())) hook.setVersion(hooks.getVersion());
            if (isBlank(hook.getScript())) hook.setScript(hooks.getScript());
            validateJbangHook(context, hooks, hook, hooks.getName() + ".failure", i, errors);
        }

        if (hooks.isEnabled()) {
            boolean enabled = hooks.getBefore().stream().anyMatch(JbangHook::isEnabled) ||
                hooks.getSuccess().stream().anyMatch(JbangHook::isEnabled) ||
                hooks.getFailure().stream().anyMatch(JbangHook::isEnabled);

            if (!activeSet && !enabled) {
                context.getLogger().debug(RB.$("validation.disabled"));
                hooks.disable();
            }
        }
    }

    private static void validateJbangHook(JReleaserContext context, MatrixAware matrixProvider, JbangHook hook, String type, int index, Errors errors) {
        context.getLogger().debug("hooks.jbang.{}[{}]", type, index);

        resolveActivatable(context, hook, "hooks.jbang." + type + "." + index, "ALWAYS");
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

        validateMatrix(context, hook.getMatrix(), "hooks.jbang." + type + "[" + index + "].matrix", errors);

        if (isBlank(hook.getScript())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "hook.script"));
        }
    }
}