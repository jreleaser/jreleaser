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
package org.jreleaser.packagers;

import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.TemplatePackager;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.templates.TemplateResource;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.model.Constants.SKIP_LICENSE_FILE;
import static org.jreleaser.templates.TemplateUtils.resolveAndMergeTemplates;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;
import static org.jreleaser.util.FileUtils.grantFullAccess;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public abstract class AbstractTemplatePackagerProcessor<T extends TemplatePackager<?>> extends AbstractPackagerProcessor<T> {
    protected AbstractTemplatePackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        try {
            doPrepareDistribution(distribution, props, distribution.getName(),
                getPrepareDirectory(props), getPackager().getTemplateDirectory(), getPackagerName(), true);
        } catch (IOException e) {
            throw new PackagerProcessingException(e);
        }
    }

    protected void doPrepareDistribution(Distribution distribution,
                                         TemplateContext props,
                                         String distributionName,
                                         Path prepareDirectory,
                                         String templateDirectory,
                                         String packagerName,
                                         boolean copyLicense) throws IOException, PackagerProcessingException {
        // cleanup from previous session
        FileUtils.deleteFiles(prepareDirectory);
        Files.createDirectories(prepareDirectory);

        context.getLogger().debug(RB.$("packager.resolve.templates"), distributionName, packagerName);
        Map<String, TemplateResource> templates = resolveAndMergeTemplates(context.getLogger(),
            distribution.getType().name(),
            // leave this one be!
            getPackagerName(),
            context.getModel().getProject().isSnapshot(),
            context.getBasedir().resolve(templateDirectory));

        for (Map.Entry<String, TemplateResource> entry : templates.entrySet()) {
            String filename = entry.getKey();
            if (isSkipped(filename)) {
                context.getLogger().debug(RB.$("packager.skipped.template"), filename, distributionName, packagerName);
                continue;
            }

            TemplateResource value = entry.getValue();
            if (value.isReader()) {
                context.getLogger().debug(RB.$("packager.evaluate.template"), filename, distributionName, packagerName);
                String content = applyTemplate(filename, value.getReader(), props);
                if (!content.endsWith(System.lineSeparator())) {
                    content += System.lineSeparator();
                }
                context.getLogger().debug(RB.$("packager.write.template"), filename, distributionName, packagerName);
                writeFile(distribution, content, props, prepareDirectory, filename);
            } else {
                context.getLogger().debug(RB.$("packager.write.file"), filename, distributionName, packagerName);
                writeFile(distribution, value.getInputStream(), props, prepareDirectory, filename);
            }
        }

        if (copyLicense && isFalse(packager.getExtraProperties().get(SKIP_LICENSE_FILE))) {
            context.getLogger().debug(RB.$("packager.copy.license"));
            FileUtils.copyFiles(context.getLogger(),
                context.getBasedir(),
                prepareDirectory, path -> path.getFileName().startsWith("LICENSE"));
        }
    }

    protected String applyTemplate(String fileName, Reader reader, TemplateContext props) {
        return MustacheUtils.applyTemplate(reader, props);
    }

    public boolean isSkipped(String filename) {
        // check explicit match
        if (packager.getSkipTemplates().contains(filename)) return true;
        // check using string contains
        if (packager.getSkipTemplates().stream()
            .anyMatch(filename::contains)) return true;
        // check using regex
        if (packager.getSkipTemplates().stream()
            .anyMatch(filename::matches)) return true;

        // remove .tpl and check again
        String fname = trimTplExtension(filename);

        // check explicit match
        if (packager.getSkipTemplates().contains(fname)) return true;
        // check using string contains
        if (packager.getSkipTemplates().stream()
            .anyMatch(fname::contains)) return true;
        // check using regex
        return packager.getSkipTemplates().stream()
            .anyMatch(fname::matches);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        doPackageDistribution(distribution, props, getPackageDirectory(props));
    }

    protected void doPackageDistribution(Distribution distribution, TemplateContext props, Path packageDirectory) throws PackagerProcessingException {
        try {
            // cleanup from previous session
            FileUtils.deleteFiles(packageDirectory);
            Files.createDirectories(packageDirectory);
        } catch (IOException e) {
            throw new PackagerProcessingException(e);
        }
    }

    protected abstract void writeFile(Distribution distribution, String content, TemplateContext props, Path outputDirectory, String fileName) throws PackagerProcessingException;

    protected void writeFile(Distribution distribution, InputStream inputStream, TemplateContext props, Path outputDirectory, String fileName) throws PackagerProcessingException {
        Path outputFile = outputDirectory.resolve(fileName);

        writeFile(inputStream, outputFile);
    }

    protected void writeFile(Reader reader, Path outputFile) throws PackagerProcessingException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, IOUtils.toByteArray(reader, StandardCharsets.UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (Exception e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_error_writing_file", outputFile.toAbsolutePath()), e);
        }
    }

    protected void writeFile(InputStream inputStream, Path outputFile) throws PackagerProcessingException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, IOUtils.toByteArray(inputStream), CREATE, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (Exception e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_error_writing_file", outputFile.toAbsolutePath()), e);
        }
    }

    protected void writeFile(byte[] content, Path outputFile) throws PackagerProcessingException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content, CREATE, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (Exception e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_error_writing_file", outputFile.toAbsolutePath()), e);
        }
    }

    protected void writeFile(String content, Path outputFile) throws PackagerProcessingException {
        writeFile(content.getBytes(UTF_8), outputFile);
    }
}
