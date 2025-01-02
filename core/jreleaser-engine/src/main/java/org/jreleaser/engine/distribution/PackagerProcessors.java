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
package org.jreleaser.engine.distribution;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.packagers.Packager;
import org.jreleaser.model.spi.packagers.PackagerProcessor;
import org.jreleaser.model.spi.packagers.PackagerProcessorFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toMap;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class PackagerProcessors {
    private PackagerProcessors() {
        // noop
    }

    public static <T extends Packager> PackagerProcessor<T> findProcessor(JReleaserContext context, T packager) {
        Map<String, PackagerProcessor> processors = StreamSupport.stream(ServiceLoader.load(PackagerProcessorFactory.class,
                PackagerProcessors.class.getClassLoader()).spliterator(), false)
            .collect(toMap(PackagerProcessorFactory::getName, factory -> factory.getPackagerNameProcessor(context)));

        if (processors.containsKey(packager.getType())) {
            PackagerProcessor<T> packagerProcessor = processors.get(packager.getType());
            packagerProcessor.setPackager(packager);
            return packagerProcessor;
        }

        throw new JReleaserException(RB.$("ERROR_unsupported_packager", packager.getType()));
    }
}
