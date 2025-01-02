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
package org.jreleaser.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.Packager;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.model.spi.packagers.PackagerProcessor;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.command.CommandExecutor;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.FileType;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_ARCH;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_FILE_EXTENSION;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_FILE_FORMAT;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_NAME;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_OS;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_PLATFORM;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_PLATFORM_REPLACED;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_ROOT_ENTRY_NAME;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_SIZE;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_VERSION;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_ARCH;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_EXTENSION;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_FORMAT;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_OS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_PLATFORM;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_PLATFORM_REPLACED;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_SIZE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_VERSION;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_PREPARE_DIRECTORY;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_SHA_256;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_SIZE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_URL;
import static org.jreleaser.model.Constants.KEY_REVERSE_REPO_HOST;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.util.FileUtils.resolveRootEntryName;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractPackagerProcessor<T extends Packager<?>> implements PackagerProcessor<T> {
    private static final String ARTIFACT = "artifact";
    private static final String DISTRIBUTION = "distribution";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final String OS = "Os";
    private static final String ARCH = "Arch";
    private static final String FILE = "File";
    private static final String SIZE = "Size";
    private static final String FILE_NAME = "FileName";
    private static final String ROOT_ENTRY_NAME = "RootEntryName";
    private static final String FILE_EXTENSION = "FileExtension";
    private static final String FILE_FORMAT = "FileFormat";
    private static final String CHECKSUM = "Checksum";
    private static final String URL = "Url";

    protected final JReleaserContext context;
    protected T packager;

    protected AbstractPackagerProcessor(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public T getPackager() {
        return packager;
    }

    @Override
    public void setPackager(T packager) {
        this.packager = packager;
    }

    @Override
    public String getPackagerName() {
        return packager.getType();
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return true;
    }

    @Override
    public void prepareDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        try {
            String distributionName = distribution.getName();
            context.getLogger().debug(RB.$("packager.create.properties"), distributionName, getPackagerName());
            TemplateContext newProps = fillProps(distribution, props);
            if (newProps.isEmpty()) {
                context.getLogger().warn(RB.$("packager.skip.distribution"), distributionName);
                return;
            }

            doPrepareDistribution(distribution, newProps);
        } catch (RuntimeException e) {
            throw new PackagerProcessingException(e);
        }
    }

    protected abstract void doPrepareDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException;

    @Override
    public void packageDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        try {
            String distributionName = distribution.getName();
            context.getLogger().debug(RB.$("packager.create.properties"), distributionName, getPackagerName());
            TemplateContext newProps = fillProps(distribution, props);
            if (newProps.isEmpty()) {
                context.getLogger().warn(RB.$("packager.skip.distribution"), distributionName);
                return;
            }

            doPackageDistribution(distribution, newProps);
        } catch (IllegalArgumentException e) {
            throw new PackagerProcessingException(e);
        }
    }

    @Override
    public void publishDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        if (context.getModel().getProject().isSnapshot() && !packager.isSnapshotSupported()) {
            context.getLogger().info(RB.$("packager.publish.snapshot.not.supported"));
            return;
        }

        try {
            String distributionName = distribution.getName();
            context.getLogger().debug(RB.$("packager.create.properties"), distributionName, getPackagerName());
            TemplateContext newProps = fillProps(distribution, props);
            if (newProps.isEmpty()) {
                context.getLogger().warn(RB.$("packager.skip.distribution"), distributionName);
                return;
            }

            doPublishDistribution(distribution, newProps);
        } catch (RuntimeException e) {
            throw new PackagerProcessingException(e);
        }
    }

    protected abstract void doPackageDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException;

    protected abstract void doPublishDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException;

    protected TemplateContext fillProps(Distribution distribution, TemplateContext props) {
        TemplateContext newProps = new TemplateContext(props);
        context.getLogger().debug(RB.$("packager.fill.distribution.properties"));
        fillDistributionProperties(newProps, distribution);
        context.getLogger().debug(RB.$("packager.fill.git.properties"));
        context.getModel().getRelease().getReleaser().fillProps(newProps, context.getModel());
        context.getLogger().debug(RB.$("packager.fill.artifact.properties"));
        if (!verifyAndAddArtifacts(newProps, distribution)) {
            // we can't continue with this packager
            return TemplateContext.empty();
        }
        context.getLogger().debug(RB.$("packager.fill.packager.properties"));
        fillPackagerProperties(newProps, distribution);
        applyTemplates(newProps, packager.resolvedExtraProperties());
        if (isBlank(context.getModel().getRelease().getReleaser().getReverseRepoHost())) {
            newProps.set(KEY_REVERSE_REPO_HOST,
                packager.getExtraProperties().get(KEY_REVERSE_REPO_HOST));
        }
        applyTemplates(newProps, newProps);
        return newProps;
    }

    protected void fillDistributionProperties(TemplateContext props, Distribution distribution) {
        props.setAll(distribution.props());
    }

    protected abstract void fillPackagerProperties(TemplateContext props, Distribution distribution);

    protected Command.Result executeCommand(Path directory, Command command) throws PackagerProcessingException {
        try {
            Command.Result result = new CommandExecutor(context.getLogger())
                .executeCommand(directory, command);
            if (result.getExitValue() != 0) {
                throw new CommandException(RB.$("ERROR_command_execution_exit_value", result.getExitValue()));
            }
            return result;
        } catch (CommandException e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    protected Command.Result executeCommand(Command command) throws PackagerProcessingException {
        try {
            Command.Result result = new CommandExecutor(context.getLogger())
                .executeCommand(command);
            if (result.getExitValue() != 0) {
                throw new CommandException(RB.$("ERROR_command_execution_exit_value", result.getExitValue()));
            }
            return result;
        } catch (CommandException e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    protected Command.Result executeCommand(Command command, InputStream in) throws PackagerProcessingException {
        try {
            Command.Result result = new CommandExecutor(context.getLogger())
                .executeCommand(command, in);
            if (result.getExitValue() != 0) {
                throw new CommandException(RB.$("ERROR_command_execution_exit_value", result.getExitValue()));
            }
            return result;
        } catch (CommandException e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    protected void copyPreparedFiles(TemplateContext props) throws PackagerProcessingException {
        Path prepareDirectory = getPrepareDirectory(props);
        Path packageDirectory = getPackageDirectory(props);
        copyFiles(prepareDirectory, packageDirectory);
    }

    protected void copyFiles(Path src, Path dest) throws PackagerProcessingException {
        try {
            if (!Files.exists(dest)) {
                Files.createDirectories(dest);
            }

            if (!FileUtils.copyFilesRecursive(context.getLogger(), src, dest)) {
                throw new PackagerProcessingException(RB.$("ERROR_copy_files_from_to",
                    context.relativizeToBasedir(src),
                    context.relativizeToBasedir(dest)));
            }
        } catch (IOException e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_copy_files_from_to",
                context.relativizeToBasedir(src),
                context.relativizeToBasedir(dest)), e);
        }
    }

    protected boolean verifyAndAddArtifacts(TemplateContext props,
                                            Distribution distribution) {
        return verifyAndAddArtifacts(props, distribution, collectArtifacts(distribution));
    }

    protected boolean verifyAndAddArtifacts(TemplateContext props,
                                            Distribution distribution,
                                            List<Artifact> artifacts) {
        List<Artifact> activeArtifacts = artifacts.stream()
            .filter(Artifact::isActiveAndSelected)
            .collect(toList());

        if (activeArtifacts.isEmpty()) {
            // we can't proceed
            context.getLogger().warn(RB.$("packager.no.matching.artifacts"),
                distribution.getName(), capitalize(packager.getType()));
            return false;
        }

        int count = 0;
        for (Artifact artifact : activeArtifacts) {
            String artifactUrl = Artifacts.resolveDownloadUrl(context, packager.getType(), distribution, artifact);
            if (isBlank(artifactUrl)) continue;
            count++;

            String platform = artifact.getPlatform();
            String artifactPlatform = isNotBlank(platform) ? capitalize(platform) : "";
            String platformReplaced = distribution.getPlatform().applyReplacements(platform);
            String artifactPlatformReplaced = isNotBlank(platformReplaced) ? capitalize(platformReplaced) : "";
            // add extra properties without clobbering existing keys
            Map<String, Object> artifactProps = artifact.resolvedExtraProperties(ARTIFACT + artifactPlatform);
            artifactProps.keySet().stream()
                .filter(k -> !props.contains(k))
                .forEach(k -> props.set(k, artifactProps.get(k)));

            Path artifactPath = artifact.getEffectivePath(context, distribution);

            long artifactSize = 0;
            try {
                artifactSize = Files.size(artifactPath);
            } catch (IOException ignored) {
                // this would be strange
                context.getLogger().trace(ignored);
            }

            String artifactFile = artifact.getEffectivePath().getFileName().toString();
            String artifactFileName = getFilename(artifactFile, FileType.getSupportedExtensions());
            String artifactRootEntryName = resolveRootEntryName(artifact.getEffectivePath());
            String artifactFileExtension = "";
            String artifactFileFormat = "";

            if (!artifactFile.equals(artifactFileName)) {
                artifactFileExtension = artifactFile.substring(artifactFileName.length());
                artifactFileFormat = artifactFileExtension.substring(1);
            }

            String artifactName = "";
            String artifactVersion = "";
            String projectVersion = context.getModel().getProject().getEffectiveVersion();
            if (isNotBlank(projectVersion) && artifactFileName.contains(projectVersion)) {
                artifactName = artifactFileName.substring(0, artifactFileName.indexOf(projectVersion));
                if (artifactName.endsWith("-")) {
                    artifactName = artifactName.substring(0, artifactName.length() - 1);
                }
                artifactVersion = projectVersion;
            }
            projectVersion = context.getModel().getProject().getVersion();
            if (isBlank(artifactName) && isNotBlank(projectVersion) && artifactFileName.contains(projectVersion)) {
                artifactName = artifactFileName.substring(0, artifactFileName.indexOf(projectVersion));
                if (artifactName.endsWith("-")) {
                    artifactName = artifactName.substring(0, artifactName.length() - 1);
                }
                artifactVersion = projectVersion;
            }

            String artifactOs = "";
            String artifactArch = "";
            if (isNotBlank(platform) && platform.contains("-")) {
                String[] parts = platform.split("-");
                artifactOs = parts[0];
                artifactArch = parts[1];
            }

            safePut(props, ARTIFACT + artifactPlatform + NAME, artifactName);
            safePut(props, ARTIFACT + artifactPlatform + VERSION, artifactVersion);
            safePut(props, ARTIFACT + artifactPlatform + OS, artifactOs);
            safePut(props, ARTIFACT + artifactPlatform + ARCH, artifactArch);
            safePut(props, ARTIFACT + artifactPlatform + FILE, artifactFile);
            safePut(props, ARTIFACT + artifactPlatform + SIZE, artifactSize);
            safePut(props, ARTIFACT + artifactPlatform + FILE_NAME, artifactFileName);
            safePut(props, ARTIFACT + artifactPlatform + ROOT_ENTRY_NAME, artifactRootEntryName);
            safePut(props, ARTIFACT + artifactPlatform + FILE_EXTENSION, artifactFileExtension);
            safePut(props, ARTIFACT + artifactPlatform + FILE_FORMAT, artifactFileFormat);

            safePut(props, ARTIFACT + artifactPlatformReplaced + NAME, artifactName);
            safePut(props, ARTIFACT + artifactPlatformReplaced + VERSION, artifactVersion);
            safePut(props, ARTIFACT + artifactPlatformReplaced + OS, artifactOs);
            safePut(props, ARTIFACT + artifactPlatformReplaced + ARCH, artifactArch);
            safePut(props, ARTIFACT + artifactPlatformReplaced + FILE, artifactFile);
            safePut(props, ARTIFACT + artifactPlatformReplaced + SIZE, artifactSize);
            safePut(props, ARTIFACT + artifactPlatformReplaced + FILE_NAME, artifactFileName);
            safePut(props, ARTIFACT + artifactPlatformReplaced + ROOT_ENTRY_NAME, artifactRootEntryName);
            safePut(props, ARTIFACT + artifactPlatformReplaced + FILE_EXTENSION, artifactFileExtension);
            safePut(props, ARTIFACT + artifactPlatformReplaced + FILE_FORMAT, artifactFileFormat);

            for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                safePut(props, ARTIFACT + artifactPlatform + CHECKSUM + capitalize(algorithm.formatted()), artifact.getHash(algorithm));
                safePut(props, ARTIFACT + artifactPlatformReplaced + CHECKSUM + capitalize(algorithm.formatted()), artifact.getHash(algorithm));
            }

            safePut(props, ARTIFACT + artifactPlatform + URL, artifactUrl);
            safePut(props, ARTIFACT + artifactPlatformReplaced + URL, artifactUrl);
            props.setAll(context.getModel().getUpload()
                .resolveDownloadUrls(context, distribution, artifact, ARTIFACT + artifactPlatform));
            props.setAll(context.getModel().getUpload()
                .resolveDownloadUrls(context, distribution, artifact, ARTIFACT + artifactPlatformReplaced));

            if (count == 1) {
                props.setAll(context.getModel().getUpload()
                    .resolveDownloadUrls(context, distribution, artifact, DISTRIBUTION));
                safePut(props, KEY_DISTRIBUTION_ARTIFACT, artifact);
                safePut(props, KEY_DISTRIBUTION_URL, artifactUrl);
                safePut(props, KEY_DISTRIBUTION_SIZE, artifactSize);
                safePut(props, KEY_DISTRIBUTION_SHA_256, artifact.getHash(Algorithm.SHA_256));
                for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                    safePut(props, DISTRIBUTION + CHECKSUM + capitalize(algorithm.formatted()), artifact.getHash(algorithm));
                }

                safePut(props, KEY_DISTRIBUTION_ARTIFACT_PLATFORM, platform);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_PLATFORM_REPLACED, platformReplaced);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_NAME, artifactName);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_VERSION, artifactVersion);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_OS, artifactOs);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_ARCH, artifactArch);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_SIZE, artifactSize);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_FILE, artifactFile);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_FILE_NAME, artifactFileName);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME, artifactRootEntryName);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_FILE_EXTENSION, artifactFileExtension);
                safePut(props, KEY_DISTRIBUTION_ARTIFACT_FILE_FORMAT, artifactFileFormat);

                safePut(props, KEY_ARTIFACT_PLATFORM, platform);
                safePut(props, KEY_ARTIFACT_PLATFORM_REPLACED, platformReplaced);
                safePut(props, KEY_ARTIFACT_NAME, artifactName);
                safePut(props, KEY_ARTIFACT_VERSION, artifactVersion);
                safePut(props, KEY_ARTIFACT_OS, artifactOs);
                safePut(props, KEY_ARTIFACT_ARCH, artifactArch);
                safePut(props, KEY_ARTIFACT_SIZE, artifactSize);
                safePut(props, KEY_ARTIFACT_FILE, artifactFile);
                safePut(props, KEY_ARTIFACT_FILE_NAME, artifactFileName);
                safePut(props, KEY_ARTIFACT_ROOT_ENTRY_NAME, artifactRootEntryName);
                safePut(props, KEY_ARTIFACT_FILE_EXTENSION, artifactFileExtension);
                safePut(props, KEY_ARTIFACT_FILE_FORMAT, artifactFileFormat);

                // add extra properties without clobbering existing keys
                Map<String, Object> aprops = artifact.resolvedExtraProperties();
                TemplateContext bprops = new TemplateContext(aprops);
                applyTemplates(aprops, bprops);
                aprops.keySet().stream()
                    .filter(k -> !props.contains(k))
                    .forEach(k -> props.set(k, aprops.get(k)));
            }
        }

        return count > 0;
    }

    protected List<Artifact> collectArtifacts(Distribution distribution) {
        return packager.resolveArtifacts(context, distribution);
    }

    protected void info(ByteArrayOutputStream out) {
        log(out, context.getLogger()::info);
    }

    protected void error(ByteArrayOutputStream err) {
        log(err, context.getLogger()::error);
    }

    private void log(ByteArrayOutputStream stream, Consumer<? super String> consumer) {
        String str = IoUtils.toString(stream);
        if (isBlank(str)) return;

        Arrays.stream(str.split(System.lineSeparator()))
            .forEach(consumer);
    }

    protected Path getPrepareDirectory(TemplateContext props) {
        return props.get(KEY_DISTRIBUTION_PREPARE_DIRECTORY);
    }

    protected Path getPackageDirectory(TemplateContext props) {
        return props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
    }

    protected void safePut(TemplateContext dest, String key, Object value) {
        if (value instanceof CharSequence && isNotBlank(String.valueOf(value)) || null != value) {
            dest.set(key, value);
        }
    }

    protected Optional<Stereotype> resolveStereotype(String fileName) {
        for (Stereotype stereotype : packager.getSupportedStereotypes()) {
            if (fileName.startsWith(stereotype.formatted() + "-")) {
                return Optional.of(stereotype);
            }
        }

        return Optional.empty();
    }
}
