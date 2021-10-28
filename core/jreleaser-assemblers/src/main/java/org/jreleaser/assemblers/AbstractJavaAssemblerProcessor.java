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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JavaAssembler;
import org.jreleaser.model.Project;
import org.jreleaser.model.assembler.spi.AssemblerProcessingException;
import org.jreleaser.util.Constants;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.templates.TemplateUtils.resolveAndMergeTemplates;
import static org.jreleaser.util.MustacheUtils.applyTemplate;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
abstract class AbstractJavaAssemblerProcessor<A extends JavaAssembler> extends AbstractAssemblerProcessor<A> {
    protected AbstractJavaAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    public A getAssembler() {
        return assembler;
    }

    @Override
    public void setAssembler(A assembler) {
        this.assembler = assembler;
    }

    @Override
    public void assemble(Map<String, Object> props) throws AssemblerProcessingException {
        try {
            context.getLogger().debug(RB.$("tool.create.properties"), assembler.getType(), assembler.getName());
            Map<String, Object> newProps = fillProps(props);

            context.getLogger().debug(RB.$("tool.resolve.templates"), assembler.getType(), assembler.getName());
            Map<String, Reader> templates = resolveAndMergeTemplates(context.getLogger(),
                assembler.getType(),
                assembler.getType(),
                context.getModel().getProject().isSnapshot(),
                context.getBasedir().resolve(getAssembler().getTemplateDirectory()));

            for (Map.Entry<String, Reader> entry : templates.entrySet()) {
                context.getLogger().debug(RB.$("tool.evaluate.template"), entry.getKey(), assembler.getName(), assembler.getType());
                String content = applyTemplate(entry.getValue(), newProps, entry.getKey());
                context.getLogger().debug(RB.$("tool.write.template"), entry.getKey(), assembler.getName(), assembler.getType());
                writeFile(context.getModel().getProject(), content, newProps, entry.getKey());
            }

            Path assembleDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
            Files.createDirectories(assembleDirectory);

            doAssemble(newProps);
        } catch (IllegalArgumentException | IOException e) {
            throw new AssemblerProcessingException(e);
        }
    }

    protected abstract void writeFile(Project project, String content, Map<String, Object> props, String fileName) throws AssemblerProcessingException;
}
