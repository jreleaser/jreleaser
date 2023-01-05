/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.Archive;
import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.JlinkAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.IoUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.StringUtils;
import org.jreleaser.version.SemanticVersion;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.assemblers.AssemblerUtils.copyJars;
import static org.jreleaser.assemblers.AssemblerUtils.readJavaVersion;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.FileUtils.listFilesAndConsume;
import static org.jreleaser.util.FileUtils.listFilesAndProcess;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class JlinkAssemblerProcessor extends AbstractJavaAssemblerProcessor<org.jreleaser.model.api.assemble.JlinkAssembler, JlinkAssembler> {
    public JlinkAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doAssemble(TemplateContext props) throws AssemblerProcessingException {
        // verify jdk
        Path jdkPath = assembler.getJdk().getEffectivePath(context, assembler);
        SemanticVersion jdkVersion = SemanticVersion.of(readJavaVersion(jdkPath));
        context.getLogger().debug(RB.$("assembler.jlink.jdk"), jdkVersion, jdkPath.toAbsolutePath().toString());

        // verify jdks
        for (Artifact targetJdk : assembler.getTargetJdks()) {
            if (!context.isPlatformSelected(targetJdk)) continue;

            Path targetJdkPath = targetJdk.getEffectivePath(context, assembler);
            SemanticVersion targetJdkVersion = SemanticVersion.of(readJavaVersion(targetJdkPath));
            context.getLogger().debug(RB.$("assembler.jlink.target"), jdkVersion, targetJdkPath.toAbsolutePath().toString());

            if (jdkVersion.getMajor() != targetJdkVersion.getMajor()) {
                throw new AssemblerProcessingException(RB.$("ERROR_jlink_target_not_compatible", targetJdkVersion, jdkVersion));
            }
        }

        Path assembleDirectory = props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path inputsDirectory = assembleDirectory.resolve("inputs");

        // run jlink x jdk
        String imageName = assembler.getResolvedImageName(context);
        if (isNotBlank(assembler.getImageNameTransform())) {
            imageName = assembler.getResolvedImageNameTransform(context);
        }

        for (Artifact targetJdk : assembler.getTargetJdks()) {
            if (!context.isPlatformSelected(targetJdk)) continue;

            String platform = targetJdk.getPlatform();
            // copy jars to assembly
            Path jarsDirectory = inputsDirectory.resolve("jars");
            Path universalJarsDirectory = jarsDirectory.resolve("universal");
            context.getLogger().debug(RB.$("assembler.copy.jars"), context.relativizeToBasedir(universalJarsDirectory));
            Set<Path> jars = copyJars(context, assembler, universalJarsDirectory, "");
            Path platformJarsDirectory = jarsDirectory.resolve(platform);
            context.getLogger().debug(RB.$("assembler.copy.jars"), context.relativizeToBasedir(platformJarsDirectory));
            jars.addAll(copyJars(context, assembler, platformJarsDirectory, platform));

            // resolve module names
            Set<String> moduleNames = new TreeSet<>(resolveModuleNames(context, jdkPath, jarsDirectory, platform, props));
            context.getLogger().debug(RB.$("assembler.resolved.module.names"), moduleNames);
            if (moduleNames.isEmpty()) {
                throw new AssemblerProcessingException(RB.$("ERROR_assembler_no_module_names"));
            }
            moduleNames.addAll(assembler.getAdditionalModuleNames());
            if (isNotBlank(assembler.getJava().getMainModule())) {
                moduleNames.add(assembler.getJava().getMainModule());
            }
            context.getLogger().debug(RB.$("assembler.module.names"), moduleNames);

            String str = targetJdk.getExtraProperties()
                .getOrDefault("archiveFormat", "ZIP")
                .toString();
            Archive.Format archiveFormat = Archive.Format.of(str);

            jlink(assembleDirectory, jdkPath, targetJdk, moduleNames, imageName, archiveFormat);
        }
    }

    private Artifact jlink(Path assembleDirectory, Path jdkPath, Artifact targetJdk, Set<String> moduleNames, String imageName, Archive.Format archiveFormat) throws AssemblerProcessingException {
        String platform = targetJdk.getPlatform();
        String platformReplaced = assembler.getPlatform().applyReplacements(platform);
        String finalImageName = imageName + "-" + platformReplaced;
        context.getLogger().info("- {}", finalImageName);

        Path inputsDirectory = assembleDirectory.resolve("inputs");
        Path jarsDirectory = inputsDirectory.resolve("jars");
        Path workDirectory = assembleDirectory.resolve("work-" + platform);
        Path imageDirectory = workDirectory.resolve(finalImageName).toAbsolutePath();
        try {
            FileUtils.deleteFiles(imageDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_delete_image", finalImageName), e);
        }

        // jlink it
        String moduleName = assembler.getJava().getMainModule();
        String modulePath = maybeQuote(targetJdk.getEffectivePath(context, assembler).resolve("jmods").toAbsolutePath().toString());
        if (isNotBlank(moduleName) || assembler.isCopyJars()) {
            modulePath += File.pathSeparator + maybeQuote(jarsDirectory
                .resolve("universal")
                .toAbsolutePath().toString());

            try {
                Path platformJarsDirectory = jarsDirectory.resolve(platform).toAbsolutePath();
                if (listFilesAndProcess(platformJarsDirectory, Stream::count) > 1) {
                    modulePath += File.pathSeparator + maybeQuote(platformJarsDirectory.toString());
                }
            } catch (IOException e) {
                throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error", e));
            }
        }

        Path jlinkExecutable = jdkPath
            .resolve("bin")
            .resolve(PlatformUtils.isWindows() ? "jlink.exe" : "jlink")
            .toAbsolutePath();

        Command cmd = new Command(jlinkExecutable.toString(), true)
            .args(assembler.getArgs())
            .arg("--module-path")
            .arg(modulePath)
            .arg("--add-modules")
            .arg(String.join(",", moduleNames));
        if (isNotBlank(moduleName)) {
            cmd.arg("--launcher")
                .arg(assembler.getExecutable() + "=" + moduleName + "/" + assembler.getJava().getMainClass());
        }
        cmd.arg("--output")
            .arg(maybeQuote(imageDirectory.toString()));

        context.getLogger().debug(String.join(" ", cmd.getArgs()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        executeCommandCapturing(cmd, out);

        if (isBlank(moduleName)) {
            // non modular
            // copy jars & launcher

            if (assembler.isCopyJars()) {
                Path outputJarsDirectory = imageDirectory.resolve("jars");

                try {
                    Files.createDirectory(outputJarsDirectory);
                    FileUtils.copyFiles(context.getLogger(),
                        jarsDirectory.resolve("universal"),
                        outputJarsDirectory);
                    FileUtils.copyFiles(context.getLogger(),
                        jarsDirectory.resolve(platform),
                        outputJarsDirectory);
                } catch (IOException e) {
                    throw new AssemblerProcessingException(RB.$("ERROR_assembler_copy_jars",
                        context.relativizeToBasedir(outputJarsDirectory)), e);
                }
            }

            try {
                if (PlatformUtils.isWindows(platform)) {
                    Files.copy(inputsDirectory.resolve(assembler.getExecutable().concat(".bat")),
                        imageDirectory.resolve("bin").resolve(assembler.getExecutable().concat(".bat")));
                } else {
                    Path launcher = imageDirectory.resolve("bin").resolve(assembler.getExecutable());
                    Files.copy(inputsDirectory.resolve(assembler.getExecutable()), launcher);
                    FileUtils.grantExecutableAccess(launcher);
                }
            } catch (IOException e) {
                throw new AssemblerProcessingException(RB.$("ERROR_assembler_copy_launcher",
                    context.relativizeToBasedir(imageDirectory.resolve("bin"))), e);
            }
        }

        try {
            Path imageArchive = assembleDirectory.resolve(finalImageName + "." + archiveFormat.extension());
            FileUtils.copyFiles(context.getLogger(),
                context.getBasedir(),
                imageDirectory, path -> path.getFileName().startsWith("LICENSE"));
            copyFiles(context, imageDirectory);
            copyFileSets(context, imageDirectory);

            FileUtils.packArchive(workDirectory, imageArchive, context.getModel().resolveArchiveTimestamp());

            context.getLogger().debug("- {}", imageArchive.getFileName());

            return Artifact.of(imageArchive, platform);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private Set<String> resolveModuleNames(JReleaserContext context, Path jdkPath, Path jarsDirectory, String platform, TemplateContext props) throws AssemblerProcessingException {
        if (!assembler.getModuleNames().isEmpty()) {
            return assembler.getModuleNames();
        }

        Path jdepsExecutable = jdkPath
            .resolve("bin")
            .resolve(PlatformUtils.isWindows() ? "jdeps.exe" : "jdeps")
            .toAbsolutePath();

        Command cmd = new Command(jdepsExecutable.toAbsolutePath().toString());
        String multiRelease = assembler.getJdeps().getMultiRelease();
        if (isNotBlank(multiRelease)) {
            cmd.arg("--multi-release")
                .arg(multiRelease);
        }
        if (assembler.getJdeps().isIgnoreMissingDeps()) {
            cmd.arg("--ignore-missing-deps");
        }
        cmd.arg("--print-module-deps");

        String moduleName = assembler.getJava().getMainModule();
        if (isNotBlank(moduleName)) {
            cmd.arg("--module")
                .arg(moduleName)
                .arg("--module-path");
            calculateJarPath(jarsDirectory, platform, cmd, true);
        } else if (!assembler.getJdeps().getTargets().isEmpty()) {
            cmd.arg("--class-path");
            if (assembler.getJdeps().isUseWildcardInPath()) {
                cmd.arg("universal" +
                    File.separator + "*" +
                    File.pathSeparator +
                    platform +
                    File.separator + "*");
            } else {
                calculateJarPath(jarsDirectory, platform, cmd, true);
            }

            assembler.getJdeps().getTargets().stream()
                .map(target -> resolveTemplate(target, props))
                .filter(StringUtils::isNotBlank)
                .map(AssemblerUtils::maybeAdjust)
                .forEach(cmd::arg);
        } else {
            calculateJarPath(jarsDirectory, platform, cmd, false);
        }

        context.getLogger().debug(String.join(" ", cmd.getArgs()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        executeCommandCapturing(jarsDirectory, cmd, out);

        String output = IoUtils.toString(out).trim();
        long lineCount = Arrays.stream(output.split(System.lineSeparator()))
            .map(String::trim)
            .count();

        if (lineCount == 1 && isNotBlank(output)) {
            return Arrays.stream(output.split(",")).collect(toSet());
        }

        throw new AssemblerProcessingException(RB.$("ERROR_assembler_jdeps_error", output));
    }

    private void calculateJarPath(Path jarsDirectory, String platform, Command cmd, boolean join) throws AssemblerProcessingException {
        try {
            if (join) {
                StringBuilder pathBuilder = new StringBuilder();

                String s = listFilesAndProcess(jarsDirectory.resolve("universal"), files ->
                    files.map(jarsDirectory::relativize)
                        .map(Object::toString)
                        .collect(joining(File.pathSeparator)));
                pathBuilder.append(s);

                String platformSpecific = listFilesAndProcess(jarsDirectory.resolve(platform), files ->
                    files.map(jarsDirectory::relativize)
                        .map(Object::toString)
                        .collect(joining(File.pathSeparator)));

                if (isNotBlank(platformSpecific)) {
                    pathBuilder.append(File.pathSeparator)
                        .append(platformSpecific);
                }

                cmd.arg(pathBuilder.toString());
            } else {
                listFilesAndConsume(jarsDirectory.resolve("universal"), files ->
                    files.map(jarsDirectory::relativize)
                        .map(Object::toString)
                        .forEach(cmd::arg));

                listFilesAndConsume(jarsDirectory.resolve(platform), files ->
                    files.map(jarsDirectory::relativize)
                        .map(Object::toString)
                        .forEach(cmd::arg));
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_jdeps_error", e.getMessage()));
        }
    }

    @Override
    protected void writeFile(String content, TemplateContext props, String fileName)
        throws AssemblerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path inputsDirectory = outputDirectory.resolve("inputs");
        try {
            Files.createDirectories(inputsDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_create_directories"), e);
        }

        Path outputFile = "launcher.bat".equals(fileName) ?
            inputsDirectory.resolve(assembler.getExecutable().concat(".bat")) :
            "launcher".equals(fileName) ?
                inputsDirectory.resolve(assembler.getExecutable()) :
                inputsDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
