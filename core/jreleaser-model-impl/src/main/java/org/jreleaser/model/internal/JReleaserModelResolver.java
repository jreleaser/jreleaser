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
package org.jreleaser.model.internal;

import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.assemble.AssemblersResolver.resolveAssemblers;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class JReleaserModelResolver {
    private JReleaserModelResolver() {
        // noop
    }

    public static void resolve(JReleaserContext context, Errors errors) {
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("resolution");
        try {
            resolveModel(context, errors);
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static void resolveModel(JReleaserContext context, Errors errors) {
        resolveAssemblers(context, errors);
    }
}
