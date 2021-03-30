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
package org.jreleaser.engine.distribution;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Tool;
import org.jreleaser.model.tool.spi.ToolProcessor;
import org.jreleaser.model.tool.spi.ToolProcessorFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ToolProcessors {
    public static <T extends Tool> ToolProcessor<T> findProcessor(JReleaserContext context, T tool) {
        Map<String, ToolProcessor> processors = StreamSupport.stream(ServiceLoader.load(ToolProcessorFactory.class,
            ToolProcessors.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(ToolProcessorFactory::getName, factory -> factory.getToolProcessor(context)));

        if (processors.containsKey(tool.getName())) {
            ToolProcessor<T> toolProcessor = processors.get(tool.getName());
            toolProcessor.setTool(tool);
            return toolProcessor;
        }

        throw new IllegalArgumentException("Unsupported tool " + tool);
    }
}
