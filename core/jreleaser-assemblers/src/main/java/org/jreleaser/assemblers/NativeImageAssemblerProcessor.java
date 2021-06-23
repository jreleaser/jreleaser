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
import org.jreleaser.model.NativeImage;
import org.jreleaser.model.Project;
import org.jreleaser.model.assembler.spi.AssemblerProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class NativeImageAssemblerProcessor extends AbstractAssemblerProcessor<NativeImage> {
    private static final String KEY_JAVA_VERSION = "JAVA_VERSION";
    private static final String KEY_GRAALVM_VERSION = "GRAALVM_VERSION";

    public NativeImageAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doAssemble(Map<String, Object> props) throws AssemblerProcessingException {
        // verify graal
        Path graalPath = assembler.getGraal().getEffectivePath(context, assembler);
        Version javaVersion = Version.of(readJavaVersion(graalPath));
        Version graalVersion = Version.of(readGraalVersion(graalPath));
        context.getLogger().debug("java version is {}", javaVersion);
        context.getLogger().debug("graal version is {}", graalVersion);

        // copy jars to assembly
        Path assembleDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path libDirectory = assembleDirectory.resolve("lib");
        context.getLogger().debug("copying JARs to {}", context.relativizeToBasedir(libDirectory));
        Set<Path> jars = copyJars(context, libDirectory);

        // install native-image
        installNativeImage(graalPath);

        // run native-image
        nativeImage(assembleDirectory, graalPath, jars);
    }

    private void installNativeImage(Path graalPath) throws AssemblerProcessingException {
        Path nativeImageExecutable = graalPath
            .resolve("bin")
            .resolve(PlatformUtils.isWindows() ? "native-image.exe" : "native-image")
            .toAbsolutePath();

        if (!Files.exists(nativeImageExecutable)) {
            context.getLogger().debug("installing native-image executable");
            List<String> cmd = new ArrayList<>();
            cmd.add(graalPath.resolve("bin").resolve("gu").toAbsolutePath().toString());
            cmd.add("install");
            cmd.add("native-image");
            executeCommand(cmd);
        }
    }

    private Artifact nativeImage(Path assembleDirectory, Path graalPath, Set<Path> jars) throws AssemblerProcessingException {
        String executable = assembler.getExecutable();
        context.getLogger().info("- {}", executable);

        Path image = assembleDirectory.resolve(executable).toAbsolutePath();
        try {
            if (Files.exists(image)) {
                Files.deleteIfExists(image);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException("Could not delete previous image " + executable, e);
        }

        assembler.getArgs().stream()
            .filter(arg -> arg.startsWith("-H:Name"))
            .findFirst()
            .ifPresent(assembler.getArgs()::remove);

        List<String> cmd = new ArrayList<>();
        cmd.add(graalPath.resolve("bin").resolve("native-image").toAbsolutePath().toString());
        cmd.addAll(assembler.getArgs());
        cmd.add("-jar");
        cmd.add(assembler.getMainJar().getEffectivePath(context).toAbsolutePath().toString());
        if (!jars.isEmpty()) {
            cmd.add("-cp");
            cmd.add(jars.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator)));
        }
        cmd.add("-H:Name=" + image.getFileName().toString());
        executeCommand(image.getParent(), cmd);

        try {
            Path tempDirectory = Files.createTempDirectory("jreleaser");
            Path binDirectory = tempDirectory.resolve("bin");
            Files.createDirectories(binDirectory);
            Files.copy(image, binDirectory.resolve(image.getFileName()));
            context.getLogger().debug("copying files to {}", context.relativizeToBasedir(tempDirectory));
            copyFiles(context, tempDirectory);

            Path imageZip = assembleDirectory.resolve(assembler.getName() + "-" + context.getModel().getProject().getResolvedVersion() + ".zip");
            FileUtils.zip(tempDirectory, imageZip);

            context.getLogger().debug("- {}", imageZip.getFileName());

            return Artifact.of(imageZip, assembler.getGraal().getPlatform());
        } catch (IOException e) {
            throw new AssemblerProcessingException("Unexpected error", e);
        }
    }

    private String readJavaVersion(Path path) throws AssemblerProcessingException {
        Path release = path.resolve("release");
        if (!Files.exists(release)) {
            throw new AssemblerProcessingException("Invalid GraalVM [" + path.toAbsolutePath() + "] release file not found");
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
                throw new AssemblerProcessingException("Invalid GraalVM release file [" + release.toAbsolutePath() + "]");
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException("Invalid GraalVM release file [" + release.toAbsolutePath() + "]", e);
        }
    }

    private String readGraalVersion(Path path) throws AssemblerProcessingException {
        Path release = path.resolve("release");
        if (!Files.exists(release)) {
            throw new AssemblerProcessingException("Invalid GraalVM [" + path.toAbsolutePath() + "] release file not found");
        }

        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(release));
            if (props.containsKey(KEY_GRAALVM_VERSION)) {
                String version = props.getProperty(KEY_GRAALVM_VERSION);
                if (version.startsWith("\"") && version.endsWith("\"")) {
                    return version.substring(1, version.length() - 1);
                }
                return version;
            } else {
                throw new AssemblerProcessingException("Invalid GraalVM release file [" + release.toAbsolutePath() + "]");
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException("Invalid GraalVM release file [" + release.toAbsolutePath() + "]", e);
        }
    }

    private Set<Path> copyJars(JReleaserContext context, Path destination) throws AssemblerProcessingException {
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

    @Override
    protected void writeFile(Project project, String content, Map<String, Object> props, String fileName)
        throws AssemblerProcessingException {
        // noop
    }
}
