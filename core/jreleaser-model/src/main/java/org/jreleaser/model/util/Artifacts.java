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
import org.jreleaser.model.ExtraProperties;
import org.jreleaser.model.FileSet;
import org.jreleaser.model.Files;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Glob;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Packager;
import org.jreleaser.model.SdkmanAnnouncer;
import org.jreleaser.model.Upload;
import org.jreleaser.model.Uploader;
import org.jreleaser.util.FileType;
import org.jreleaser.util.JReleaserException;
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
import java.util.Optional;
import java.util.Set;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_ARCH;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_FILE;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_FILE_EXTENSION;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_FILE_FORMAT;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_FILE_NAME;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_NAME;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_OS;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_PLATFORM;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_PLATFORM_REPLACED;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_VERSION;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_ARCHIVE_FORMAT;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_EXTENSION;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_FORMAT;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_PLATFORM;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_PLATFORM_REPLACED;
import static org.jreleaser.util.Constants.KEY_PROJECT_EFFECTIVE_VERSION;
import static org.jreleaser.util.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Artifacts {
    private static final String DOWNLOAD_URL_SUFFIX = "DownloadUrl";
    private static final String DOWNLOAD_URL_KEY = "downloadUrl";
    private static final String DOWNLOAD_URL_FROM_KEY = "downloadUrlFrom";

    public static String resolveForArtifact(String input, JReleaserContext context) {
        return resolveTemplate(input, context.fullProps());
    }

    public static String resolveForArtifact(String input, JReleaserContext context, Artifact artifact) {
        return resolveTemplate(input, artifactProps(artifact, context.fullProps()));
    }

    public static String resolveForGlob(String input, JReleaserContext context, Glob glob) {
        return resolveTemplate(input, globProps(glob, context.fullProps()));
    }

    public static String resolveForFileSet(String input, JReleaserContext context, FileSet fileSet) {
        return resolveTemplate(input, fileSetProps(fileSet, context.fullProps()));
    }

    public static String resolveForArtifact(String input, JReleaserContext context, Artifact artifact, Distribution distribution) {
        Map<String, Object> props = context.fullProps();
        props.putAll(distribution.props());
        props = artifactProps(artifact, props);
        return resolveTemplate(input, props);
    }

    public static String resolveForArtifact(String input, JReleaserContext context, Artifact artifact, Assembler assembler) {
        Map<String, Object> props = context.fullProps();
        props.putAll(assembler.props());
        props = artifactProps(artifact, props);
        return resolveTemplate(input, props);
    }

    public static Map<String, Object> artifactProps(Artifact artifact, Map<String, Object> props) {
        if (artifact.getEffectivePath() != null) {
            return resolvedArtifactProps(artifact, props);
        }
        return unresolvedArtifactProps(artifact, props);
    }

    public static Map<String, Object> artifactProps(Artifact artifact, Distribution distribution, Map<String, Object> props) {
        if (artifact.getEffectivePath() != null) {
            return resolvedArtifactProps(artifact, distribution, props);
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
        props.put(KEY_ARTIFACT_FILE, artifactFile);
        props.put(KEY_ARTIFACT_FILE_NAME, artifactFileName);

        if (!artifactFile.equals(artifactFileName)) {
            String artifactExtension = artifactFile.substring(artifactFileName.length());
            String artifactFileFormat = artifactExtension.substring(1);
            props.put(KEY_ARTIFACT_FILE_EXTENSION, artifactExtension);
            props.put(KEY_ARTIFACT_FILE_FORMAT, artifactFileFormat);
        }

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

    public static Map<String, Object> resolvedArtifactProps(Artifact artifact, Distribution distribution, Map<String, Object> props) {
        props.putAll(artifact.getExtraProperties());
        props.putAll(artifact.getResolvedExtraProperties());

        String artifactFile = artifact.getEffectivePath().getFileName().toString();
        String artifactFileName = getFilename(artifactFile, FileType.getSupportedExtensions());
        props.put(KEY_ARTIFACT_FILE, artifactFile);
        props.put(KEY_ARTIFACT_FILE_NAME, artifactFileName);
        props.put(KEY_DISTRIBUTION_ARTIFACT_FILE, artifactFile);
        props.put(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME, artifactFileName);

        if (!artifactFile.equals(artifactFileName)) {
            String artifactExtension = artifactFile.substring(artifactFileName.length());
            String artifactFileFormat = artifactExtension.substring(1);
            props.put(KEY_ARTIFACT_FILE_EXTENSION, artifactExtension);
            props.put(KEY_ARTIFACT_FILE_FORMAT, artifactFileFormat);
            props.put(KEY_DISTRIBUTION_ARTIFACT_FILE_EXTENSION, artifactExtension);
            props.put(KEY_DISTRIBUTION_ARTIFACT_FILE_FORMAT, artifactFileFormat);
            props.put(KEY_DISTRIBUTION_ARTIFACT_ARCHIVE_FORMAT, artifactFileFormat);
        }

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

        String platformReplaced = distribution.getPlatform().applyReplacements(platform);
        if (isNotBlank(platformReplaced)) props.put(KEY_ARTIFACT_PLATFORM_REPLACED, platformReplaced);
        if (isNotBlank(platform)) props.put(KEY_DISTRIBUTION_ARTIFACT_PLATFORM, platform);
        if (isNotBlank(platformReplaced)) props.put(KEY_DISTRIBUTION_ARTIFACT_PLATFORM_REPLACED, platformReplaced);

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

    public static String resolveDownloadUrl(JReleaserContext context, String packagerName, Distribution distribution, Artifact artifact) {
        List<String> keys = Collections.singletonList("skip" + capitalize(packagerName));
        if (isSkip(artifact, keys)) return "";

        String downloadUrl = artifact.getExtraProperty(packagerName + DOWNLOAD_URL_SUFFIX);
        if (isBlank(downloadUrl)) {
            downloadUrl = artifact.getExtraProperty(DOWNLOAD_URL_KEY);
        }

        Packager packager = distribution.findPackager(packagerName);
        if (isBlank(downloadUrl)) {
            downloadUrl = packager.getDownloadUrl();
        }

        if (isBlank(downloadUrl)) {
            downloadUrl = distribution.getExtraProperty(packagerName + DOWNLOAD_URL_SUFFIX);
        }
        if (isBlank(downloadUrl)) {
            downloadUrl = distribution.getExtraProperty(DOWNLOAD_URL_KEY);
        }

        GitService service = context.getModel().getRelease().getGitService();
        if (isBlank(downloadUrl)) {
            if (!service.isSkipRelease() && service.isArtifacts() && service.resolveUploadAssetsEnabled(context.getModel().getProject())) {
                downloadUrl = service.getDownloadUrl();
            }
        }

        if (isBlank(downloadUrl)) {
            downloadUrl = resolveDownloadUrlFromUploader(context, artifact, artifact);
        }
        if (isBlank(downloadUrl)) {
            downloadUrl = resolveDownloadUrlFromUploader(context, packager, artifact);
        }
        if (isBlank(downloadUrl)) {
            downloadUrl = resolveDownloadUrlFromUploader(context, distribution, artifact);
        }

        if (isBlank(downloadUrl)) {
            context.getLogger().warn(RB.$("ERROR_artifacts_download_url_missing",
                artifact.getEffectivePath(context, distribution).getFileName().toString(),
                service.getServiceName()));
            // assume artifact is available from Git
            downloadUrl = service.getDownloadUrl();
        }

        Map<String, Object> props = context.fullProps();
        props.putAll(packager.getResolvedExtraProperties());
        props.putAll(distribution.props());
        artifactProps(artifact, distribution, props);

        return resolveTemplate(downloadUrl, props);
    }

    public static String resolveDownloadUrl(JReleaserContext context, SdkmanAnnouncer announcer, Distribution distribution, Artifact artifact) {
        String packager = SdkmanAnnouncer.NAME;
        List<String> keys = Collections.singletonList("skip" + capitalize(packager));
        if (isSkip(artifact, keys)) return "";

        String downloadUrl = artifact.getExtraProperty(packager + DOWNLOAD_URL_SUFFIX);
        if (isBlank(downloadUrl)) {
            downloadUrl = artifact.getExtraProperty(DOWNLOAD_URL_KEY);
        }

        if (isBlank(downloadUrl)) {
            downloadUrl = announcer.getDownloadUrl();
        }

        if (isBlank(downloadUrl)) {
            downloadUrl = distribution.getExtraProperty(packager + DOWNLOAD_URL_SUFFIX);
        }
        if (isBlank(downloadUrl)) {
            downloadUrl = distribution.getExtraProperty(DOWNLOAD_URL_KEY);
        }

        GitService service = context.getModel().getRelease().getGitService();
        if (isBlank(downloadUrl)) {
            if (!service.isSkipRelease() && service.isArtifacts() && service.resolveUploadAssetsEnabled(context.getModel().getProject())) {
                downloadUrl = service.getDownloadUrl();
            }
        }

        if (isBlank(downloadUrl)) {
            downloadUrl = resolveDownloadUrlFromUploader(context, artifact, artifact);
        }
        if (isBlank(downloadUrl)) {
            downloadUrl = resolveDownloadUrlFromUploader(context, announcer, artifact);
        }
        if (isBlank(downloadUrl)) {
            downloadUrl = resolveDownloadUrlFromUploader(context, distribution, artifact);
        }

        if (isBlank(downloadUrl)) {
            context.getLogger().warn(RB.$("ERROR_artifacts_download_url_missing",
                    artifact.getEffectivePath(context, distribution).getFileName().toString()),
                service.getServiceName());
            // assume artifact is available from Git
            downloadUrl = service.getDownloadUrl();
        }

        Map<String, Object> props = context.fullProps();
        props.putAll(announcer.getResolvedExtraProperties());
        props.putAll(distribution.props());
        artifactProps(artifact, distribution, props);

        return resolveTemplate(downloadUrl, props);
    }

    private static String resolveDownloadUrlFromUploader(JReleaserContext context, ExtraProperties props, Artifact artifact) {
        Upload upload = context.getModel().getUpload();

        String coords = props.getExtraProperty(DOWNLOAD_URL_FROM_KEY);
        if (isBlank(coords)) {
            // search for "<uploaderType><uploaderName>Path"
            for (Uploader up : upload.findAllActiveUploaders()) {
                List<String> keys = up.resolveSkipKeys();
                String key = up.getType() + capitalize(up.getName()) + "Path";
                if (artifact.getExtraProperties().containsKey(key) && !isSkip(props, keys)) {
                    return up.getResolvedDownloadUrl(context, artifact);
                }
            }
            return null;
        }

        String[] parts = coords.split(":");
        if (parts.length != 2) return null;

        Optional<? extends Uploader> uploader = upload
            .getActiveUploader(parts[0], parts[1]);
        if (uploader.isPresent()) {
            List<String> keys = uploader.get().resolveSkipKeys();
            if (!isSkip(props, keys)) {
                return uploader.get().getResolvedDownloadUrl(context, artifact);
            }
        } else {
            // search for "<uploaderType><uploaderName>Path"
            for (Uploader up : upload.findAllActiveUploaders()) {
                List<String> keys = up.resolveSkipKeys();
                String key = up.getType() + capitalize(up.getName()) + "Path";
                if (artifact.getExtraProperties().containsKey(key) && !isSkip(props, keys)) {
                    return up.getResolvedDownloadUrl(context, artifact);
                }
            }
        }

        return null;
    }

    private static boolean isSkip(ExtraProperties props, List<String> keys) {
        for (String key : keys) {
            if (props.extraPropertyIsTrue(key)) {
                return true;
            }
        }
        return false;
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
                context.relativizeToBasedir(dest),
                e.toString()));
        }
    }

    public static Set<Artifact> resolveFiles(JReleaserContext context) throws JReleaserException {
        Files files = context.getModel().getFiles();
        Set<Artifact> paths = new LinkedHashSet<>();
        if (!files.isEnabled()) return paths;

        if (files.arePathsResolved()) {
            return files.getPaths();
        }

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
            matchers.add(fileSystem.getPathMatcher(resolveTemplate(glob, props)));
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
        return resolveFiles(context.getLogger(), context.fullProps(), directory, globs);
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
