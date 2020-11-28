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

import org.kordamp.jreleaser.model.Distribution;
import org.kordamp.jreleaser.model.JReleaserModel;
import org.kordamp.jreleaser.model.Tool;
import org.kordamp.jreleaser.util.Logger;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface ToolProcessor<T extends Tool> {
    T getTool();

    String getToolName();

    Logger getLogger();

    JReleaserModel getModel();

    boolean prepareDistribution(Distribution distribution, Map<String, Object> context) throws ToolProcessingException;

    boolean packageDistribution(Distribution distribution, Map<String, Object> context) throws ToolProcessingException;
}
