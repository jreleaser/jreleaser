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

import org.apache.commons.compress.utils.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Jpackage;
import org.jreleaser.model.Project;
import org.jreleaser.model.assembler.spi.AssemblerProcessingException;
import org.jreleaser.templates.TemplateResource;
import org.jreleaser.templates.TemplateUtils;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.SemVer;
import org.jreleaser.util.command.Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.assemblers.AssemblerUtils.copyJars;
import static org.jreleaser.assemblers.AssemblerUtils.readJavaVersion;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.FileUtils.listFilesAndProcess;
import static org.jreleaser.util.PlatformUtils.isWindows;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public class JpackageAssemblerProcessor extends AbstractJavaAssemblerProcessor<Jpackage> {
    public JpackageAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doAssemble(Map<String, Object> props) throws AssemblerProcessingException {
        Jpackage.PlatformPackager packager = assembler.getResolvedPlatformPackager();

        // verify jdk
        Path jdkPath = packager.getJdk().getEffectivePath(context, assembler);
        SemVer jdkVersion = SemVer.of(readJavaVersion(jdkPath));
        context.getLogger().debug(RB.$("assembler.jpackage.jdk"), jdkVersion, jdkPath.toAbsolutePath().toString());
        if (jdkVersion.getMajor() < 16) {
            throw new AssemblerProcessingException(RB.$("ERROR_jpackage_minimum_jdk_required", jdkVersion.toString()));
        }

        String platform = packager.getJdk().getPlatform();

        Path assembleDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path workDirectory = assembleDirectory.resolve("work-" + platform);
        Path inputsDirectory = workDirectory.resolve("inputs");
        Path filesDirectory = inputsDirectory.resolve("files");

        // copy files to inputs
        copyFiles(context, filesDirectory);
        copyFileSets(context, filesDirectory);

        // copy jars to inputs
        context.getLogger().debug(RB.$("assembler.copy.jars"), context.relativizeToBasedir(filesDirectory));
        copyJars(context, assembler, filesDirectory, "");
        copyJars(context, assembler, filesDirectory, platform);

        // copy icon to inputs
        copyIcon(context, assembler, packager, inputsDirectory, platform, props);

        // adjust runtime image
        if (isNotBlank(assembler.getJlink())) {
            adjustRuntimeImage(context, assembler, workDirectory, platform);
        }

        for (String type : packager.getTypes()) {
            context.getLogger().info("- " + RB.$("assembler.jpackage.type"), type);
            jpackage(context, type, workDirectory, props);
        }
    }

    private void copyIcon(JReleaserContext context, Jpackage assembler,
                          Jpackage.PlatformPackager packager, Path inputsDirectory,
                          String platform, Map<String, Object> props) throws AssemblerProcessingException {
        String p = "linux";
        String ext = ".png";

        if (isWindows(platform)) {
            p = "windows";
            ext = ".ico";
        } else if (PlatformUtils.isMac(platform)) {
            p = "osx";
            ext = ".icns";
        }

        String icon = resolveTemplate(packager.getIcon(), props);
        try {
            if (isNotBlank(icon) && Files.exists(context.getBasedir().resolve(icon)) && icon.endsWith(ext)) {
                Path iconPath = context.getBasedir().resolve(icon);
                Files.copy(iconPath, inputsDirectory.resolve(assembler.getName() + ext), REPLACE_EXISTING);
            } else {
                String iconResource = "META-INF/jreleaser/icons/" + p + "/duke" + ext;
                try (TemplateResource templateResource = TemplateUtils.resolveResource(context.getLogger(), iconResource)) {
                    writeFile(IOUtils.toByteArray(templateResource.getInputStream()), inputsDirectory.resolve(assembler.getName() + ext));
                }
            }
        } catch (Exception e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private void adjustRuntimeImage(JReleaserContext context, Jpackage assembler, Path workDirectory, String platform) throws AssemblerProcessingException {
        Optional<Artifact> runtimeImageByPlatform = assembler.findRuntimeImageByPlatform(platform);
        if (!runtimeImageByPlatform.isPresent()) {
            throw new AssemblerProcessingException(RB.$("ERROR_jpackage_runtime_image_not_found", platform));
        }

        Path originalImage = runtimeImageByPlatform.get().getEffectivePath(context, assembler);
        Path adjustedImage = workDirectory.resolve("runtime-image");

        try {
            if (!FileUtils.copyFilesRecursive(context.getLogger(), originalImage, adjustedImage, new Predicate<Path>() {
                @Override
                public boolean test(Path path) {
                    return (path.getFileName().toString().endsWith(".jar") && path.getParent().getFileName().toString().equals("jars")) ||
                        path.getFileName().toString().equals(context.getModel().getAssemble().findJlink(assembler.getJlink()).getExecutable());
                }
            })) {
                throw new IOException(RB.$("ERROR_assembler_adjusting_image", adjustedImage));
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }

        runtimeImageByPlatform.get().setPath(adjustedImage.toAbsolutePath().toString());
    }

    private void jpackage(JReleaserContext context, String type, Path workDirectory, Map<String, Object> props) throws AssemblerProcessingException {
        Jpackage.PlatformPackager packager = assembler.getResolvedPlatformPackager();
        Path jdkPath = packager.getJdk().getEffectivePath(context, assembler);
        String platform = packager.getJdk().getPlatform();
        String platformReplaced = assembler.getPlatform().applyReplacements(platform);

        Path assembleDirectory = workDirectory.getParent();
        Path packagerDirectory = workDirectory.resolve(type).toAbsolutePath();
        try {
            FileUtils.deleteFiles(packagerDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_delete_dir",
                context.relativizeToBasedir(packagerDirectory)), e);
        }

        Path inputsDirectory = workDirectory.resolve("inputs");

        Optional<Artifact> runtimeImageByPlatform = assembler.findRuntimeImageByPlatform(platform);
        if (!runtimeImageByPlatform.isPresent()) {
            throw new AssemblerProcessingException(RB.$("ERROR_jpackage_runtime_image_not_found", platform));
        }

        String moduleName = assembler.getJava().getMainModule();
        String appName = packager.getResolvedAppName(context, assembler);
        String appVersion = assembler.getApplicationPackage().getResolvedAppVersion(context, assembler);
        String vendor = assembler.getApplicationPackage().getVendor();
        String copyright = assembler.getApplicationPackage().getCopyright();

        Path jpackageExecutable = jdkPath
            .resolve("bin")
            .resolve(isWindows() ? "jpackage.exe" : "jpackage")
            .toAbsolutePath();

        Command cmd = new Command(jpackageExecutable.toAbsolutePath().toString(), true)
            .arg("--type")
            .arg(type)
            .arg("--dest")
            .arg(assembleDirectory.toAbsolutePath().toString())
            .arg("--input")
            .arg(inputsDirectory.resolve("files").toAbsolutePath().toString())
            .arg("--name")
            .arg(maybeQuote(appName))
            .arg("--runtime-image")
            .arg(maybeQuote(runtimeImageByPlatform.get().getEffectivePath(context, assembler).toAbsolutePath().toString()))
            .arg("--app-version")
            .arg(appVersion)
            .arg("--vendor")
            .arg(maybeQuote(vendor))
            .arg("--copyright")
            .arg(maybeQuote(copyright))
            .arg("--description")
            .arg(maybeQuote(context.getModel().getProject().getDescription()));

        if (assembler.isVerbose()) {
            cmd.arg("--verbose");
        }

        if (isNotBlank(moduleName)) {
            cmd.arg("--module")
                .arg(moduleName + "/" + assembler.getJava().getMainClass());
        } else {
            cmd.arg("--main-class")
                .arg(assembler.getJava().getMainClass())
                .arg("--main-jar")
                .arg(maybeQuote(assembler.getMainJar().getResolvedPath().getFileName().toString()));
        }

        // Launcher
        for (String argument : assembler.getLauncher().getArguments()) {
            cmd.arg("--arguments")
                .arg(maybeQuote(argument));
        }
        for (String javaOption : assembler.getLauncher().getJavaOptions()) {
            cmd.arg("--java-options")
                .arg(maybeQuote(javaOption));
        }

        // ApplicationPackage
        String licenseFile = resolveTemplate(assembler.getApplicationPackage().getLicenseFile(), props);
        if (isNotBlank(licenseFile)) {
            Path licenseFilePath = context.getBasedir().resolve(licenseFile);
            if (Files.exists(licenseFilePath)) {
                cmd.arg("--license-file")
                    .arg(maybeQuote(licenseFilePath.toAbsolutePath().toString()));
            }
        }

        if (!assembler.getApplicationPackage().getFileAssociations().isEmpty()) {
            for (String filename : assembler.getApplicationPackage().getFileAssociations()) {
                Path path = context.getBasedir().resolve(resolveTemplate(filename, props));
                if (Files.exists(path)) {
                    cmd.arg("--file-associations")
                        .arg(maybeQuote(path.toAbsolutePath().toString()));
                }
            }
        }

        customize(type, packager, inputsDirectory, cmd, props);

        context.getLogger().debug(String.join(" ", cmd.getArgs()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        executeCommandCapturing(cmd, out);
        if (assembler.isVerbose()) {
            context.getLogger().debug(out.toString());
        }

        // replace only if not linux
        if (!PlatformUtils.isLinux(platform) && assembler.isAttachPlatform()) {
            try {
                Optional<Path> artifact = listFilesAndProcess(assembleDirectory, files ->
                    files.filter(path -> path.getFileName().toString().endsWith(type))
                        .findFirst());

                if (artifact.isPresent()) {
                    String dest = artifact.get().getFileName().toString()
                        .replace("." + type, "-" + platformReplaced + "." + type);
                    Files.move(
                        assembleDirectory.resolve(artifact.get().getFileName()),
                        assembleDirectory.resolve(dest), REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
            }
        }
    }

    private void customize(String type, Jpackage.PlatformPackager packager, Path inputsDirectory, Command cmd, Map<String, Object> props) {
        String installDir = resolveTemplate(packager.getInstallDir(), props);
        if (isNotBlank(installDir)) {
            cmd.arg("--install-dir")
                .arg(maybeQuote(installDir));
        }

        String resourceDir = resolveTemplate(packager.getResourceDir(), props);
        if (isNotBlank(resourceDir)) {
            Path resourceDirPath = context.getBasedir().resolve(resourceDir);
            if (Files.exists(resourceDirPath)) {
                cmd.arg("--resource-dir")
                    .arg(maybeQuote(resourceDirPath.toAbsolutePath().toString()));
            }
        }

        if (packager instanceof Jpackage.Osx) {
            customizeOsx(type, (Jpackage.Osx) packager, inputsDirectory, cmd, props);
        } else if (packager instanceof Jpackage.Linux) {
            customizeLinux(type, (Jpackage.Linux) packager, inputsDirectory, cmd, props);
        } else if (packager instanceof Jpackage.Windows) {
            customizeWindows(type, (Jpackage.Windows) packager, inputsDirectory, cmd, props);
        }
    }

    private void customizeOsx(String type, Jpackage.Osx packager, Path inputsDirectory, Command cmd, Map<String, Object> props) {
        if (isNotBlank(packager.getPackageName())) {
            cmd.arg("--mac-package-name")
                .arg(packager.getPackageName());
        }
        if (isNotBlank(packager.getPackageSigningPrefix())) {
            cmd.arg("--mac-package-signing-prefix")
                .arg(packager.getPackageSigningPrefix());
        }
        if (packager.isSign()) {
            cmd.arg("--mac-sign");
        }

        String signingKeychain = resolveTemplate(packager.getSigningKeychain(), props);
        if (isNotBlank(signingKeychain)) {
            Path path = context.getBasedir().resolve(resolveTemplate(signingKeychain, props));
            if (Files.exists(path)) {
                cmd.arg("--mac-signing-keychain")
                    .arg(path.toAbsolutePath().toString());
            }
        }
        if (isNotBlank(packager.getSigningKeyUsername())) {
            cmd.arg("--mac-signing-key-user-name")
                .arg(packager.getSigningKeyUsername());
        }

        cmd.arg("--icon")
            .arg(inputsDirectory.resolve(assembler.getName() + ".icns").toAbsolutePath().toString());
    }

    private void customizeLinux(String type, Jpackage.Linux packager, Path inputsDirectory, Command cmd, Map<String, Object> props) {
        if (isNotBlank(packager.getPackageName())) {
            cmd.arg("--linux-package-name")
                .arg(packager.getPackageName());
        }
        if (isNotBlank(packager.getMenuGroup())) {
            cmd.arg("--linux-menu-group")
                .arg(packager.getMenuGroup());
        }
        if (isNotBlank(packager.getAppRelease())) {
            cmd.arg("--linux-app-release")
                .arg(packager.getAppRelease());
        }
        if (isNotBlank(packager.getAppCategory())) {
            cmd.arg("--linux-app-category")
                .arg(packager.getAppCategory());
        }
        if (packager.isShortcut()) {
            cmd.arg("--linux-shortcut");
        }
        if (!packager.getPackageDeps().isEmpty()) {
            cmd.arg("--linux-package-deps")
                .arg(String.join(",", packager.getPackageDeps()));
        }

        if ("deb".equals(type)) {
            if (isNotBlank(packager.getMaintainer())) {
                cmd.arg("--linux-deb-maintainer")
                    .arg(packager.getMaintainer());
            }
        } else if ("rpm".equals(type)) {
            if (isNotBlank(packager.getLicense())) {
                cmd.arg("--linux-rpm-license-type")
                    .arg(packager.getLicense());
            }
        }

        cmd.arg("--icon")
            .arg(inputsDirectory.resolve(assembler.getName() + ".png").toAbsolutePath().toString());
    }

    private void customizeWindows(String type, Jpackage.Windows packager, Path inputsDirectory, Command cmd, Map<String, Object> props) {
        if (packager.isConsole()) {
            cmd.arg("--win-console");
        }
        if (packager.isDirChooser()) {
            cmd.arg("--win-dir-chooser");
        }
        if (packager.isMenu()) {
            cmd.arg("--win-menu");
        }
        if (packager.isPerUserInstall()) {
            cmd.arg("--win-per-user-install");
        }
        if (packager.isShortcut()) {
            cmd.arg("--win-shortcut");
        }
        if (isNotBlank(packager.getMenuGroup())) {
            cmd.arg("--win-menu-group")
                .arg(maybeQuote(packager.getMenuGroup()));
        }
        if (isNotBlank(packager.getUpgradeUuid())) {
            cmd.arg("--win-upgrade-uuid")
                .arg(packager.getUpgradeUuid());
        }

        cmd.arg("--icon")
            .arg(maybeQuote(inputsDirectory.resolve(assembler.getName() + ".ico").toAbsolutePath().toString()));
    }

    @Override
    protected void writeFile(Project project, String content, Map<String, Object> props, String fileName)
        throws AssemblerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path outputFile = outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
