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
package org.jreleaser.model;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.util.Artifacts;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.Files.exists;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Glob implements Domain, ExtraProperties {
    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private String pattern;
    private Set<Artifact> artifacts;

    private String directory;
    private String include;
    private String exclude;
    private Boolean recursive;

    void setAll(Glob glob) {
        this.pattern = glob.pattern;
        setExtraProperties(glob.extraProperties);
    }

    @Override
    public String getPrefix() {
        return "artifact";
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    public Set<Artifact> getResolvedArtifacts(JReleaserContext context) {
        return isNotBlank(pattern) ? getResolvedArtifactsPattern(context) : getResolvedArtifactsLegacy(context);
    }

    public Set<Artifact> getResolvedArtifactsPattern(JReleaserContext context) {
        if (null == artifacts) {
            artifacts = Artifacts.resolveFiles(context, resolveDirectory(context), Collections.singletonList(pattern));
            artifacts.forEach(artifact -> {
                if (context.isPlatformSelected(artifact)) artifact.activate();
                artifact.setExtraProperties(getExtraProperties());
            });
        }

        return artifacts;
    }

    private Path resolveDirectory(JReleaserContext context) {
        // resolve directory
        Path path = context.getBasedir();
        if (isNotBlank(directory)) {
            if (directory.contains("{{")) {
                directory = applyTemplate(directory, context.props());
            }
            path = context.getBasedir().resolve(Paths.get(directory)).normalize();
            if (!exists(path)) {
                throw new JReleaserException(RB.$("ERROR_path_does_not_exist", context.relativizeToBasedir(path)));
            }
        }

        return path;
    }

    @Deprecated
    public Set<Artifact> getResolvedArtifactsLegacy(JReleaserContext context) {
        if (null == artifacts) {
            // resolve directory
            Path path = context.getBasedir();
            if (isNotBlank(directory)) {
                if (directory.contains("{{")) {
                    directory = applyTemplate(directory, context.props());
                }
                path = context.getBasedir().resolve(Paths.get(directory)).normalize();
                if (!exists(path)) {
                    throw new JReleaserException(RB.$("ERROR_path_does_not_exist", context.relativizeToBasedir(path)));
                }
            }

            FileCollector fileCollector = new FileCollector(context, include, exclude, path, isRecursive());
            try {
                java.nio.file.Files.walkFileTree(path, fileCollector);
            } catch (IOException e) {
                throw new JReleaserException(RB.$("ERROR_unexpected_glob_resolve", this.asMap(true)));
            }

            if (fileCollector.failed) {
                throw new JReleaserException(RB.$("ERROR_glob_resolve", this.asMap(true)));
            }

            artifacts = fileCollector.getFiles().stream()
                .map(p -> Artifact.of(p, getExtraProperties()))
                .peek(a -> {
                    if (context.isPlatformSelected(a)) a.activate();
                })
                .collect(Collectors.toSet());
            Artifact.sortArtifacts(artifacts);
        }
        return artifacts;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        if (isBlank(pattern)) return;

        if (pattern.startsWith(GLOB_PREFIX) || pattern.startsWith(REGEX_PREFIX)) {
            this.pattern = pattern.trim();
        } else {
            this.pattern = GLOB_PREFIX + pattern.trim();
        }

        if (this.pattern.startsWith(GLOB_PREFIX)) {
            String path = this.pattern.substring(GLOB_PREFIX.length());
            if (!Paths.get(path).isAbsolute()) {
                this.pattern = GLOB_PREFIX + "**" + File.separator + path;
            }
        } else {
            String path = this.pattern.substring(REGEX_PREFIX.length());
            if (!Paths.get(path).isAbsolute()) {
                this.pattern = REGEX_PREFIX + ".*" + File.separator + path;
            }
        }
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getInclude() {
        return include;
    }

    @Deprecated
    public void setInclude(String include) {
        System.out.println("glob.include has been deprecated since 0.6.0 and will be removed in the future. Use glob.pattern instead");
        this.include = include;
    }

    public String getExclude() {
        return exclude;
    }

    @Deprecated
    public void setExclude(String exclude) {
        System.out.println("glob.exclude has been deprecated since 0.6.0 and will be removed in the future. Use glob.pattern instead");
        this.exclude = exclude;
    }

    public Boolean isRecursive() {
        return recursive != null && recursive;
    }

    @Deprecated
    public void setRecursive(Boolean recursive) {
        System.out.println("glob.recursive has been deprecated since 0.6.0 and will be removed in the future. Use glob.pattern instead");
        this.recursive = recursive;
    }

    public boolean isRecursiveSet() {
        return recursive != null;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("pattern", pattern);
        props.put("extraProperties", getResolvedExtraProperties());
        props.put("directory", directory);
        props.put("include", include);
        props.put("exclude", exclude);
        if (isBlank(pattern)) props.put("recursive", isRecursive());
        return props;
    }

    public static class FileCollector extends SimpleFileVisitor<Path> {
        private final PathMatcher includeMatcher;
        private final Set<Path> files = new LinkedHashSet<>();
        private final JReleaserContext context;
        private final boolean recursive;
        private final Path start;

        private PathMatcher excludeMatcher;
        private boolean failed;

        FileCollector(JReleaserContext context, String include, String exclude, Path start, boolean recursive) {
            this.context = context;
            this.start = start;
            this.recursive = recursive;

            if (include.startsWith("regex:") || include.startsWith("glob:")) {
                includeMatcher = FileSystems.getDefault()
                    .getPathMatcher(include);
            } else {
                includeMatcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + include);
            }

            if (isNotBlank(exclude)) {
                if (exclude.startsWith("regex:") || exclude.startsWith("glob:")) {
                    excludeMatcher = FileSystems.getDefault()
                        .getPathMatcher(exclude);
                } else {
                    excludeMatcher = FileSystems.getDefault()
                        .getPathMatcher("glob:" + exclude);
                }
            }
        }

        private boolean match(Path file) {
            Path fileName = file.getFileName();

            if (null != fileName && includeMatcher.matches(fileName)) {
                if (null != excludeMatcher && excludeMatcher.matches(fileName)) {
                    return false;
                }
                if (java.nio.file.Files.isRegularFile(file)) files.add(file);
            }
            return null == excludeMatcher || !excludeMatcher.matches(fileName);
        }

        public Set<Path> getFiles() {
            return Collections.unmodifiableSet(files);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            match(file);
            return CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (recursive || dir.equals(start)) {
                return match(dir) ? CONTINUE : SKIP_SUBTREE;
            }
            return SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            failed = true;
            context.getLogger().error(RB.$("ERROR_artifacts_unexpected_error_path",
                context.relativizeToBasedir(file)), e);
            return CONTINUE;
        }
    }
}
