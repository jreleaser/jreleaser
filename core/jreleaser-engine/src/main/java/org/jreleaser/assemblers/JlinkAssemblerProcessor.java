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
import org.jreleaser.model.Archive;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.JlinkAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.JvmOptions;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.StringUtils;
import org.jreleaser.version.SemanticVersion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.assemblers.AssemblerUtils.copyJars;
import static org.jreleaser.assemblers.AssemblerUtils.readJavaVersion;
import static org.jreleaser.model.Constants.KEY_ARCHIVE_FORMAT;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_EXECUTABLE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_LINUX;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_OSX;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_UNIVERSAL;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_UNIX;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_WINDOWS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_LINUX;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_OSX;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_UNIVERSAL;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_UNIX;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_WINDOWS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_JAR;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.mustache.MustacheUtils.passThrough;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.FileType.BAT;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileUtils.listFilesAndConsume;
import static org.jreleaser.util.FileUtils.listFilesAndProcess;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class JlinkAssemblerProcessor extends AbstractAssemblerProcessor<org.jreleaser.model.api.assemble.JlinkAssembler, JlinkAssembler> {
    public JlinkAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void fillAssemblerProperties(TemplateContext props) {
        super.fillAssemblerProperties(props);
        if (isNotBlank(assembler.getMainJar().getPath())) {
            props.set(KEY_DISTRIBUTION_JAVA_MAIN_JAR, assembler.getMainJar().getEffectivePath(context, assembler)
                .getFileName());
        } else {
            props.set(KEY_DISTRIBUTION_JAVA_MAIN_JAR, "");
        }
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, assembler.getJava().getMainClass());
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_MODULE, assembler.getJava().getMainModule());
        JvmOptions jvmOptions = assembler.getJava().getJvmOptions();
        props.set(KEY_DISTRIBUTION_EXECUTABLE, assembler.getExecutable());
        props.set(KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_UNIVERSAL,
            !jvmOptions.getUniversal().isEmpty() ? passThrough(join(" ", jvmOptions.getResolvedUniversal(context))) : "");
        props.set(KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_UNIX,
            !jvmOptions.getUnix().isEmpty() ? passThrough(join(" ", jvmOptions.getResolvedUnix(context))) : "");
        props.set(KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_LINUX,
            !jvmOptions.getLinux().isEmpty() ? passThrough(join(" ", jvmOptions.getResolvedLinux(context))) : "");
        props.set(KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_OSX,
            !jvmOptions.getOsx().isEmpty() ? passThrough(join(" ", jvmOptions.getResolvedOsx(context))) : "");
        props.set(KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_WINDOWS,
            !jvmOptions.getWindows().isEmpty() ? passThrough(join(" ", jvmOptions.getResolvedWindows(context))) : "");
        props.set(KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_UNIVERSAL,
            assembler.getJava().getEnvironmentVariables().getResolvedUniversal(context).entrySet());
        props.set(KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_UNIX,
            assembler.getJava().getEnvironmentVariables().getResolvedUnix(context).entrySet());
        props.set(KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_LINUX,
            assembler.getJava().getEnvironmentVariables().getResolvedLinux(context).entrySet());
        props.set(KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_OSX,
            assembler.getJava().getEnvironmentVariables().getResolvedOsx(context).entrySet());
        props.set(KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_WINDOWS,
            assembler.getJava().getEnvironmentVariables().getResolvedWindows(context).entrySet());
    }

    @Override
    protected void doAssemble(TemplateContext props) throws AssemblerProcessingException {
        // verify jdk
        Path jdkPath = assembler.getJdk().getEffectivePath(context, assembler);
        SemanticVersion jdkVersion = SemanticVersion.of(readJavaVersion(jdkPath));
        context.getLogger().debug(RB.$("assembler.jlink.jdk"), jdkVersion, jdkPath.toAbsolutePath().toString());

        boolean selectedJdks = false;
        // verify jdks
        for (Artifact targetJdk : assembler.getTargetJdks()) {
            if (!targetJdk.isActiveAndSelected()) continue;
            selectedJdks = true;

            Path targetJdkPath = targetJdk.getEffectivePath(context, assembler);
            SemanticVersion targetJdkVersion = SemanticVersion.of(readJavaVersion(targetJdkPath));
            context.getLogger().debug(RB.$("assembler.jlink.target"), jdkVersion, targetJdkPath.toAbsolutePath().toString());

            if (jdkVersion.getMajor() != targetJdkVersion.getMajor()) {
                throw new AssemblerProcessingException(RB.$("ERROR_jlink_target_not_compatible", targetJdkVersion, jdkVersion));
            }
        }

        if (!selectedJdks) return;

        Path assembleDirectory = props.get(KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path inputsDirectory = assembleDirectory.resolve(INPUTS_DIRECTORY);

        // copy templates
        copyTemplates(context, props, inputsDirectory);

        // run jlink x jdk
        String imageName = assembler.getResolvedImageName(context);
        if (isNotBlank(assembler.getImageNameTransform())) {
            imageName = assembler.getResolvedImageNameTransform(context);
        }

        boolean hasJavaArchive = assembler.getJavaArchive().isSet();

        if (hasJavaArchive) {
            String archiveFile = resolveTemplate(assembler.getJavaArchive().getPath(), props);
            Path archivePath = context.getBasedir().resolve(Paths.get(archiveFile));
            if (!Files.exists(archivePath)) {
                throw new AssemblerProcessingException(RB.$("ERROR_path_does_not_exist", archivePath));
            }

            Path archiveDirectory = inputsDirectory.resolve(ARCHIVE_DIRECTORY);
            try {
                FileUtils.unpackArchive(archivePath, archiveDirectory, true);
            } catch (IOException e) {
                throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
            }
        }

        // copy jars to assembly
        Path jarsDirectory = inputsDirectory.resolve(JARS_DIRECTORY);
        Path universalJarsDirectory = jarsDirectory.resolve(UNIVERSAL_DIRECTORY);

        if (hasJavaArchive) {
            String libDirectoryName = resolveTemplate(assembler.getJavaArchive().getLibDirectoryName(), props);
            Path libPath = inputsDirectory.resolve(ARCHIVE_DIRECTORY).resolve(libDirectoryName);
            try {
                FileUtils.copyFiles(context.getLogger(), libPath, universalJarsDirectory);
            } catch (IOException e) {
                throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
            }
        }
        context.getLogger().debug(RB.$("assembler.copy.jars"), context.relativizeToBasedir(universalJarsDirectory));
        copyJars(context, assembler, universalJarsDirectory, "");

        Optional<String> compress = assembler.getArgs().stream()
            .filter(arg -> arg.contains("--compress") || arg.startsWith("-c=") || arg.startsWith("-c "))
            .findFirst();
        if (!compress.isPresent()) {
            if (jdkVersion.getMajor() >= 21) {
                assembler.getArgs().add("--compress");
                assembler.getArgs().add("zip-9");
            } else {
                assembler.getArgs().add("--compress=2");
            }
        }

        for (Artifact targetJdk : assembler.getTargetJdks()) {
            if (!targetJdk.isActiveAndSelected()) continue;

            String platform = targetJdk.getPlatform();
            Path platformJarsDirectory = jarsDirectory.resolve(platform);
            context.getLogger().debug(RB.$("assembler.copy.jars"), context.relativizeToBasedir(platformJarsDirectory));
            copyJars(context, assembler, platformJarsDirectory, platform);

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
                .getOrDefault(KEY_ARCHIVE_FORMAT, assembler.getArchiveFormat())
                .toString();
            Archive.Format archiveFormat = Archive.Format.of(str);

            jlink(props, assembleDirectory, jdkPath, targetJdk, moduleNames, imageName, archiveFormat);
        }
    }

    private void jlink(TemplateContext props, Path assembleDirectory, Path jdkPath, Artifact targetJdk, Set<String> moduleNames, String imageName, Archive.Format archiveFormat) throws AssemblerProcessingException {
        String platform = targetJdk.getPlatform();
        String platformReplaced = assembler.getPlatform().applyReplacements(platform);
        String finalImageName = imageName + "-" + platformReplaced;
        context.getLogger().info("- {}", finalImageName);

        boolean hasJavaArchive = assembler.getJavaArchive().isSet();
        Path inputsDirectory = assembleDirectory.resolve(INPUTS_DIRECTORY);
        Path archiveDirectory = inputsDirectory.resolve(ARCHIVE_DIRECTORY);
        Path jarsDirectory = inputsDirectory.resolve(JARS_DIRECTORY);
        Path workDirectory = assembleDirectory.resolve(WORK_DIRECTORY + "-" + platform);
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
                .resolve(UNIVERSAL_DIRECTORY)
                .toAbsolutePath().toString());

            try {
                Path platformJarsDirectory = jarsDirectory.resolve(platform).toAbsolutePath();
                if (listFilesAndProcess(platformJarsDirectory, Stream::count).orElse(0L) > 1) {
                    modulePath += File.pathSeparator + maybeQuote(platformJarsDirectory.toString());
                }
            } catch (IOException e) {
                throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error", e));
            }
        }

        Path jlinkExecutable = jdkPath
            .resolve(BIN_DIRECTORY)
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
        executeCommand(cmd);

        if (isBlank(moduleName)) {
            // non modular
            // copy jars & launcher

            if (assembler.isCopyJars()) {
                Path outputJarsDirectory = imageDirectory.resolve(JARS_DIRECTORY);

                try {
                    Files.createDirectory(outputJarsDirectory);
                    FileUtils.copyFiles(context.getLogger(),
                        jarsDirectory.resolve(UNIVERSAL_DIRECTORY),
                        outputJarsDirectory);
                    FileUtils.copyFiles(context.getLogger(),
                        jarsDirectory.resolve(platform),
                        outputJarsDirectory);
                } catch (IOException e) {
                    throw new AssemblerProcessingException(RB.$("ERROR_assembler_copy_jars",
                        context.relativizeToBasedir(outputJarsDirectory)), e);
                }
            }

            Path binDirectory = imageDirectory.resolve(BIN_DIRECTORY);
            try {
                Files.createDirectories(binDirectory);

                Optional<Set<Path>> launchers = listFilesAndProcess(inputsDirectory.resolve(BIN_DIRECTORY), files -> files.collect(toSet()));
                if (launchers.isPresent()) {
                    for (Path srcLauncher : launchers.get()) {
                        Path destLauncher = binDirectory.resolve(srcLauncher.getFileName());
                        Files.copy(srcLauncher, destLauncher);
                        FileUtils.grantExecutableAccess(destLauncher);
                    }
                }
            } catch (IOException e) {
                throw new AssemblerProcessingException(RB.$("ERROR_assembler_copy_launcher",
                    context.relativizeToBasedir(binDirectory)), e);
            }
        }

        try {
            Path imageArchive = assembleDirectory.resolve(finalImageName + "." + archiveFormat.extension());
            FileUtils.copyFiles(context.getLogger(),
                context.getBasedir(),
                imageDirectory, path -> path.getFileName().startsWith(LICENSE));
            // copy all templates, filter existing launchers
            FileUtils.copyFiles(context.getLogger(), inputsDirectory, imageDirectory, path -> {
                if (!BIN_DIRECTORY.equals(path.getParent().getFileName().toString())) return true;
                String fileName = path.getFileName().toString();
                // don't copy jars twice
                if (fileName.endsWith(JAR.extension()) && JARS_DIRECTORY.equals(path.getParent().getParent().getFileName().toString())) {
                    return false;
                }
                Path candidateBinary = imageDirectory.resolve(BIN_DIRECTORY).resolve(fileName);
                return !Files.exists(candidateBinary);
            });

            if (hasJavaArchive) {
                String libDirectory = resolveTemplate(assembler.getJavaArchive().getLibDirectoryName(), props);
                String archivePathName = archiveDirectory.toString();
                FileUtils.copyFiles(context.getLogger(),
                    archiveDirectory,
                    imageDirectory, path -> {
                        String fileName = path.getFileName().toString();
                        if (!fileName.endsWith(JAR.extension())) return true;
                        return !path.getParent().toString()
                            .substring(archivePathName.length())
                            .contains(libDirectory);
                    });
            }

            copyArtifacts(context, imageDirectory, platform, true);
            copyFiles(context, imageDirectory);
            copyFileSets(context, imageDirectory);
            generateSwidTag(context, imageDirectory);

            FileUtils.packArchive(workDirectory, imageArchive, assembler.getOptions().toOptions());

            context.getLogger().debug("- {}", imageArchive.getFileName());
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private Set<String> resolveModuleNames(JReleaserContext context, Path jdkPath, Path jarsDirectory, String platform, TemplateContext props) throws AssemblerProcessingException {
        if (!assembler.getModuleNames().isEmpty()) {
            return assembler.getModuleNames();
        }

        Path jdepsExecutable = jdkPath
            .resolve(BIN_DIRECTORY)
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
                cmd.arg(UNIVERSAL_DIRECTORY +
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
                .map(context::relativizeToBasedir)
                .map(p -> p.toAbsolutePath().normalize().toString())
                .forEach(cmd::arg);
        } else {
            calculateJarPath(jarsDirectory, platform, cmd, false);
        }

        context.getLogger().debug(String.join(" ", cmd.getArgs()));
        Command.Result result = executeCommand(jarsDirectory, cmd);

        String output = result.getOut();
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

                listFilesAndProcess(jarsDirectory.resolve(UNIVERSAL_DIRECTORY), files ->
                    files.map(Path::toAbsolutePath)
                        .map(Object::toString)
                        .collect(joining(File.pathSeparator)))
                    .ifPresent(pathBuilder::append);

                listFilesAndProcess(jarsDirectory.resolve(platform), files ->
                    files.map(Path::toAbsolutePath)
                        .map(Object::toString)
                        .collect(joining(File.pathSeparator)))
                    .ifPresent(platformSpecific -> {
                        if (isNotBlank(platformSpecific)) {
                            pathBuilder.append(File.pathSeparator)
                                .append(platformSpecific);
                        }
                    });

                cmd.arg(pathBuilder.toString());
            } else {
                listFilesAndConsume(jarsDirectory.resolve(UNIVERSAL_DIRECTORY), files ->
                    files.map(Path::toAbsolutePath)
                        .map(Object::toString)
                        .forEach(cmd::arg));

                listFilesAndConsume(jarsDirectory.resolve(platform), files ->
                    files.map(Path::toAbsolutePath)
                        .map(Object::toString)
                        .forEach(cmd::arg));
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_jdeps_error", e.getMessage(), e));
        }
    }

    @Override
    protected Path resolveOutputFile(TemplateContext props, Path targetDirectory, String fileName) throws AssemblerProcessingException {
        String executableName = assembler.getExecutable();

        return "bin/launcher.bat".equals(fileName) ?
            targetDirectory.resolve(BIN_DIRECTORY).resolve(executableName.concat(BAT.extension())) :
            "bin/launcher".equals(fileName) ?
                targetDirectory.resolve(BIN_DIRECTORY).resolve(executableName) :
                targetDirectory.resolve(fileName);
    }
}
