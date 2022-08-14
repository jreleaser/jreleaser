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
package org.jreleaser.assemblers;

import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Glob;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JavaAssembler;
import org.jreleaser.model.Project;
import org.jreleaser.model.assembler.spi.AssemblerProcessingException;
import org.jreleaser.templates.TemplateResource;
import org.jreleaser.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
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
            context.getLogger().debug(RB.$("packager.create.properties"), assembler.getType(), assembler.getName());
            Map<String, Object> newProps = fillProps(props);

            context.getLogger().debug(RB.$("packager.resolve.templates"), assembler.getType(), assembler.getName());
            Map<String, TemplateResource> templates = resolveAndMergeTemplates(context.getLogger(),
                assembler.getType(),
                assembler.getType(),
                context.getModel().getProject().isSnapshot(),
                context.getBasedir().resolve(getAssembler().getTemplateDirectory()));

            for (Map.Entry<String, TemplateResource> entry : templates.entrySet()) {
                String key = entry.getKey();
                TemplateResource value = entry.getValue();

                if (value.isReader()) {
                    context.getLogger().debug(RB.$("packager.evaluate.template"), key, assembler.getName(), assembler.getType());
                    String content = applyTemplate(value.getReader(), newProps, key);
                    context.getLogger().debug(RB.$("packager.write.template"), key, assembler.getName(), assembler.getType());
                    writeFile(context.getModel().getProject(), content, newProps, key);
                } else {
                    context.getLogger().debug(RB.$("packager.write.template"), key, assembler.getName(), assembler.getType());
                    writeFile(context.getModel().getProject(), IOUtils.toByteArray(value.getInputStream()), newProps, key);
                }
            }

            Path assembleDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
            Files.createDirectories(assembleDirectory);

            doAssemble(newProps);
        } catch (IllegalArgumentException | IOException e) {
            throw new AssemblerProcessingException(e);
        }
    }

    protected Set<Path> copyFiles(JReleaserContext context, Path destination) throws AssemblerProcessingException {
        Set<Path> paths = new LinkedHashSet<>();

        // resolve all first
        for (Glob glob : assembler.getFiles()) {
            glob.getResolvedArtifacts(context).stream()
                .map(artifact -> artifact.getResolvedPath(context, assembler))
                .forEach(paths::add);
        }

        // copy all next
        try {
            Files.createDirectories(destination);
            for (Path path : paths) {
                context.getLogger().debug(RB.$("assembler.copying"), path.getFileName());
                Files.copy(path, destination.resolve(path.getFileName()), REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_copying_files"), e);
        }

        return paths;
    }

    protected abstract void writeFile(Project project, String content, Map<String, Object> props, String fileName) throws AssemblerProcessingException;

    protected void writeFile(Project project, byte[] content, Map<String, Object> props, String fileName) throws AssemblerProcessingException {
        Path outputDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path inputsDirectory = outputDirectory.resolve("inputs");
        try {
            Files.createDirectories(inputsDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_create_directories"), e);
        }

        Path outputFile = inputsDirectory.resolve(fileName);
        writeFile(content, outputFile);
    }
}
