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
package org.jreleaser.templates;

import org.jreleaser.bundle.RB;
import org.jreleaser.util.JReleaserException;
import org.jreleaser.util.JReleaserLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public final class TemplateUtils {
    private static final Properties TEMPLATES_INVENTORY = new Properties();
    private static final String BASE_TEMPLATE_PREFIX = "META-INF/jreleaser/templates/";

    static {
        try {
            TEMPLATES_INVENTORY.load(TemplateUtils.class.getResourceAsStream("/META-INF/jreleaser/templates.properties"));
        } catch (IOException e) {
            // well this is awkward
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private TemplateUtils() {
        // noop
    }

    public static String trimTplExtension(String str) {
        if (str.endsWith(".tpl")) {
            return str.substring(0, str.length() - 4);
        }
        return str;
    }

    public static Map<String, Reader> resolveAndMergeTemplates(JReleaserLogger logger, String distributionType, String toolName, boolean snapshot, Path templateDirectory) {
        Map<String, Reader> templates = resolveTemplates(logger, distributionType, toolName, snapshot);
        if (null != templateDirectory && Files.exists(templateDirectory)) {
            templates.putAll(resolveTemplates(distributionType, toolName, snapshot, templateDirectory));
        }
        return templates;
    }

    public static Map<String, Reader> resolveTemplates(String distributionType, String toolName, boolean snapshot, Path templateDirectory) {
        Map<String, Reader> templates = new LinkedHashMap<>();

        Path snapshotTemplateDirectory = templateDirectory.resolveSibling(templateDirectory.getFileName() + "-snapshot");
        Path directory = templateDirectory;
        if (snapshot && snapshotTemplateDirectory.toFile().exists()) {
            directory = snapshotTemplateDirectory;
        }
        Path actualTemplateDirectory = directory;

        try {
            Files.walkFileTree(actualTemplateDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    templates.put(actualTemplateDirectory.relativize(file).toString(),
                        Files.newBufferedReader(file));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            String distributionTypeName = distributionType.toLowerCase().replace('_', '-');
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_templates_distribution",
                distributionTypeName, toolName, actualTemplateDirectory.toAbsolutePath()));
        }

        return templates;
    }

    public static Map<String, Reader> resolveTemplates(Path templateDirectory) {
        Map<String, Reader> templates = new LinkedHashMap<>();

        try {
            Files.walkFileTree(templateDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    templates.put(templateDirectory.relativize(file).toString(),
                        Files.newBufferedReader(file));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_templates_from", templateDirectory.toAbsolutePath()));
        }

        return templates;
    }

    public static Map<String, Reader> resolveTemplates(JReleaserLogger logger, String distributionType, String toolName, boolean snapshot) {
        String distributionTypeName = distributionType.toLowerCase().replace('_', '-');

        Map<String, Reader> templates = new LinkedHashMap<>();

        logger.debug(RB.$("templates.templates.resolve.classpath"));

        String templatePrefix = distributionTypeName + "." + toolName.toLowerCase() + (snapshot ? "-snapshot" : "");
        logger.debug(RB.$("templates.template.resolve.classpath", templatePrefix));
        String values = TEMPLATES_INVENTORY.getProperty(templatePrefix);
        if (isBlank(values) && snapshot) {
            templatePrefix = distributionTypeName + "." + toolName.toLowerCase();
            logger.debug(RB.$("templates.template.resolve.classpath", templatePrefix));
            values = TEMPLATES_INVENTORY.getProperty(templatePrefix);
        }

        if (isNotBlank(values)) {
            for (String k : values.split(",")) {
                templates.put(k, resolveTemplate(logger, distributionTypeName + "/" + toolName.toLowerCase() + "/" + k));
            }
        }

        return templates;
    }

    public static Reader resolveTemplate(JReleaserLogger logger, String templateKey) {
        logger.debug(RB.$("templates.template.resolve.classpath"), templateKey);

        try {
            InputStream inputStream = TemplateUtils.class.getClassLoader()
                .getResourceAsStream(BASE_TEMPLATE_PREFIX + templateKey);
            if (null == inputStream) {
                throw new JReleaserException(RB.$("ERROR_template_not_found", BASE_TEMPLATE_PREFIX + templateKey));
            }
            return new InputStreamReader(inputStream);
        } catch (Exception e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_template_for", templateKey, "classpath"));
        }
    }

    public static InputStream resolveResource(JReleaserLogger logger, String key) {
        logger.debug(RB.$("templates.resource.resolve.classpath"), key);

        try {
            InputStream inputStream = TemplateUtils.class.getClassLoader()
                .getResourceAsStream(key);
            if (null == inputStream) {
                throw new JReleaserException(RB.$("ERROR_resource_not_found", key));
            }
            return inputStream;
        } catch (Exception e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_resource_for", key, "classpath"), e);
        }
    }
}
