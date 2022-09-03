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
package org.jreleaser.engine.assemble;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.spi.assemble.AssemblerProcessor;
import org.jreleaser.model.spi.assemble.AssemblerProcessorFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class AssemblerProcessors {
    public static <A extends Assembler> AssemblerProcessor<A> findProcessor(JReleaserContext context, A assembler) {
        Map<String, AssemblerProcessor> processors = StreamSupport.stream(ServiceLoader.load(AssemblerProcessorFactory.class,
                AssemblerProcessors.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(AssemblerProcessorFactory::getName, factory -> factory.getAssemblerProcessor(context)));

        if (processors.containsKey(assembler.getType())) {
            AssemblerProcessor<A> assemblerProcessor = processors.get(assembler.getType());
            assemblerProcessor.setAssembler(assembler);
            return assemblerProcessor;
        }

        throw new JReleaserException(RB.$("ERROR_unsupported_assembler", assembler.getType()));
    }
}
