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
package org.jreleaser.model.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.CommandHook;
import org.jreleaser.model.CommandHooks;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public abstract class CommandHooksValidator extends Validator {
    public static void validateCommandHooks(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("hooks.command");

        CommandHooks hooks = context.getModel().getHooks().getCommand();
        boolean activeSet = hooks.isActiveSet();
        hooks.resolveEnabled(context.getModel().getProject());

        for (int i = 0; i < hooks.getBefore().size(); i++) {
            validateCommandHook(context, mode, hooks.getBefore().get(i), "before", i, errors);
        }
        for (int i = 0; i < hooks.getSuccess().size(); i++) {
            validateCommandHook(context, mode, hooks.getSuccess().get(i), "success", i, errors);
        }
        for (int i = 0; i < hooks.getFailure().size(); i++) {
            validateCommandHook(context, mode, hooks.getFailure().get(i), "failure", i, errors);
        }

        if (hooks.isEnabled()) {
            boolean enabled = hooks.getBefore().stream().anyMatch(CommandHook::isEnabled) ||
                hooks.getSuccess().stream().anyMatch(CommandHook::isEnabled) ||
                hooks.getFailure().stream().anyMatch(CommandHook::isEnabled);

            if (!activeSet && !enabled) hooks.disable();
        }
    }

    private static void validateCommandHook(JReleaserContext context, JReleaserContext.Mode mode, CommandHook hook, String type, int index, Errors errors) {
        context.getLogger().debug("hooks.command.{}[{}]", type, index);

        if (!hook.isActiveSet()) {
            hook.setActive(Active.ALWAYS);
        }
        if (!hook.resolveEnabled(context.getModel().getProject())) return;

        if (isBlank(hook.getCmd())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "hook.cmd"));
        }
    }
}