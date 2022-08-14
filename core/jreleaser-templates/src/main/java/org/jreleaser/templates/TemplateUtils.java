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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Locale;
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
    private static final String TPL = ".tpl";

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
        if (str.endsWith(TPL)) {
            return str.substring(0, str.length() - 4);
        }
        return str;
    }

    public static Map<String, TemplateResource> resolveAndMergeTemplates(JReleaserLogger logger, String distributionType, String toolName, boolean snapshot, Path templateDirectory) {
        Map<String, TemplateResource> templates = resolveTemplates(logger, distributionType, toolName, snapshot);
        if (null != templateDirectory && Files.exists(templateDirectory)) {
            templates.putAll(resolveTemplates(distributionType, toolName, snapshot, templateDirectory));
        }
        return templates;
    }

    public static Map<String, TemplateResource> resolveTemplates(String distributionType, String toolName, boolean snapshot, Path templateDirectory) {
        Map<String, TemplateResource> templates = new LinkedHashMap<>();

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
                        asResource(file));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            String distributionTypeName = distributionType.toLowerCase(Locale.ENGLISH).replace('_', '-');
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_templates_distribution",
                distributionTypeName, toolName, actualTemplateDirectory.toAbsolutePath()));
        }

        return templates;
    }

    public static Map<String, TemplateResource> resolveTemplates(Path templateDirectory) {
        Map<String, TemplateResource> templates = new LinkedHashMap<>();

        try {
            Files.walkFileTree(templateDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    templates.put(templateDirectory.relativize(file).toString(),
                        asResource(file));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_templates_from", templateDirectory.toAbsolutePath()));
        }

        return templates;
    }

    private static TemplateResource asResource(Path file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file.toFile());
        if (file.getFileName().toString().endsWith(TPL)) {
            return new ReaderTemplateResource(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }

        return new InputStreamTemplateResource(inputStream);
    }

    public static Map<String, TemplateResource> resolveTemplates(JReleaserLogger logger, String distributionType, String toolName, boolean snapshot) {
        String distributionTypeName = distributionType.toLowerCase(Locale.ENGLISH).replace('_', '-');

        Map<String, TemplateResource> templates = new LinkedHashMap<>();

        logger.debug(RB.$("templates.templates.resolve.classpath"));

        String templatePrefix = distributionTypeName + "." + toolName.toLowerCase(Locale.ENGLISH) + (snapshot ? "-snapshot" : "");
        logger.debug(RB.$("templates.template.resolve.classpath", templatePrefix));
        String values = TEMPLATES_INVENTORY.getProperty(templatePrefix);
        if (isBlank(values) && snapshot) {
            templatePrefix = distributionTypeName + "." + toolName.toLowerCase(Locale.ENGLISH);
            logger.debug(RB.$("templates.template.resolve.classpath", templatePrefix));
            values = TEMPLATES_INVENTORY.getProperty(templatePrefix);
        }

        if (isNotBlank(values)) {
            for (String k : values.split(",")) {
                templates.put(k, resolveTemplate(logger, distributionTypeName + "/" + toolName.toLowerCase(Locale.ENGLISH) + "/" + k));
            }
        }

        return templates;
    }

    public static TemplateResource resolveTemplate(JReleaserLogger logger, String templateKey) {
        logger.debug(RB.$("templates.template.resolve.classpath"), templateKey);

        try {
            InputStream inputStream = TemplateUtils.class.getClassLoader()
                .getResourceAsStream(BASE_TEMPLATE_PREFIX + templateKey);
            if (null == inputStream) {
                throw new JReleaserException(RB.$("ERROR_template_not_found", BASE_TEMPLATE_PREFIX + templateKey));
            }
            return templateKey.endsWith(TPL) ? new ReaderTemplateResource(new InputStreamReader(inputStream, StandardCharsets.UTF_8)) : new InputStreamTemplateResource(inputStream);
        } catch (Exception e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_template_for", templateKey, "classpath"));
        }
    }

    public static TemplateResource resolveResource(JReleaserLogger logger, String key) {
        logger.debug(RB.$("templates.resource.resolve.classpath"), key);

        try {
            InputStream inputStream = TemplateUtils.class.getClassLoader()
                .getResourceAsStream(key);
            if (null == inputStream) {
                throw new JReleaserException(RB.$("ERROR_resource_not_found", key));
            }
            return new InputStreamTemplateResource(inputStream);
        } catch (Exception e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_resource_for", key, "classpath"), e);
        }
    }
}
