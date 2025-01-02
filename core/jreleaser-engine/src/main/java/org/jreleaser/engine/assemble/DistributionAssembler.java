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
package org.jreleaser.engine.assemble;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.model.spi.assemble.AssemblerProcessor;
import org.jreleaser.mustache.TemplateContext;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class DistributionAssembler {
    private final JReleaserContext context;
    private final Assembler<?> assembler;
    private final Path outputDirectory;

    private DistributionAssembler(JReleaserContext context,
                                  Assembler<?> assembler) {
        this.context = context;
        this.assembler = assembler;
        this.outputDirectory = context.getOutputDirectory();
    }

    public Assembler<?> getAssembler() {
        return assembler;
    }

    public boolean assemble() throws AssemblerProcessingException {
        if (!assembler.isEnabled()) {
            context.getLogger().debug(RB.$("assemblers.distribution.skip"), assembler.getName());
            return false;
        }

        AssemblerProcessor<?, ?> assemblerProcessor = AssemblerProcessors.findProcessor(context, assembler);

        context.getLogger().info(RB.$("assemblers.distribution.assemble"), assembler.getName());

        assemblerProcessor.assemble(initProps());
        return true;
    }

    private TemplateContext initProps() {
        TemplateContext props = context.props();
        props.set(Constants.KEY_BASEDIR, context.getBasedir());
        props.set(Constants.KEY_BASE_OUTPUT_DIRECTORY, outputDirectory.getParent());
        props.set(Constants.KEY_OUTPUT_DIRECTORY, outputDirectory);
        props.set(Constants.KEY_ASSEMBLE_DIRECTORY, context.getAssembleDirectory());
        props.set(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY, context.getAssembleDirectory()
            .resolve(assembler.getName())
            .resolve(assembler.getType()));
        return props;
    }

    public static DistributionAssemblerBuilder builder() {
        return new DistributionAssemblerBuilder();
    }

    public static class DistributionAssemblerBuilder {
        private JReleaserContext context;
        private Assembler<?> assembler;

        public DistributionAssemblerBuilder context(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
            return this;
        }

        public DistributionAssemblerBuilder assembler(Assembler<?> assembler) {
            this.assembler = requireNonNull(assembler, "'assembler' must not be null");
            return this;
        }

        public DistributionAssembler build() {
            requireNonNull(context, "'context' must not be null");
            requireNonNull(assembler, "'assembler' must not be null");
            return new DistributionAssembler(context, assembler);
        }
    }
}
