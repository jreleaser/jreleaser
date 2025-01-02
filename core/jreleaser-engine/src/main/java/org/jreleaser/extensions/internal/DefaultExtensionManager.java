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
package org.jreleaser.extensions.internal;

import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.Extension;
import org.jreleaser.extensions.api.ExtensionManager;
import org.jreleaser.extensions.api.ExtensionPoint;
import org.jreleaser.extensions.api.workflow.WorkflowListener;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.tool.Mvn;
import org.jreleaser.sdk.tool.ToolException;
import org.jreleaser.templates.TemplateResource;
import org.jreleaser.templates.TemplateUtils;
import org.jreleaser.util.DefaultVersions;
import org.jreleaser.util.FileUtils;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
@ServiceProviderFor(ExtensionManager.class)
public final class DefaultExtensionManager implements ExtensionManager {
    private final Map<String, ExtensionDef> extensionDefs = new LinkedHashMap<>();
    private final Set<ExtensionPoint> allExtensionPoints = new LinkedHashSet<>();
    private final Map<String, Set<ExtensionPoint>> extensionPoints = new LinkedHashMap<>();

    public ExtensionBuilder configureExtension(String name) {
        return new ExtensionBuilder(name, this);
    }

    public void load(JReleaserContext context) {
        extensionPoints.clear();
        allExtensionPoints.clear();

        Set<String> visitedExtensionNames = new LinkedHashSet<>();
        Set<String> visitedExtensionTypes = new LinkedHashSet<>();

        // load defaults
        for (Extension extension : resolveServiceLoader()) {
            processExtension(context, extension, visitedExtensionNames, visitedExtensionTypes);
        }

        for (Map.Entry<String, ExtensionDef> e : extensionDefs.entrySet()) {
            String extensionName = e.getKey();
            ExtensionDef extensionDef = e.getValue();
            if (visitedExtensionNames.contains(extensionName)) {
                continue;
            }

            if (!extensionDef.isEnabled()) {
                context.getLogger().debug(RB.$("extension.manager.disabled", extensionName));
                return;
            }

            createClassLoader(context, extensionDef).ifPresent(classLoader -> {
                for (Extension extension : ServiceLoader.load(Extension.class, classLoader)) {
                    processExtension(context, extension, visitedExtensionNames, visitedExtensionTypes);
                }
            });
        }

        context.setWorkflowListeners(findExtensionPoints(WorkflowListener.class));
    }

    @Override
    public <T extends ExtensionPoint> Set<T> findExtensionPoints(Class<T> extensionPointType) {
        return (Set<T>) extensionPoints.computeIfAbsent(extensionPointType.getName(), k -> {
            Set<T> set = new LinkedHashSet<>();

            for (ExtensionPoint extensionPoint : allExtensionPoints) {
                if (extensionPointType.isAssignableFrom(extensionPoint.getClass())) {
                    set.add((T) extensionPoint);
                }
            }

            return Collections.unmodifiableSet(set);
        });
    }

    private Optional<ClassLoader> createClassLoader(JReleaserContext context, ExtensionDef extensionDef) {
        String directory = extensionDef.getDirectory();

        if (isNotBlank(extensionDef.getGav())) {
            directory = resolveJARs(context, extensionDef);
        }

        Path directoryPath = Paths.get(directory);
        if (!directoryPath.isAbsolute()) {
            directoryPath = context.getBasedir().resolve(directoryPath);
        }

        if (!Files.exists(directoryPath)) {
            context.getLogger().warn(RB.$("extension.manager.load.directory.missing", extensionDef.getName(), directoryPath.toAbsolutePath()));
            return Optional.empty();
        }

        List<Path> jars = null;
        try (Stream<Path> jarPaths = Files.list(directoryPath)) {
            jars = jarPaths
                .filter(path -> path.getFileName().toString().endsWith(".jar"))
                .collect(toList());
        } catch (IOException e) {
            context.getLogger().trace(e);
            context.getLogger().warn(RB.$("extension.manager.load.directory.error", extensionDef.getName(), directoryPath.toAbsolutePath()));
            return Optional.empty();
        }

        if (jars.isEmpty()) {
            context.getLogger().warn(RB.$("extension.manager.load.empty.jars", extensionDef.getName(), directoryPath.toAbsolutePath()));
            return Optional.empty();
        }

        URL[] urls = new URL[jars.size()];
        for (int i = 0; i < jars.size(); i++) {
            Path jar = jars.get(i);
            try {
                urls[i] = jar.toUri().toURL();
            } catch (MalformedURLException e) {
                context.getLogger().trace(e);
                context.getLogger().warn(RB.$("extension.manager.load.jar.error", extensionDef.getName(), jar.toAbsolutePath()));
                return Optional.empty();
            }
        }

        return Optional.of(new URLClassLoader(urls, getClass().getClassLoader()));
    }

    private String resolveJARs(JReleaserContext context, ExtensionDef extensionDef) {
        Path target = context.getOutputDirectory().resolve("extensions")
            .resolve(extensionDef.getName())
            .toAbsolutePath();

        Mvn mvn = new Mvn(context.asImmutable(), DefaultVersions.getInstance().getMvnVersion());

        try {
            if (!mvn.setup()) {
                throw new JReleaserException(RB.$("tool_unavailable", "mvn"));
            }
        } catch (ToolException e) {
            throw new JReleaserException(RB.$("tool_unavailable", "mvn"), e);
        }

        try {
            FileUtils.deleteFiles(target, true);

            Path pom = Files.createTempFile("jreleaser-extensions", "pom.xml");

            TemplateResource template = TemplateUtils.resolveTemplate(context.getLogger(), "extensions/pom.xml.tpl");

            String[] gav = extensionDef.getGav().split(":");

            String content = IOUtils.toString(template.getReader());
            content = content.replace("@groupId@", gav[0])
                .replace("@artifactId@", gav[1])
                .replace("@version@", gav[2]);

            Files.write(pom, content.getBytes(UTF_8), WRITE, TRUNCATE_EXISTING);

            List<String> args = new ArrayList<>();
            args.add("-B");
            args.add("-q");
            args.add("-f");
            args.add(pom.toAbsolutePath().toString());
            args.add("dependency:resolve");
            // resolve
            context.getLogger().debug(RB.$("extension.manager.resolve.jars", extensionDef.getGav()));
            mvn.invoke(context.getBasedir(), args);

            args.clear();
            args.add("-B");
            args.add("-q");
            args.add("-f");
            args.add(pom.toAbsolutePath().toString());
            args.add("dependency:copy-dependencies");
            args.add("-DoutputDirectory=" + target);
            // copy
            context.getLogger().debug(RB.$("extension.manager.copy.jars", extensionDef.getGav(), context.relativizeToBasedir(target)));
            mvn.invoke(context.getBasedir(), args);
        } catch (IOException | CommandException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        }

        return target.toString();
    }

    private void processExtension(JReleaserContext context, Extension extension, Set<String> visitedExtensionNames, Set<String> visitedExtensionTypes) {
        String extensionName = extension.getName();
        String extensionType = extension.getClass().getName();

        if (visitedExtensionNames.contains(extensionName) || visitedExtensionTypes.contains(extensionType)) {
            return;
        }

        context.getLogger().debug(RB.$("extension.manager.load", extensionName, extensionType));
        visitedExtensionNames.add(extensionName);
        visitedExtensionTypes.add(extensionType);

        ExtensionDef extensionDef = extensionDefs.get(extensionName);

        if (null != extensionDef && !extensionDef.isEnabled()) {
            context.getLogger().debug(RB.$("extension.manager.disabled", extensionName));
            return;
        }

        for (ExtensionPoint extensionPoint : extension.provides()) {
            String extensionPointTypeName = extensionPoint.getClass().getName();
            if (null != extensionDef && extensionDef.getExtensionPoints().containsKey(extensionPointTypeName)) {
                extensionPoint.init(context.asImmutable(), extensionDef.getExtensionPoints().get(extensionPointTypeName)
                    .getProperties());
            } else {
                extensionPoint.init(context.asImmutable(), Collections.emptyMap());
            }
            context.getLogger().debug(RB.$("extension.manager.add.extension.point", extensionPointTypeName, extensionName));
            allExtensionPoints.add(extensionPoint);
        }
    }

    private static ServiceLoader<Extension> resolveServiceLoader() {
        // Check if the type.classLoader works
        ServiceLoader<Extension> handlers = ServiceLoader.load(Extension.class, Extension.class.getClassLoader());
        if (handlers.iterator().hasNext()) {
            return handlers;
        }

        // If *nothing* else works
        return ServiceLoader.load(Extension.class);
    }

    private static class ExtensionDef {
        private final String name;
        private final String gav;
        private final String directory;
        private final boolean enabled;
        private final Map<String, ExtensionPointDef> extensionPoints = new LinkedHashMap<>();

        private ExtensionDef(String name, String directory, String gav, boolean enabled, Map<String, ExtensionPointDef> extensionPoints) {
            this.name = name;
            this.gav = gav;
            this.directory = directory;
            this.enabled = enabled;
            this.extensionPoints.putAll(extensionPoints);
        }

        private String getName() {
            return name;
        }

        public String getGav() {
            return gav;
        }

        private String getDirectory() {
            return directory;
        }

        private boolean isEnabled() {
            return enabled;
        }

        private Map<String, ExtensionPointDef> getExtensionPoints() {
            return extensionPoints;
        }
    }

    private static class ExtensionPointDef {
        private final String type;
        private final Map<String, Object> properties = new LinkedHashMap<>();

        private ExtensionPointDef(String type, Map<String, Object> properties) {
            this.type = type;
            this.properties.putAll(properties);
        }

        private String getType() {
            return type;
        }

        private Map<String, Object> getProperties() {
            return properties;
        }
    }

    public static class ExtensionBuilder {
        private final Map<String, ExtensionPointDef> extensionPoints = new LinkedHashMap<>();
        private final String name;
        private final DefaultExtensionManager defaultExtensionManager;
        private String gav;
        private String directory;
        private boolean enabled;

        public ExtensionBuilder(String name, DefaultExtensionManager defaultExtensionManager) {
            this.name = name;
            this.defaultExtensionManager = defaultExtensionManager;

            String jreleaserHome = System.getenv("JRELEASER_USER_HOME");
            if (isBlank(jreleaserHome)) {
                jreleaserHome = System.getProperty("user.home") + File.separator + ".jreleaser";
            }
            Path baseExtensionsDirectory = Paths.get(jreleaserHome).resolve("extensions");
            this.directory = baseExtensionsDirectory.resolve(name).toAbsolutePath().toString();
        }

        public ExtensionBuilder withGav(String gav) {
            this.gav = gav;
            return this;
        }

        public ExtensionBuilder withDirectory(String directory) {
            this.directory = directory;
            return this;
        }

        public ExtensionBuilder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ExtensionBuilder withExtensionPoint(String type, Map<String, Object> properties) {
            extensionPoints.put(type, new ExtensionPointDef(type, properties));
            return this;
        }

        public void build() {
            defaultExtensionManager.extensionDefs.put(name,
                new ExtensionDef(name, directory, gav, enabled, extensionPoints));
        }
    }
}
