/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.assemblers;

import org.jreleaser.model.Archive;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.assembler.spi.AssemblerProcessorFactory;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@ServiceProviderFor(AssemblerProcessorFactory.class)
public class ArchiveAssemblerProcessorFactory implements AssemblerProcessorFactory<Archive, ArchiveAssemblerProcessor> {
    @Override
    public String getName() {
        return Archive.NAME;
    }

    @Override
    public ArchiveAssemblerProcessor getAssemblerProcessor(JReleaserContext context) {
        return new ArchiveAssemblerProcessor(context);
    }
}