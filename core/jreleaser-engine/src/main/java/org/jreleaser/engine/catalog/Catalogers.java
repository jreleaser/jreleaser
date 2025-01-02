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
package org.jreleaser.engine.catalog;

import org.jreleaser.bundle.RB;
import org.jreleaser.engine.catalog.sbom.SbomCatalogers;
import org.jreleaser.model.internal.JReleaserContext;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public final class Catalogers {
    private Catalogers() {
        // noop
    }

    public static void catalog(JReleaserContext context) {
        context.getLogger().info(RB.$("catalogers.header"));

        if (!context.getModel().getCatalog().isEnabled()) {
            context.getLogger().increaseIndent();
            context.getLogger().info(RB.$("catalogers.not.enabled"));
            context.getLogger().decreaseIndent();
            return;
        }

        SbomCatalogers.catalog(context);
        Github.catalog(context);
        Slsa.catalog(context);
    }
}
