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
package org.jreleaser.assemblers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.ArchiveAssembler;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.internal.assemble.DebAssembler;
import org.jreleaser.model.internal.assemble.JavaArchiveAssembler;
import org.jreleaser.model.internal.assemble.JlinkAssembler;
import org.jreleaser.model.internal.assemble.NativeImageAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.Errors;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_ARCHITECTURE;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_BREAKS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_BUILT_USING;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_CONFLICTS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_DEPENDS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_DESCRIPTION;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_ENHANCES;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_ESSENTIAL;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_HAS_BREAKS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_HAS_CONFLICTS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_HAS_DEPENDS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_HAS_ENHANCES;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_HAS_PRE_DEPENDS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_HAS_RECOMMENDS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_HAS_SUGGESTS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_HOMEPAGE;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_INSTALLED_SIZE;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_MAINTAINER;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_PACKAGE;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_PRE_DEPENDS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_PRIORITY;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_PROVIDES;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_RECOMMENDS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_REVISION;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_SECTION;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_SUGGESTS;
import static org.jreleaser.model.Constants.KEY_DEB_CONTROL_VERSION;
import static org.jreleaser.model.Constants.KEY_DEB_INSTALLATION_PATH;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_EXECUTABLE_UNIX;
import static org.jreleaser.model.internal.validation.assemble.ArchiveAssemblerResolver.resolveArchiveOutputs;
import static org.jreleaser.model.internal.validation.assemble.JavaArchiveAssemblerResolver.resolveJavaArchiveOutputs;
import static org.jreleaser.model.internal.validation.assemble.JlinkAssemblerResolver.resolveJlinkOutputs;
import static org.jreleaser.model.internal.validation.assemble.NativeImageAssemblerResolver.resolveNativeImageOutputs;
import static org.jreleaser.mustache.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.16.0
 */
public class DebAssemblerProcessor extends AbstractAssemblerProcessor<org.jreleaser.model.api.assemble.DebAssembler, DebAssembler> {
    private static final String CONTROL_DIRECTORY = "control";
    private static final String CONTROL_TAR_ZST = "control.tar.zst";
    private static final String DATA_DIRECTORY = "data";
    private static final String DATA_TAR_ZST = "data.tar.zst";
    private static final String DEBIAN_BINARY = "debian-binary";

    public DebAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void fillAssemblerProperties(TemplateContext props) {
        super.fillAssemblerProperties(props);
        props.set(KEY_DISTRIBUTION_EXECUTABLE_UNIX, assembler.getExecutable());
        props.set(KEY_DEB_INSTALLATION_PATH, assembler.getResolvedInstallationPath(props));
        props.set(KEY_DEB_CONTROL_PACKAGE, assembler.getControl().getPackageName());
        props.set(KEY_DEB_CONTROL_VERSION, assembler.getControl().getPackageVersion());
        props.set(KEY_DEB_CONTROL_REVISION, assembler.getControl().getPackageRevision());
        props.set(KEY_DEB_CONTROL_PROVIDES, assembler.getControl().getProvides());
        props.set(KEY_DEB_CONTROL_MAINTAINER, passThrough(assembler.getControl().getMaintainer()));
        props.set(KEY_DEB_CONTROL_SECTION, assembler.getControl().getSection().value());
        props.set(KEY_DEB_CONTROL_PRIORITY, assembler.getControl().getPriority().value());
        props.set(KEY_DEB_CONTROL_ESSENTIAL, assembler.getControl().isEssential() ? "yes" : "no");
        props.set(KEY_DEB_CONTROL_DESCRIPTION, passThrough(Arrays.stream(assembler.getControl().getDescription().split("\n"))
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining("\n "))));
        props.set(KEY_DEB_CONTROL_HOMEPAGE, assembler.getControl().getHomepage());
        props.set(KEY_DEB_CONTROL_BUILT_USING, passThrough(assembler.getControl().getBuiltUsing()));
        props.set(KEY_DEB_CONTROL_HAS_DEPENDS, !assembler.getControl().getDepends().isEmpty());
        props.set(KEY_DEB_CONTROL_HAS_PRE_DEPENDS, !assembler.getControl().getPreDepends().isEmpty());
        props.set(KEY_DEB_CONTROL_HAS_RECOMMENDS, !assembler.getControl().getRecommends().isEmpty());
        props.set(KEY_DEB_CONTROL_HAS_SUGGESTS, !assembler.getControl().getSuggests().isEmpty());
        props.set(KEY_DEB_CONTROL_HAS_ENHANCES, !assembler.getControl().getEnhances().isEmpty());
        props.set(KEY_DEB_CONTROL_HAS_BREAKS, !assembler.getControl().getBreaks().isEmpty());
        props.set(KEY_DEB_CONTROL_HAS_CONFLICTS, !assembler.getControl().getConflicts().isEmpty());
        props.set(KEY_DEB_CONTROL_DEPENDS, formatDependencies(assembler.getControl().getDepends()));
        props.set(KEY_DEB_CONTROL_PRE_DEPENDS, formatDependencies(assembler.getControl().getPreDepends()));
        props.set(KEY_DEB_CONTROL_RECOMMENDS, formatDependencies(assembler.getControl().getRecommends()));
        props.set(KEY_DEB_CONTROL_SUGGESTS, formatDependencies(assembler.getControl().getSuggests()));
        props.set(KEY_DEB_CONTROL_ENHANCES, formatDependencies(assembler.getControl().getEnhances()));
        props.set(KEY_DEB_CONTROL_BREAKS, formatDependencies(assembler.getControl().getBreaks()));
        props.set(KEY_DEB_CONTROL_CONFLICTS, formatDependencies(assembler.getControl().getConflicts()));
    }

    private String formatDependencies(Set<String> dependencies) {
        return passThrough(String.join(",\n ", dependencies));
    }

    @Override
    protected void doAssemble(TemplateContext props) throws AssemblerProcessingException {
        String assemblerRef = assembler.getAssemblerRef();

        if (isNotBlank(assemblerRef)) {
            Assembler<?> assemblerReference = resolveAssemblerReference(assemblerRef.trim());
            for (Artifact artifact : assemblerReference.getOutputs()) {
                assembleDebianArtifact(props, artifact);
            }
        } else {
            assembleDebianArtifact(props, null);
        }
    }

    private void assembleDebianArtifact(TemplateContext props, Artifact artifact) throws AssemblerProcessingException {
        Path assembleDirectory = props.get(KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path workDirectory = assembleDirectory.resolve(WORK_DIRECTORY);

        String architecture = isNotBlank(assembler.getArchitecture()) ? assembler.getArchitecture().trim() : "all";
        if (null != artifact) {
            String platform = artifact.getPlatform();

            if (isBlank(platform)) {
                architecture = "all";
            } else if (PlatformUtils.isLinux(platform) && PlatformUtils.isIntel64(platform)) {
                architecture = "amd64";
            } else if (PlatformUtils.isLinux(platform) && PlatformUtils.isArm64(platform)) {
                architecture = "arm64";
            } else {
                context.getLogger().debug(RB.$("assemble.deb.unsupported.artifact", platform));
                return;
            }
        }

        Path architectureDirectory = workDirectory.resolve(architecture);
        Path dataDirectory = architectureDirectory.resolve(DATA_DIRECTORY);

        try {
            Files.createDirectories(dataDirectory);
            if (null != artifact) {
                FileUtils.unpackArchive(artifact.getResolvedPath(context, assembler), dataDirectory);
            }

            props.set(KEY_DEB_CONTROL_ARCHITECTURE, architecture);
            context.getLogger().debug(RB.$("assembler.copy.files"), context.relativizeToBasedir(dataDirectory));
            //copyArtifacts(context, dataDirectory, PlatformUtils.getCurrentFull(), assembler.isAttachPlatform());
            copyFiles(context, dataDirectory);
            copyFileSets(context, dataDirectory);

            long size = org.apache.commons.io.FileUtils.sizeOfDirectory(dataDirectory.toFile()) / 1024;
            props.set(KEY_DEB_CONTROL_INSTALLED_SIZE, String.valueOf(size));
            copyTemplates(context, props, architectureDirectory);

            createControlArchive(props, architectureDirectory);
            createDataArchive(props, architectureDirectory);
            createDebArchive(props, architectureDirectory);

        } catch (IOException ioe) {
            throw new AssemblerProcessingException(ioe);
        }
    }

    private void createControlArchive(TemplateContext props, Path architectureDirectory) throws AssemblerProcessingException {
        Path controlDirectory = architectureDirectory.resolve(CONTROL_DIRECTORY);

        try {
            FileUtils.packArchive(controlDirectory, architectureDirectory.resolve(CONTROL_TAR_ZST),
                new FileUtils.ArchiveOptions()
                    .withTimestamp(context.getModel().resolveArchiveTimestamp()));
        } catch (IOException e) {
            throw new AssemblerProcessingException(e);
        }
    }

    private void createDataArchive(TemplateContext props, Path architectureDirectory) throws AssemblerProcessingException {
        Path dataDirectory = architectureDirectory.resolve(DATA_DIRECTORY);

        try {
            FileUtils.packArchive(dataDirectory, architectureDirectory.resolve(DATA_TAR_ZST),
                new FileUtils.ArchiveOptions()
                    .withTimestamp(context.getModel().resolveArchiveTimestamp())
                    .withRootEntryName(assembler.getResolvedInstallationPath(props))
                    .withCreateIntermediateDirs(true));
        } catch (IOException e) {
            throw new AssemblerProcessingException(e);
        }
    }

    private void createDebArchive(TemplateContext props, Path architectureDirectory) throws AssemblerProcessingException {
        Path assembleDirectory = props.get(KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path debianBinaryPath = architectureDirectory.resolve(DEBIAN_BINARY);

        try {
            Files.deleteIfExists(debianBinaryPath);
            Path debianBinaryFile = Files.createFile(debianBinaryPath);
            Files.write(debianBinaryFile, "2.0\n".getBytes(UTF_8));
        } catch (IOException e) {
            throw new AssemblerProcessingException(e);
        }

        Path debianPackage = assembleDirectory.resolve(
            assembler.getControl().getPackageName() + "-" +
                assembler.getControl().getPackageVersion() + "-" +
                assembler.getControl().getPackageRevision() + "_" +
                props.get(KEY_DEB_CONTROL_ARCHITECTURE) + ".deb");

        context.getLogger().info("- {}", debianPackage.getFileName());

        try {
            Files.deleteIfExists(debianPackage);
            FileUtils.ar(architectureDirectory, debianPackage,
                new FileUtils.ArchiveOptions()
                    .withTimestamp(context.getModel().resolveArchiveTimestamp())
                    .withLongFileMode(FileUtils.ArchiveOptions.TarMode.POSIX)
                    .withIncludedPath(debianBinaryPath)
                    .withIncludedPath(architectureDirectory.resolve(CONTROL_TAR_ZST))
                    .withIncludedPath(architectureDirectory.resolve(DATA_TAR_ZST)));
        } catch (IOException e) {
            throw new AssemblerProcessingException(e);
        }
    }

    private Assembler<?> resolveAssemblerReference(String assemblerName) throws AssemblerProcessingException {
        Assembler<?> assemblerReference = context.getModel().getAssemble().findAssembler(assemblerName);

        Errors errors = new Errors();
        if (assemblerReference instanceof ArchiveAssembler) {
            resolveArchiveOutputs(context, (ArchiveAssembler) assemblerReference, errors);
        } else if (assemblerReference instanceof JavaArchiveAssembler) {
            resolveJavaArchiveOutputs(context, (JavaArchiveAssembler) assemblerReference, errors);
        } else if (assemblerReference instanceof JlinkAssembler) {
            resolveJlinkOutputs(context, (JlinkAssembler) assemblerReference, errors);
        } else if (assemblerReference instanceof NativeImageAssembler) {
            resolveNativeImageOutputs(context, (NativeImageAssembler) assemblerReference, errors);
        }

        if (errors.hasErrors()) {
            throw new AssemblerProcessingException(errors.asString());
        }

        return assemblerReference;
    }
}
