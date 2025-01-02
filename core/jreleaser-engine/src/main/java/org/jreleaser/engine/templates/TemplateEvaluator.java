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
package org.jreleaser.engine.templates;

import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.engine.changelog.Changelog;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.Constants;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.templates.TemplateResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.bundle.RB.$;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.passThrough;
import static org.jreleaser.templates.TemplateUtils.resolveTemplate;
import static org.jreleaser.templates.TemplateUtils.resolveTemplates;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;
import static org.jreleaser.util.FileUtils.grantFullAccess;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public class TemplateEvaluator {
    private TemplateEvaluator() {
        // noop
    }

    public static Path generateTemplate(JReleaserContext context, Path templatePath, Path targetDirectory, boolean overwrite) throws JReleaserException {
        if (!Files.exists(templatePath)) {
            throw fail(RB.$("validation_directory_not_exist", "templatePath", templatePath));
        }

        if (!Files.isRegularFile(templatePath)) {
            throw fail(RB.$("validation_is_not_a_file", "templatePath", templatePath));
        }


        try {
            context.getLogger().info(RB.$("templates.create.directory"), targetDirectory.toAbsolutePath());
            Files.createDirectories(targetDirectory);

            TemplateResource template = resolveTemplate(templatePath);
            TemplateContext props = setupTemplateContext(context);

            String key = templatePath.getFileName().toString();

            Path targetTemplate = targetDirectory.resolve(trimTplExtension(key));
            if (template.isReader()) {
                context.getLogger().info(RB.$("templates.evaluate"), key);
                String content = applyTemplate(template.getReader(), props, key);
                context.getLogger().info(RB.$("templates.writing.file"), targetTemplate);
                writeFile(context.getLogger(), content, targetTemplate, overwrite);
            } else {
                context.getLogger().info(RB.$("templates.writing.file"), targetTemplate);
                writeFile(context.getLogger(), IOUtils.toByteArray(template.getInputStream()), targetTemplate, overwrite);
            }

        } catch (IOException e) {
            throw fail(e);
        }

        context.getLogger().info(RB.$("templates.evaluate.single.done", templatePath, targetDirectory));
        return targetDirectory;
    }

    public static Path generateTemplates(JReleaserContext context, Path inputDirectory, Path targetDirectory, boolean overwrite) throws JReleaserException {
        if (!Files.exists(inputDirectory)) {
            throw fail(RB.$("validation_directory_not_exist", "inputDirectory", inputDirectory));
        }

        if (!Files.isDirectory(inputDirectory)) {
            throw fail(RB.$("validation_is_not_a_directory", "inputDirectory", inputDirectory));
        }

        try {
            File inputDirectoryFile = inputDirectory.toFile();
            if (null == inputDirectoryFile.listFiles() || inputDirectoryFile.listFiles().length == 0) {
                throw fail(RB.$("validation_directory_is_empty", "inputDirectory", inputDirectory));
            }

            context.getLogger().info(RB.$("templates.create.directory"), targetDirectory.toAbsolutePath());
            Files.createDirectories(targetDirectory);

            Map<String, TemplateResource> templates = resolveTemplates(inputDirectory);
            TemplateContext props = setupTemplateContext(context);

            for (Map.Entry<String, TemplateResource> entry : templates.entrySet()) {
                String key = entry.getKey();
                TemplateResource value = entry.getValue();

                Path targetTemplate = targetDirectory.resolve(trimTplExtension(key));
                if (value.isReader()) {
                    context.getLogger().info(RB.$("templates.evaluate"), key);
                    String content = applyTemplate(value.getReader(), props, key);
                    context.getLogger().info(RB.$("templates.writing.file"), targetTemplate);
                    writeFile(context.getLogger(), content, targetTemplate, overwrite);
                } else {
                    context.getLogger().info(RB.$("templates.writing.file"), targetTemplate);
                    writeFile(context.getLogger(), IOUtils.toByteArray(value.getInputStream()), targetTemplate, overwrite);
                }
            }
        } catch (IOException e) {
            throw fail(e);
        }

        context.getLogger().info(RB.$("templates.evaluate.multiple.done", inputDirectory, targetDirectory));
        return targetDirectory;
    }

    private static TemplateContext setupTemplateContext(JReleaserContext context) {
        TemplateContext props = context.fullProps();

        if (context.getMode() == org.jreleaser.model.api.JReleaserContext.Mode.ANNOUNCE ||
            context.getMode() == org.jreleaser.model.api.JReleaserContext.Mode.CHANGELOG ||
            context.getMode() == org.jreleaser.model.api.JReleaserContext.Mode.CONFIG ||
            context.getMode() == org.jreleaser.model.api.JReleaserContext.Mode.FULL) {
            String resolvedChangelog = Changelog.createChangelog(context);
            context.getChangelog().setResolvedChangelog(resolvedChangelog);
            props.set(Constants.KEY_CHANGELOG_CONTENT, passThrough(resolvedChangelog));
            props.set(Constants.KEY_CHANGELOG_CHANGES, passThrough(context.getChangelog().getFormattedChanges()));
            props.set(Constants.KEY_CHANGELOG_CONTRIBUTORS, passThrough(context.getChangelog().getFormattedContributors()));
        }

        return props;
    }

    private static void writeFile(JReleaserLogger logger, String content, Path outputFile, boolean overwrite) {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content.getBytes(UTF_8), overwrite ? CREATE : CREATE_NEW, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (FileAlreadyExistsException e) {
            logger.error($("jreleaser.ERROR_file_exists"), outputFile.toAbsolutePath());
        } catch (IOException e) {
            throw fail(RB.$("ERROR_unexpected_error_writing_file", outputFile.toAbsolutePath()), e);
        }
    }

    private static void writeFile(JReleaserLogger logger, byte[] content, Path outputFile, boolean overwrite) {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content, overwrite ? CREATE : CREATE_NEW, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (FileAlreadyExistsException e) {
            logger.error($("jreleaser.ERROR_file_exists"), outputFile.toAbsolutePath());
        } catch (IOException e) {
            throw fail(RB.$("ERROR_unexpected_error_writing_file", outputFile.toAbsolutePath()), e);
        }
    }

    private static JReleaserException fail(String msg) throws JReleaserException {
        throw new JReleaserException(msg);
    }

    private static JReleaserException fail(String msg, Exception e) throws JReleaserException {
        throw new JReleaserException(msg, e);
    }

    private static JReleaserException fail(Exception e) throws JReleaserException {
        throw new JReleaserException(RB.$("ERROR_unexpected_template_fail2"), e);
    }
}
