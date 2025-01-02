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
import org.jreleaser.model.internal.hooks.Hooks;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.MatrixValidator.validateMatrix;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.hooks.CommandHooksValidator.validateCommandHooks;
import static org.jreleaser.model.internal.validation.hooks.ScriptHooksValidator.validateScriptHooks;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class HooksValidator {
    private HooksValidator() {
        // noop
    }

    public static void validateHooks(JReleaserContext context, Errors errors) {
        context.getLogger().debug("hooks");

        Hooks hooks = context.getModel().getHooks();
        if (hooks.isApplyDefaultMatrix()) {
            hooks.setMatrix(context.getModel().getMatrix());
        }

        validateMatrix(context, hooks.getMatrix(), "hooks.matrix", errors);
        validateCommandHooks(context, errors);
        validateScriptHooks(context, errors);

        boolean activeSet = hooks.isActiveSet();
        resolveActivatable(context, hooks, "hooks", "ALWAYS");
        hooks.resolveEnabled(context.getModel().getProject());

        if (hooks.isEnabled()) {
            boolean enabled = hooks.getCommand().isEnabled() ||
                hooks.getScript().isEnabled();

            if (!activeSet && !enabled) {
                context.getLogger().debug(RB.$("validation.disabled"));
                hooks.disable();
            }
        }
    }
}