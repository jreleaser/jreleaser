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
package org.jreleaser.model.util;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Files;
import org.jreleaser.model.Glob;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.util.JReleaserLogger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.util.MustacheUtils.applyTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Artifacts {
    public static Path checkAndCopyFile(JReleaserContext context, Path src, Path dest) throws JReleaserException {
        if (null == dest) return src;

        if (!java.nio.file.Files.exists(dest)) {
            context.getLogger().debug("artifact does not exist: {}",
                context.relativizeToBasedir(dest));
            copyFile(context, src, dest);
        } else if (src.toFile().lastModified() > dest.toFile().lastModified()) {
            context.getLogger().debug("{} is newer than {}",
                context.relativizeToBasedir(src),
                context.relativizeToBasedir(dest));
            copyFile(context, src, dest);
        }

        return dest;
    }

    public static void copyFile(JReleaserContext context, Path src, Path dest) throws JReleaserException {
        try {
            java.nio.file.Files.createDirectories(dest.getParent());
            java.nio.file.Files.copy(src, dest, REPLACE_EXISTING, COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error copying " +
                context.relativizeToBasedir(src) + " to " +
                context.relativizeToBasedir(dest));
        }
    }

    public static Set<Artifact> resolveFiles(JReleaserContext context) throws JReleaserException {
        Files files = context.getModel().getFiles();

        if (files.arePathsResolved()) {
            return files.getPaths();
        }

        Set<Artifact> paths = new LinkedHashSet<>();

        // resolve artifacts
        for (Artifact artifact : files.getArtifacts()) {
            if (!context.isPlatformSelected(artifact)) continue;
            artifact.activate();
            artifact.getEffectivePath(context);
            paths.add(artifact);
        }

        // resolve globs
        for (Glob glob : files.getGlobs()) {
            for (Artifact artifact : glob.getResolvedArtifacts(context)) {
                if (!artifact.isActive()) continue;
                paths.add(artifact);
            }
        }

        files.setPaths(Artifact.sortArtifacts(paths));

        return files.getPaths();
    }

    public static Set<Artifact> resolveFiles(JReleaserLogger logger, Path basedir, Collection<String> globs) throws JReleaserException {
        if (null == globs || globs.isEmpty()) {
            return Collections.emptySet();
        }

        FileSystem fileSystem = FileSystems.getDefault();
        List<PathMatcher> matchers = new ArrayList<>();
        for (String glob : globs) {
            matchers.add(fileSystem.getPathMatcher(glob));
        }

        GlobResolver resolver = new GlobResolver(logger, basedir, matchers);
        try {
            java.nio.file.Files.walkFileTree(basedir, resolver);
            if (resolver.failed) {
                throw new JReleaserException("Some globs failed to be resolved.");
            }

            return Artifact.sortArtifacts(resolver.artifacts);
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error when resolving globs", e);
        }
    }

    public static Set<Artifact> resolveFiles(JReleaserLogger logger,
                                             Map<String, Object> props,
                                             Path basedir,
                                             Collection<String> globs) throws JReleaserException {
        if (null == globs || globs.isEmpty()) {
            return Collections.emptySet();
        }

        FileSystem fileSystem = FileSystems.getDefault();
        List<PathMatcher> matchers = new ArrayList<>();
        for (String glob : globs) {
            matchers.add(fileSystem.getPathMatcher(applyTemplate(glob, props)));
        }

        GlobResolver resolver = new GlobResolver(logger, basedir, matchers);
        try {
            java.nio.file.Files.walkFileTree(basedir, resolver);
            if (resolver.failed) {
                throw new JReleaserException("Some globs failed to be resolved.");
            }

            return Artifact.sortArtifacts(resolver.artifacts);
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error when resolving globs", e);
        }
    }

    public static Set<Artifact> resolveFiles(JReleaserContext context, Collection<String> globs) throws JReleaserException {
        return resolveFiles(context.getLogger(), context.props(), context.getBasedir(), globs);
    }

    private static class GlobResolver extends SimpleFileVisitor<Path> {
        private final JReleaserLogger logger;
        private final List<PathMatcher> matchers;
        private final Path basedir;
        private final Set<Artifact> artifacts = new LinkedHashSet<>();
        private boolean failed;

        private GlobResolver(JReleaserLogger logger, Path basedir, List<PathMatcher> matchers) {
            this.logger = logger;
            this.matchers = matchers;
            this.basedir = basedir;
        }

        private void match(Path path) {
            if (matchers.stream()
                .anyMatch(matcher -> matcher.matches(path))) {
                artifacts.add(Artifact.of(path));
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            match(file);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
            failed = true;
            logger.error("Unexpected error visiting path " +
                basedir.toAbsolutePath().relativize(file.toAbsolutePath()), e);
            return CONTINUE;
        }
    }
}
