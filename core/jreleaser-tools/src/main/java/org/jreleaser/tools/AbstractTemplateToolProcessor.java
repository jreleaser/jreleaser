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
package org.jreleaser.tools;

import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.TemplateTool;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.templates.TemplateUtils.resolveAndMergeTemplates;
import static org.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;
import static org.jreleaser.util.FileUtils.grantFullAccess;
import static org.jreleaser.util.MustacheUtils.applyTemplate;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
abstract class AbstractTemplateToolProcessor<T extends TemplateTool> extends AbstractToolProcessor<T> {
    protected AbstractTemplateToolProcessor(JReleaserContext context) {
        super(context);
    }

    protected void doPrepareDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        try {
            doPrepareDistribution(distribution, props, distribution.getName(),
                getPrepareDirectory(props), getTool().getTemplateDirectory(), getToolName());
        } catch (IOException e) {
            throw new ToolProcessingException(e);
        }
    }


    protected void doPrepareDistribution(Distribution distribution,
                                         Map<String, Object> props,
                                         String distributionName,
                                         Path prepareDirectory,
                                         String templateDirectory,
                                         String toolName) throws IOException, ToolProcessingException {
        // cleanup from previous session
        FileUtils.deleteFiles(prepareDirectory);
        Files.createDirectories(prepareDirectory);

        context.getLogger().debug("resolving templates for {}/{}", distributionName, toolName);
        Map<String, Reader> templates = resolveAndMergeTemplates(context.getLogger(),
            distribution.getType().name(),
            // leave this one be!
            getToolName(),
            context.getModel().getProject().isSnapshot(),
            context.getBasedir().resolve(templateDirectory));

        for (Map.Entry<String, Reader> entry : templates.entrySet()) {
            String filename = entry.getKey();
            if (filename.endsWith(".tpl")) {
                context.getLogger().debug("evaluating template {} for {}/{}", filename, distributionName, toolName);
                String content = applyTemplate(entry.getValue(), props);
                context.getLogger().debug("writing template {} for {}/{}", filename, distributionName, toolName);
                writeFile(context.getModel().getProject(), distribution, content, props, prepareDirectory, filename);
            } else {
                context.getLogger().debug("writing file {} for {}/{}", filename, distributionName, toolName);
                writeFile(entry.getValue(), prepareDirectory.resolve(filename));
            }
        }

        context.getLogger().debug("copying license files");
        FileUtils.copyFiles(context.getLogger(),
            context.getBasedir(),
            prepareDirectory, path -> path.getFileName().startsWith("LICENSE"));
    }

    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        doPackageDistribution(distribution, props, getPackageDirectory(props));
    }

    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        try {
            // cleanup from previous session
            FileUtils.deleteFiles(packageDirectory);
            Files.createDirectories(packageDirectory);
        } catch (IOException e) {
            throw new ToolProcessingException(e);
        }
    }

    protected abstract void writeFile(Project project, Distribution distribution, String content, Map<String, Object> props, Path outputDirectory, String fileName) throws ToolProcessingException;

    protected void writeFile(Reader reader, Path outputFile) throws ToolProcessingException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Scanner scanner = new Scanner(reader);
            scanner.useDelimiter("\\Z");
            Files.write(outputFile, scanner.next().getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);
            scanner.close();
            grantFullAccess(outputFile);
        } catch (IOException e) {
            throw new ToolProcessingException("Unexpected error when writing to " + outputFile.toAbsolutePath(), e);
        }
    }

    protected void writeFile(String content, Path outputFile) throws ToolProcessingException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (IOException e) {
            throw new ToolProcessingException("Unexpected error when writing to " + outputFile.toAbsolutePath(), e);
        }
    }
}
