/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.tools;

import org.kordamp.jreleaser.model.Brew;
import org.kordamp.jreleaser.model.Chocolatey;
import org.kordamp.jreleaser.model.JReleaserModel;
import org.kordamp.jreleaser.model.Scoop;
import org.kordamp.jreleaser.model.Snap;
import org.kordamp.jreleaser.model.Tool;
import org.kordamp.jreleaser.util.Logger;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ToolProcessors {
    public static <T extends Tool> ToolProcessor<T> findProcessor(Logger logger, JReleaserModel model, T tool) {
        if (tool instanceof Brew) {
            return (ToolProcessor<T>) new BrewToolProcessor(logger, model, (Brew) tool);
        } else if (tool instanceof Chocolatey) {
            return (ToolProcessor<T>) new ChocolateyToolProcessor(logger, model, (Chocolatey) tool);
        } else if (tool instanceof Scoop) {
            return (ToolProcessor<T>) new ScoopToolProcessor(logger, model, (Scoop) tool);
        } else if (tool instanceof Snap) {
            return (ToolProcessor<T>) new SnapToolProcessor(logger, model, (Snap) tool);
        }

        throw new IllegalArgumentException("Unsupported tool " + tool);
    }
}
