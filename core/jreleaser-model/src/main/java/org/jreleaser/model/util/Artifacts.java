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
package org.jreleaser.model.util;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Assembler;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.FileSet;
import org.jreleaser.model.Files;
import org.jreleaser.model.Glob;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.util.FileType;
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
import static org.jreleaser.model.util.Templates.resolve;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_ARCH;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_FILE;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_FILE_EXTENSION;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_FILE_FORMAT;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_FILE_NAME;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_NAME;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_OS;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_PLATFORM;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_VERSION;
import static org.jreleaser.util.Constants.KEY_PROJECT_EFFECTIVE_VERSION;
import static org.jreleaser.util.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Artifacts {
    public static String resolveForArtifact(String input, JReleaserContext context) {
        return resolve(input, context.props());
    }

    public static String resolveForArtifact(String input, JReleaserContext context, Artifact artifact) {
        return resolve(input, artifactProps(artifact, context.props()));
    }

    public static String resolveForGlob(String input, JReleaserContext context, Glob glob) {
        return resolve(input, globProps(glob, context.props()));
    }

    public static String resolveForFileSet(String input, JReleaserContext context, FileSet fileSet) {
        return resolve(input, fileSetProps(fileSet, context.props()));
    }

    public static String resolveForArtifact(String input, JReleaserContext context, Artifact artifact, Distribution distribution) {
        Map<String, Object> props = context.props();
        props.putAll(distribution.props());
        props = artifactProps(artifact, props);
        return resolve(input, props);
    }

    public static String resolveForArtifact(String input, JReleaserContext context, Artifact artifact, Assembler assembler) {
        Map<String, Object> props = context.props();
        props.putAll(assembler.props());
        props = artifactProps(artifact, props);
        return resolve(input, props);
    }

    public static Map<String, Object> artifactProps(Artifact artifact, Map<String, Object> props) {
        if (artifact.getEffectivePath() != null) {
            return resolvedArtifactProps(artifact, props);
        }
        return unresolvedArtifactProps(artifact, props);
    }

    public static Map<String, Object> unresolvedArtifactProps(Artifact artifact, Map<String, Object> props) {
        props.putAll(artifact.getExtraProperties());
        props.putAll(artifact.getResolvedExtraProperties());
        props.put("platform", artifact.getPlatform());
        props.put("artifactPlatform", artifact.getPlatform());
        return props;
    }

    public static Map<String, Object> resolvedArtifactProps(Artifact artifact, Map<String, Object> props) {
        props.putAll(artifact.getExtraProperties());
        props.putAll(artifact.getResolvedExtraProperties());

        String artifactFile = artifact.getEffectivePath().getFileName().toString();
        String artifactFileName = getFilename(artifactFile, FileType.getSupportedExtensions());
        String artifactExtension = artifactFile.substring(artifactFileName.length() + 1);
        String artifactFileFormat = artifactExtension.substring(1);

        props.put(KEY_ARTIFACT_FILE, artifactFile);
        props.put(KEY_ARTIFACT_FILE_NAME, artifactFileName);
        props.put(KEY_ARTIFACT_FILE_EXTENSION, artifactExtension);
        props.put(KEY_ARTIFACT_FILE_FORMAT, artifactFileFormat);

        String artifactName = "";
        String projectVersion = (String) props.get(KEY_PROJECT_EFFECTIVE_VERSION);
        if (isNotBlank(projectVersion) && artifactFileName.contains(projectVersion)) {
            artifactName = artifactFileName.substring(0, artifactFileName.indexOf(projectVersion));
            if (artifactName.endsWith("-")) {
                artifactName = artifactName.substring(0, artifactName.length() - 1);
            }
            props.put(KEY_ARTIFACT_VERSION, projectVersion);
        }
        projectVersion = (String) props.get(KEY_PROJECT_VERSION);
        if (isBlank(artifactName) && isNotBlank(projectVersion) && artifactFileName.contains(projectVersion)) {
            artifactName = artifactFileName.substring(0, artifactFileName.indexOf(projectVersion));
            if (artifactName.endsWith("-")) {
                artifactName = artifactName.substring(0, artifactName.length() - 1);
            }
            props.put(KEY_ARTIFACT_VERSION, projectVersion);
        }
        props.put(KEY_ARTIFACT_NAME, artifactName);

        String platform = artifact.getPlatform();
        if (isNotBlank(platform)) {
            props.put("platform", platform);
            props.put(KEY_ARTIFACT_PLATFORM, platform);
            if (platform.contains("-")) {
                String[] parts = platform.split("-");
                props.put(KEY_ARTIFACT_OS, parts[0]);
                props.put(KEY_ARTIFACT_ARCH, parts[1]);
            }
        }

        return props;
    }

    public static Map<String, Object> globProps(Glob glob, Map<String, Object> props) {
        props.putAll(glob.getExtraProperties());
        props.putAll(glob.getResolvedExtraProperties());
        return props;
    }

    public static Map<String, Object> fileSetProps(FileSet fileSet, Map<String, Object> props) {
        props.putAll(fileSet.getExtraProperties());
        props.putAll(fileSet.getResolvedExtraProperties());
        return props;
    }

    public static Path checkAndCopyFile(JReleaserContext context, Path src, Path dest) throws JReleaserException {
        if (null == dest) return src;

        if (!java.nio.file.Files.exists(dest)) {
            context.getLogger().debug(RB.$("artifacts.not.exists"),
                context.relativizeToBasedir(dest));
            copyFile(context, src, dest);
        } else if (src.toFile().lastModified() > dest.toFile().lastModified()) {
            context.getLogger().debug(RB.$("artifacts.newer"),
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
            throw new JReleaserException(RB.$("ERROR_artifacts_unexpected_error_copying",
                context.relativizeToBasedir(src),
                context.relativizeToBasedir(dest)));
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
                throw new JReleaserException(RB.$("ERROR_artifacts_glob_resolution"));
            }

            return Artifact.sortArtifacts(resolver.artifacts);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_artifacts_unexpected_error_globs"), e);
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
                throw new JReleaserException(RB.$("ERROR_artifacts_glob_resolution"));
            }

            return Artifact.sortArtifacts(resolver.artifacts);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_artifacts_unexpected_error_globs"), e);
        }
    }

    public static Set<Artifact> resolveFiles(JReleaserContext context, Collection<String> globs) throws JReleaserException {
        return resolveFiles(context, context.getBasedir(), globs);
    }

    public static Set<Artifact> resolveFiles(JReleaserContext context, Path directory, Collection<String> globs) throws JReleaserException {
        return resolveFiles(context.getLogger(), context.props(), directory, globs);
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
            logger.error(RB.$("ERROR_artifacts_unexpected_error_path"),
                basedir.toAbsolutePath().relativize(file.toAbsolutePath()), e);
            return CONTINUE;
        }
    }
}
