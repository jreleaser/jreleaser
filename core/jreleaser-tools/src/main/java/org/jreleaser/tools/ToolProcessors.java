/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.tools;

import org.jreleaser.model.Brew;
import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Scoop;
import org.jreleaser.model.Snap;
import org.jreleaser.model.Tool;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ToolProcessors {
    public static <T extends Tool> ToolProcessor<T> findProcessor(JReleaserContext context, T tool) {
        if (tool instanceof Brew) {
            return (ToolProcessor<T>) new BrewToolProcessor(context, (Brew) tool);
        } else if (tool instanceof Chocolatey) {
            return (ToolProcessor<T>) new ChocolateyToolProcessor(context, (Chocolatey) tool);
        } else if (tool instanceof Scoop) {
            return (ToolProcessor<T>) new ScoopToolProcessor(context, (Scoop) tool);
        } else if (tool instanceof Snap) {
            return (ToolProcessor<T>) new SnapToolProcessor(context, (Snap) tool);
        }

        throw new IllegalArgumentException("Unsupported tool " + tool);
    }
}
