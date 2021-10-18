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
package org.jreleaser.templates;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.util.JReleaserLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class TemplateUtils {
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
        if (null != templateDirectory && templateDirectory.toFile().exists()) {
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
        URL location = resolveLocation(TemplateUtils.class);
        if (null == location) {
            throw new JReleaserException(RB.$("ERROR_classpath_template_resolve"));
        }

        try {
            if ("file".equals(location.getProtocol())) {
                boolean templateFound = false;

                String templatePrefix = "META-INF/jreleaser/templates/" +
                    distributionTypeName + "/" + toolName.toLowerCase() +
                    (snapshot ? "-snapshot" : "") + "/";

                try (JarFile jarFile = new JarFile(new File(location.toURI()))) {
                    if (snapshot) {
                        templateFound = findTemplate(logger, jarFile, templatePrefix, templates);
                        if (!templateFound) {
                            templatePrefix = "META-INF/jreleaser/templates/" +
                                distributionTypeName + "/" + toolName.toLowerCase() + "/";
                            templateFound = findTemplate(logger, jarFile, templatePrefix, templates);
                        }
                    } else {
                        templateFound = findTemplate(logger, jarFile, templatePrefix, templates);
                    }
                }

                // if (!templateFound) {
                //     logger.error("templates for {}/{} were not found", distributionTypeName, toolName);
                // }
            } else {
                throw new JReleaserException(RB.$("ERROR_classpath_template_resolve"));
            }
        } catch (URISyntaxException | IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_templates_distribution",
                distributionTypeName, toolName, "classpath"));
        }

        return templates;
    }

    public static Reader resolveTemplate(JReleaserLogger logger, String announcerName) {
        logger.debug(RB.$("templates.templates.resolve.classpath"));
        URL location = resolveLocation(TemplateUtils.class);
        if (null == location) {
            throw new JReleaserException(RB.$("ERROR_classpath_template_resolve"));
        }

        try {
            if ("file".equals(location.getProtocol())) {
                boolean templateFound = false;

                String templateEntryName = "META-INF/jreleaser/templates/announcers/" + announcerName + ".tpl";
                JarFile jarFile = new JarFile(new File(location.toURI()));

                logger.debug(RB.$("templates.search_matching_multiple"), templateEntryName);
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
                    JarEntry entry = e.nextElement();
                    if (entry.isDirectory() || !entry.getName().equals(templateEntryName)) {
                        continue;
                    }

                    logger.debug(RB.$("templates.found"), templateEntryName);
                    return new InputStreamReader(jarFile.getInputStream(entry));
                }
            }
            throw new JReleaserException(RB.$("ERROR_classpath_template_resolve"));
        } catch (URISyntaxException | IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_templates_announcer", announcerName));
        }
    }

    private static boolean findTemplate(JReleaserLogger logger, JarFile jarFile, String templatePrefix, Map<String, Reader> templates) throws IOException {
        boolean templatesFound = false;

        logger.debug(RB.$("templates.search_matching_multiple"), templatePrefix);
        for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
            JarEntry entry = e.nextElement();
            if (entry.isDirectory() || !entry.getName().startsWith(templatePrefix)) {
                continue;
            }

            try (InputStreamReader in = new InputStreamReader(jarFile.getInputStream(entry))) {
                String templateName = entry.getName().substring(templatePrefix.length());
                templates.put(templateName, in);
                logger.debug(RB.$("templates.found"), templateName);
                templatesFound = true;
            }
        }

        return templatesFound;
    }

    public static Reader resolveTemplate(JReleaserLogger logger, Class<?> anchor, String templateKey) {
        logger.debug(RB.$("templates.template.resolve.classpath"), anchor.getName(), templateKey);
        URL location = resolveLocation(anchor);
        if (null == location) {
            throw new JReleaserException(RB.$("ERROR_classpath_template_resolve"));
        }

        try {
            if ("file".equals(location.getProtocol())) {
                JarFile jarFile = new JarFile(new File(location.toURI()));
                logger.debug(RB.$("templates.search_matching"), templateKey);
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
                    JarEntry entry = e.nextElement();
                    if (entry.isDirectory() || !entry.getName().equals(templateKey)) {
                        continue;
                    }

                    logger.debug(RB.$("templates.found"), templateKey);
                    return new InputStreamReader(jarFile.getInputStream(entry));
                }
                throw new JReleaserException(RB.$("ERROR_template_not_found", anchor.getName(), templateKey));
            } else {
                throw new JReleaserException(RB.$("ERROR_classpath_template_resolve"));
            }
        } catch (URISyntaxException | IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_reading_template_for",
                anchor.getName() + "@" + templateKey, "classpath"));
        }
    }

    private static URL resolveLocation(Class<?> klass) {
        if (klass == null) return null;

        try {
            URL codeSourceLocation = klass.getProtectionDomain()
                .getCodeSource()
                .getLocation();
            if (codeSourceLocation != null) return codeSourceLocation;
        } catch (SecurityException | NullPointerException ignored) {
            // noop
        }

        URL classResource = klass.getResource(klass.getSimpleName() + ".class");
        if (classResource == null) return null;

        String url = classResource.toString();
        String suffix = klass.getCanonicalName().replace('.', '/') + ".class";
        if (!url.endsWith(suffix)) return null;
        String path = url.substring(0, url.length() - suffix.length());

        if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

        try {
            return new URL(path);
        } catch (MalformedURLException ignored) {
            return null;
        }
    }
}
