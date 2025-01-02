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
import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.NativeImageAssembler;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.mustache.TemplateContext;
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
import java.util.Properties;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static org.jreleaser.assemblers.AssemblerUtils.copyJars;
import static org.jreleaser.assemblers.AssemblerUtils.readJavaVersion;
import static org.jreleaser.model.Constants.KEY_ARCHIVE_FORMAT;
import static org.jreleaser.util.FileType.EXE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class NativeImageAssemblerProcessor extends AbstractAssemblerProcessor<org.jreleaser.model.api.assemble.NativeImageAssembler, NativeImageAssembler> {
    private static final String KEY_GRAALVM_VERSION = "GRAALVM_VERSION";

    public NativeImageAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doAssemble(TemplateContext props) throws AssemblerProcessingException {
        if (!assembler.getGraal().isActiveAndSelected()) return;

        // verify graal
        Path graalPath = assembler.getGraal().getEffectivePath(context, assembler);
        SemanticVersion javaVersion = SemanticVersion.of(readJavaVersion(graalPath));
        SemanticVersion graalVersion = SemanticVersion.of(readGraalVersion(graalPath));
        context.getLogger().debug(RB.$("assembler.graal.java"), javaVersion, graalPath.toAbsolutePath().toString());
        context.getLogger().debug(RB.$("assembler.graal.graal"), graalVersion, graalPath.toAbsolutePath().toString());

        String platform = assembler.getGraal().getPlatform();
        // copy jars to assembly
        Path assembleDirectory = props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path jarsDirectory = assembleDirectory.resolve(JARS_DIRECTORY);
        Path universalJarsDirectory = jarsDirectory.resolve(UNIVERSAL_DIRECTORY);
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

        nativeImage(props, assembleDirectory, graalPath, jars, imageName);
    }

    private void installNativeImage(Path graalPath) throws AssemblerProcessingException {
        Path nativeImageExecutable = graalPath
            .resolve("bin")
            .resolve(PlatformUtils.isWindows() ? "native-image.cmd" : "native-image")
            .toAbsolutePath();

        if (!Files.exists(nativeImageExecutable)) {
            Path guExecutable = graalPath
                .resolve(BIN_DIRECTORY)
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
            .resolve(BIN_DIRECTORY)
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

    private void nativeImage(TemplateContext props, Path assembleDirectory, Path graalPath, Set<Path> jars, String imageName) throws AssemblerProcessingException {
        String platform = assembler.getGraal().getPlatform();
        String platformReplaced = assembler.getPlatform().applyReplacements(platform);
        String finalImageName = imageName + "-" + platformReplaced;

        String executable = assembler.getExecutable();
        if (PlatformUtils.isWindows()) {
            executable += EXE.extension();
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

        if (isNotBlank(assembler.getJava().getMainModule())) {
            cmd.arg("--module")
                .arg(assembler.getJava().getMainModule() + "/" + assembler.getJava().getMainClass());

            cmd.arg("--module-path")
                .arg(jars.stream()
                    .map(Path::toAbsolutePath)
                    .map(Path::getParent)
                    .distinct()
                    .map(Path::toString)
                    .map(this::maybeQuote)
                    .collect(joining(File.pathSeparator)));

        } else {
            cmd.arg("-jar")
                .arg(maybeQuote(assembler.getMainJar().getEffectivePath(context, assembler).toAbsolutePath().toString()));

            if (!jars.isEmpty()) {
                cmd.arg("-cp")
                    .arg(jars.stream()
                        .map(Path::toAbsolutePath)
                        .map(image.getParent()::relativize)
                        .map(Path::toString)
                        .map(this::maybeQuote)
                        .collect(joining(File.pathSeparator)));
            }
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
            Path binDirectory = distDirectory.resolve(BIN_DIRECTORY);
            Files.createDirectories(binDirectory);
            Files.copy(image, binDirectory.resolve(image.getFileName()));
            FileUtils.copyFiles(context.getLogger(),
                context.getBasedir(),
                distDirectory, path -> path.getFileName().startsWith(LICENSE));
            copyTemplates(context, props, distDirectory);
            copyArtifacts(context, distDirectory, platform, true);
            copyFiles(context, distDirectory);
            copyFileSets(context, distDirectory);
            generateSwidTag(context, distDirectory);

            String str = assembler.getGraal().getExtraProperties()
                .getOrDefault(KEY_ARCHIVE_FORMAT, assembler.getArchiveFormat())
                .toString();
            Archive.Format archiveFormat = Archive.Format.of(str);

            Path imageArchive = assembleDirectory.resolve(finalImageName + "." + archiveFormat.extension());
            FileUtils.packArchive(tempDirectory, imageArchive, assembler.getOptions().toOptions());

            context.getLogger().debug("- {}", imageArchive.getFileName());
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
        context.getLogger().info("  upx {}", image.getFileName().toString());

        try {
            upx.invoke(image.getParent(), args);
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
}
