/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import java.io.IOException;
import java.io.StringReader;
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

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.Files.exists;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Glob implements Domain {
    private String directory;
    private String include;
    private String exclude;
    private Boolean recursive;
    private Set<Path> paths;

    public Set<Path> getResolvedPaths(JReleaserContext context) {
        if (null == paths) {
            // resolve directory
            Path path = context.getBasedir();
            if (isNotBlank(directory)) {
                if (directory.contains("{{")) {
                    directory = applyTemplate(new StringReader(directory), context.getModel().props());
                }
                path = context.getBasedir().resolve(Paths.get(directory)).normalize();
                if (!exists(path)) {
                    throw new JReleaserException("Path does not exist. " + context.getBasedir().relativize(path));
                }
            }

            FileCollector fileCollector = new FileCollector(context, include, exclude, path, isRecursive());
            try {
                java.nio.file.Files.walkFileTree(path, fileCollector);
            } catch (IOException e) {
                throw new JReleaserException("Unnexpected error resolving glob " + this.asMap());
            }

            if (fileCollector.failed) {
                throw new JReleaserException("Could not resolve glob " + this.asMap());
            }

            paths = fileCollector.getFiles();
        }
        return paths;
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

    public void setInclude(String include) {
        this.include = include;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public Boolean isRecursive() {
        return recursive != null && recursive;
    }

    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isRecursiveSet() {
        return recursive != null;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("directory", directory);
        map.put("include", include);
        map.put("exclude", exclude);
        map.put("recursive", isRecursive());
        return map;
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
                files.add(file);
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
            context.getLogger().error("Unnexpected error visiting path " +
                context.getBasedir().relativize(file), e);
            return CONTINUE;
        }
    }
}
