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
package org.jreleaser.assemblers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.NativeImageAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.tool.ToolException;
import org.jreleaser.sdk.tool.Upx;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.version.SemanticVersion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jreleaser.assemblers.AssemblerUtils.copyJars;
import static org.jreleaser.assemblers.AssemblerUtils.readJavaVersion;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class NativeImageAssemblerProcessor extends AbstractJavaAssemblerProcessor<org.jreleaser.model.api.assemble.NativeImageAssembler, NativeImageAssembler> {
    private static final String KEY_GRAALVM_VERSION = "GRAALVM_VERSION";

    public NativeImageAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doAssemble(Map<String, Object> props) throws AssemblerProcessingException {
        // verify graal
        Path graalPath = assembler.getGraal().getEffectivePath(context, assembler);
        SemanticVersion javaVersion = SemanticVersion.of(readJavaVersion(graalPath));
        SemanticVersion graalVersion = SemanticVersion.of(readGraalVersion(graalPath));
        context.getLogger().debug(RB.$("assembler.graal.java"), javaVersion, graalPath.toAbsolutePath().toString());
        context.getLogger().debug(RB.$("assembler.graal.graal"), graalVersion, graalPath.toAbsolutePath().toString());

        String platform = assembler.getGraal().getPlatform();
        // copy jars to assembly
        Path assembleDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path jarsDirectory = assembleDirectory.resolve("jars");
        Path universalJarsDirectory = jarsDirectory.resolve("universal");
        context.getLogger().debug(RB.$("assembler.copy.jars"), context.relativizeToBasedir(universalJarsDirectory));
        Set<Path> jars = copyJars(context, assembler, universalJarsDirectory, "");
        Path platformJarsDirectory = jarsDirectory.resolve(platform);
        context.getLogger().debug(RB.$("assembler.copy.jars"), context.relativizeToBasedir(platformJarsDirectory));
        jars.addAll(copyJars(context, assembler, platformJarsDirectory, platform));

        // install native-image
        installNativeImage(graalPath);
        installComponents(graalPath);

        // run native-image
        String imageName = assembler.getResolvedImageName(context);
        if (isNotBlank(assembler.getImageNameTransform())) {
            imageName = assembler.getResolvedImageNameTransform(context);
        }

        nativeImage(assembleDirectory, graalPath, jars, imageName);
    }

    private void installNativeImage(Path graalPath) throws AssemblerProcessingException {
        Path nativeImageExecutable = graalPath
            .resolve("bin")
            .resolve(PlatformUtils.isWindows() ? "native-image.cmd" : "native-image")
            .toAbsolutePath();

        if (!Files.exists(nativeImageExecutable)) {
            Path guExecutable = graalPath
                .resolve("bin")
                .resolve(PlatformUtils.isWindows() ? "gu.cmd" : "gu")
                .toAbsolutePath();

            context.getLogger().debug(RB.$("assembler.graal.install.native.exec"));
            Command cmd = new Command(guExecutable.toString())
                .arg("install")
                .arg("-n")
                .arg("native-image");
            context.getLogger().debug(String.join(" ", cmd.getArgs()));
            executeCommand(cmd);
        }
    }

    private void installComponents(Path graalPath) throws AssemblerProcessingException {
        Path guExecutable = graalPath
            .resolve("bin")
            .resolve(PlatformUtils.isWindows() ? "gu.cmd" : "gu")
            .toAbsolutePath();

        for (String component : assembler.getComponents()) {
            context.getLogger().debug(RB.$("assembler.graal.install.component", component));
            Command cmd = new Command(guExecutable.toString())
                .arg("install")
                .arg("-n")
                .arg(component);
            context.getLogger().debug(String.join(" ", cmd.getArgs()));
            executeCommand(cmd);
        }
    }

    private Artifact nativeImage(Path assembleDirectory, Path graalPath, Set<Path> jars, String imageName) throws AssemblerProcessingException {
        String platform = assembler.getGraal().getPlatform();
        String platformReplaced = assembler.getPlatform().applyReplacements(platform);
        String finalImageName = imageName + "-" + platformReplaced;

        String executable = assembler.getExecutable();
        if (PlatformUtils.isWindows()) {
            executable += ".exe";
        }
        context.getLogger().info("- {}", finalImageName);

        Path image = assembleDirectory.resolve(executable).toAbsolutePath();
        try {
            if (Files.exists(image)) {
                Files.deleteIfExists(image);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_delete_image", executable), e);
        }

        assembler.getArgs().stream()
            .filter(arg -> arg.startsWith("-H:Name"))
            .findFirst()
            .ifPresent(assembler.getArgs()::remove);

        Path nativeImageExecutable = graalPath
            .resolve("bin")
            .resolve(PlatformUtils.isWindows() ? "native-image.cmd" : "native-image")
            .toAbsolutePath();

        Command cmd = new Command(nativeImageExecutable.toString(), true)
            .args(assembler.getArgs());

        NativeImageAssembler.PlatformCustomizer customizer = assembler.getResolvedPlatformCustomizer();
        cmd.args(customizer.getArgs());

        cmd.arg("-jar")
            .arg(maybeQuote(assembler.getMainJar().getEffectivePath(context, assembler).toAbsolutePath().toString()));

        if (!jars.isEmpty()) {
            cmd.arg("-cp")
                .arg(jars.stream()
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .map(this::maybeQuote)
                    .collect(Collectors.joining(File.pathSeparator)));
        }
        cmd.arg("-H:Name=" + assembler.getExecutable());
        context.getLogger().debug(String.join(" ", cmd.getArgs()));
        executeCommand(image.getParent(), cmd);

        if (assembler.getUpx().isEnabled()) {
            upx(image);
        }

        try {
            Path tempDirectory = Files.createTempDirectory("jreleaser");
            Path distDirectory = tempDirectory.resolve(finalImageName);
            Files.createDirectories(distDirectory);
            Path binDirectory = distDirectory.resolve("bin");
            Files.createDirectories(binDirectory);
            Files.copy(image, binDirectory.resolve(image.getFileName()));
            FileUtils.copyFiles(context.getLogger(),
                context.getBasedir(),
                distDirectory, path -> path.getFileName().startsWith("LICENSE"));
            copyFiles(context, distDirectory);
            copyFileSets(context, distDirectory);

            Path imageArchive = assembleDirectory.resolve(finalImageName + "." + assembler.getArchiveFormat().extension());
            FileUtils.packArchive(tempDirectory, imageArchive, context.getModel().resolveArchiveTimestamp());

            context.getLogger().debug("- {}", imageArchive.getFileName());

            return Artifact.of(imageArchive, platform);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private void upx(Path image) throws AssemblerProcessingException {
        Upx upx = new Upx(context.asImmutable(), assembler.getUpx().getVersion());
        try {
            if (!upx.setup()) {
                context.getLogger().warn(RB.$("tool_unavailable", "upx"));
                return;
            }
        } catch (ToolException e) {
            throw new AssemblerProcessingException(e.getMessage(), e);
        }

        List<String> args = new ArrayList<>(assembler.getUpx().getArgs());
        args.add(image.getFileName().toString());
        context.getLogger().info("upx {}", image.getFileName().toString());

        try {
            upx.compress(image.getParent(), args);
        } catch (CommandException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private String readGraalVersion(Path path) throws AssemblerProcessingException {
        Path release = path.resolve("release");
        if (!Files.exists(release)) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_invalid_graal_release", path.toAbsolutePath()));
        }

        try (InputStream in = Files.newInputStream(release)) {
            Properties props = new Properties();
            props.load(in);
            if (props.containsKey(KEY_GRAALVM_VERSION)) {
                String version = props.getProperty(KEY_GRAALVM_VERSION);
                if (version.startsWith("\"") && version.endsWith("\"")) {
                    return version.substring(1, version.length() - 1);
                }
                return version;
            } else {
                throw new AssemblerProcessingException(RB.$("ERROR_assembler_invalid_graal_release_file", release.toAbsolutePath()));
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_invalid_graal_release_file", release.toAbsolutePath()), e);
        }
    }

    @Override
    protected void writeFile(Project project, String content, Map<String, Object> props, String fileName)
        throws AssemblerProcessingException {
        // noop
    }
}
