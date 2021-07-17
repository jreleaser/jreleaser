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
package org.jreleaser.assemblers;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Glob;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Jlink;
import org.jreleaser.model.Project;
import org.jreleaser.model.assembler.spi.AssemblerProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.Version;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class JlinkAssemblerProcessor extends AbstractAssemblerProcessor<Jlink> {
    private static final String KEY_JAVA_VERSION = "JAVA_VERSION";

    public JlinkAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doAssemble(Map<String, Object> props) throws AssemblerProcessingException {
        // verify jdk
        Path jdkPath = assembler.getJdk().getEffectivePath(context, assembler);
        Version jdkVersion = Version.of(readJavaVersion(jdkPath));
        context.getLogger().debug("jdk version is {}", jdkVersion);

        // verify jdks
        for (Artifact targetJdk : assembler.getTargetJdks()) {
            Path targetJdkPath = targetJdk.getEffectivePath(context, assembler);
            Version targetJdkVersion = Version.of(readJavaVersion(targetJdkPath));
            context.getLogger().debug("target jdk version is {}", jdkVersion);

            if (jdkVersion.getMajor() != targetJdkVersion.getMajor()) {
                throw new AssemblerProcessingException("Target JDK " + targetJdkVersion +
                    " is not compatible with " + jdkVersion);
            }
        }

        Path assembleDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path inputsDirectory = assembleDirectory.resolve("inputs");

        // copy jars to assembly
        Path jarsDirectory = inputsDirectory.resolve("jars");
        context.getLogger().debug("copying JARs to {}", context.relativizeToBasedir(jarsDirectory));
        Set<Path> jars = copyJars(context, jarsDirectory);

        // resolve module names
        Set<String> moduleNames = resolveModuleNames(context, jdkPath, jars);
        if (isNotBlank(assembler.getModuleName())) {
            moduleNames.add(assembler.getModuleName());
        }
        context.getLogger().debug("resolved moduleNames: {}", moduleNames);

        // run jlink x jdk
        String imageName = assembler.getResolvedImageName(context);
        for (Artifact targetJdk : assembler.getTargetJdks()) {
            Artifact image = jlink(assembleDirectory, jdkPath, targetJdk, moduleNames, imageName);
            if (isNotBlank(assembler.getImageNameTransform())) {
                image.setTransform(assembler.getResolvedImageNameTransform(context) + "-" + targetJdk.getPlatform() + ".zip");
                image.getEffectivePath(context);
            }
        }
    }

    private Artifact jlink(Path assembleDirectory, Path jdkPath, Artifact targetJdk, Set<String> moduleNames, String imageName) throws AssemblerProcessingException {
        String finalImageName = imageName + "-" + targetJdk.getPlatform();
        context.getLogger().info("- {}", finalImageName);

        Path inputsDirectory = assembleDirectory.resolve("inputs");
        Path workDirectory = assembleDirectory.resolve("work-" + targetJdk.getPlatform());
        Path imageDirectory = workDirectory.resolve(finalImageName).toAbsolutePath();
        try {
            FileUtils.deleteFiles(imageDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException("Could not delete previous image " + finalImageName, e);
        }

        // jlink it
        List<String> cmd = new ArrayList<>();
        cmd.add(jdkPath.resolve("bin").resolve("jlink").toAbsolutePath().toString());
        cmd.addAll(assembler.getArgs());
        cmd.add("--module-path");
        cmd.add(targetJdk.getEffectivePath(context).resolve("jmods").toAbsolutePath().toString() + ":" +
            assembleDirectory.resolve("jars").toAbsolutePath());
        cmd.add("--add-modules");
        cmd.add(String.join(",", moduleNames));
        if (isNotBlank(assembler.getModuleName())) {
            cmd.add("--launcher");
            cmd.add(assembler.getExecutable() + "=" + assembler.getModuleName() + "/" + assembler.getJava().getMainClass());
        }
        cmd.add("--output");
        cmd.add(imageDirectory.toString());
        executeCommand(cmd);

        if (isBlank(assembler.getModuleName())) {
            // non modular
            // copy jars & launcher
            Path jarsDirectory = imageDirectory.resolve("jars");

            try {
                Files.createDirectory(jarsDirectory);
                FileUtils.copyFiles(context.getLogger(),
                    inputsDirectory.resolve("jars"),
                    jarsDirectory);
            } catch (IOException e) {
                throw new AssemblerProcessingException("Could not copy JARs to " +
                    context.relativizeToBasedir(jarsDirectory), e);
            }
            try {
                if (PlatformUtils.isWindows(targetJdk.getPlatform())) {
                    Files.copy(inputsDirectory.resolve(assembler.getExecutable().concat(".bat")),
                        imageDirectory.resolve("bin").resolve(assembler.getExecutable().concat(".bat")));
                } else {
                    Path launcher = imageDirectory.resolve("bin").resolve(assembler.getExecutable());
                    Files.copy(inputsDirectory.resolve(assembler.getExecutable()), launcher);
                    FileUtils.grantExecutableAccess(launcher);
                }
            } catch (IOException e) {
                throw new AssemblerProcessingException("Could not copy launcher to " +
                    context.relativizeToBasedir(jarsDirectory), e);
            }
        }

        try {
            Path imageZip = assembleDirectory.resolve(finalImageName + ".zip");
            copyFiles(context, imageDirectory);
            FileUtils.zip(workDirectory, imageZip);

            context.getLogger().debug("- {}", imageZip.getFileName());

            return Artifact.of(imageZip, targetJdk.getPlatform());
        } catch (IOException e) {
            throw new AssemblerProcessingException("Unexpected error", e);
        }
    }

    private String readJavaVersion(Path path) throws AssemblerProcessingException {
        Path release = path.resolve("release");
        if (!Files.exists(release)) {
            throw new AssemblerProcessingException("Invalid JDK [" + path.toAbsolutePath() + "] release file not found");
        }

        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(release));
            if (props.containsKey(KEY_JAVA_VERSION)) {
                String version = props.getProperty(KEY_JAVA_VERSION);
                if (version.startsWith("\"") && version.endsWith("\"")) {
                    return version.substring(1, version.length() - 1);
                }
                return version;
            } else {
                throw new AssemblerProcessingException("Invalid JDK release file [" + release.toAbsolutePath() + "]");
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException("Invalid JDK release file [" + release.toAbsolutePath() + "]", e);
        }
    }

    private Set<Path> copyJars(JReleaserContext context, Path jarsDirectory) throws AssemblerProcessingException {
        Set<Path> paths = new LinkedHashSet<>();

        // resolve all first
        paths.add(assembler.getMainJar().getEffectivePath(context, assembler));
        for (Glob glob : assembler.getJars()) {
            glob.getResolvedPaths(context).stream()
                .filter(Files::isRegularFile)
                .forEach(paths::add);
        }

        // copy all next
        try {
            Files.createDirectories(jarsDirectory);
            for (Path path : paths) {
                context.getLogger().debug("copying {}", path.getFileName());
                Files.copy(path, jarsDirectory.resolve(path.getFileName()), REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException("Unexpected error when copying JAR files", e);
        }

        return paths;
    }

    private Set<Path> copyFiles(JReleaserContext context, Path destination) throws AssemblerProcessingException {
        Set<Path> paths = new LinkedHashSet<>();

        // resolve all first
        for (Glob glob : assembler.getFiles()) {
            glob.getResolvedPaths(context).stream()
                .filter(Files::isRegularFile)
                .forEach(paths::add);
        }

        // copy all next
        try {
            Files.createDirectories(destination);
            for (Path path : paths) {
                context.getLogger().debug("copying {}", path.getFileName());
                Files.copy(path, destination.resolve(path.getFileName()), REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException("Unexpected error when copying files", e);
        }

        return paths;
    }

    private Set<String> resolveModuleNames(JReleaserContext context, Path jdkPath, Set<Path> jars) throws AssemblerProcessingException {
        if (!assembler.getModuleNames().isEmpty()) {
            return assembler.getModuleNames();
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(jdkPath.resolve("bin").resolve("jdeps").toAbsolutePath().toString());
        cmd.add("--multi-release");
        cmd.add("base");
        cmd.add("--print-module-deps");
        for (Path jar : jars) {
            cmd.add(jar.toAbsolutePath().toString());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        executeCommandCapturing(cmd, out);

        return Arrays.stream(out.toString().split(System.lineSeparator()))
            .map(String::trim)
            .collect(Collectors.toSet());
    }

    @Override
    protected void writeFile(Project project, String content, Map<String, Object> props, String fileName)
        throws AssemblerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path inputsDirectory = outputDirectory.resolve("inputs");
        try {
            Files.createDirectories(inputsDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException("Could not create directories", e);
        }

        Path outputFile = "launcher.bat".equals(fileName) ?
            inputsDirectory.resolve(assembler.getExecutable().concat(".bat")) :
            "launcher".equals(fileName) ?
                inputsDirectory.resolve(assembler.getExecutable()) :
                inputsDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
