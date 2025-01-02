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

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.WingetPackager;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.io.Reader;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_PLATFORM;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_PROJECT_LONG_DESCRIPTION;
import static org.jreleaser.model.Constants.KEY_WINGET_AUTHOR;
import static org.jreleaser.model.Constants.KEY_WINGET_DEFAULT_LOCALE;
import static org.jreleaser.model.Constants.KEY_WINGET_EXTERNAL_DEPENDENCIES;
import static org.jreleaser.model.Constants.KEY_WINGET_HAS_DEPENDENCIES;
import static org.jreleaser.model.Constants.KEY_WINGET_HAS_EXTERNAL_DEPENDENCIES;
import static org.jreleaser.model.Constants.KEY_WINGET_HAS_PACKAGE_DEPENDENCIES;
import static org.jreleaser.model.Constants.KEY_WINGET_HAS_TAGS;
import static org.jreleaser.model.Constants.KEY_WINGET_HAS_WINDOWS_FEATURES;
import static org.jreleaser.model.Constants.KEY_WINGET_HAS_WINDOWS_LIBRARIES;
import static org.jreleaser.model.Constants.KEY_WINGET_INSTALLERS;
import static org.jreleaser.model.Constants.KEY_WINGET_INSTALLER_ARCHITECTURE;
import static org.jreleaser.model.Constants.KEY_WINGET_INSTALLER_TYPE;
import static org.jreleaser.model.Constants.KEY_WINGET_INSTALL_MODES;
import static org.jreleaser.model.Constants.KEY_WINGET_MANIFEST_TYPE;
import static org.jreleaser.model.Constants.KEY_WINGET_MINIMUM_OS_VERSION;
import static org.jreleaser.model.Constants.KEY_WINGET_MONIKER;
import static org.jreleaser.model.Constants.KEY_WINGET_PACKAGE_DEPENDENCIES;
import static org.jreleaser.model.Constants.KEY_WINGET_PACKAGE_IDENTIFIER;
import static org.jreleaser.model.Constants.KEY_WINGET_PACKAGE_LOCALE;
import static org.jreleaser.model.Constants.KEY_WINGET_PACKAGE_NAME;
import static org.jreleaser.model.Constants.KEY_WINGET_PACKAGE_URL;
import static org.jreleaser.model.Constants.KEY_WINGET_PACKAGE_VERSION;
import static org.jreleaser.model.Constants.KEY_WINGET_PRODUCT_CODE;
import static org.jreleaser.model.Constants.KEY_WINGET_PUBLISHER_NAME;
import static org.jreleaser.model.Constants.KEY_WINGET_PUBLISHER_SUPPORT_URL;
import static org.jreleaser.model.Constants.KEY_WINGET_PUBLISHER_URL;
import static org.jreleaser.model.Constants.KEY_WINGET_RELEASE_DATE;
import static org.jreleaser.model.Constants.KEY_WINGET_SCOPE;
import static org.jreleaser.model.Constants.KEY_WINGET_TAGS;
import static org.jreleaser.model.Constants.KEY_WINGET_UPGRADE_BEHAVIOR;
import static org.jreleaser.model.Constants.KEY_WINGET_WINDOWS_FEATURES;
import static org.jreleaser.model.Constants.KEY_WINGET_WINDOWS_LIBRARIES;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public class WingetPackagerProcessor extends AbstractRepositoryPackagerProcessor<WingetPackager> {
    public WingetPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, TemplateContext props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(props);
    }

    @Override
    protected void fillPackagerProperties(TemplateContext props, Distribution distribution) {
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, distribution.getJava().getMainClass());
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_MODULE, distribution.getJava().getMainModule());
        String desc = context.getModel().getProject().getLongDescription();
        desc = Arrays.stream(desc.split(System.lineSeparator()))
            .map(line -> "  " + line)
            .collect(Collectors.joining(System.lineSeparator()));
        props.set(KEY_PROJECT_LONG_DESCRIPTION,
            MustacheUtils.passThrough("|" + System.lineSeparator() + desc));

        props.set(KEY_WINGET_DEFAULT_LOCALE, packager.getDefaultLocale());
        props.set(KEY_WINGET_AUTHOR, packager.getAuthor());
        props.set(KEY_WINGET_MONIKER, resolveTemplate(packager.getMoniker(), props));
        props.set(KEY_WINGET_MINIMUM_OS_VERSION, packager.getMinimumOsVersion());
        props.set(KEY_WINGET_PRODUCT_CODE, resolveTemplate(packager.getProductCode(), props));
        props.set(KEY_WINGET_HAS_TAGS, !packager.getTags().isEmpty());
        props.set(KEY_WINGET_TAGS, packager.getTags());
        props.set(KEY_WINGET_PACKAGE_IDENTIFIER, resolveTemplate(packager.getPackage().getIdentifier(), props));
        props.set(KEY_WINGET_PACKAGE_NAME, resolveTemplate(packager.getPackage().getName(), props));
        props.set(KEY_WINGET_PACKAGE_VERSION, resolveTemplate(packager.getPackage().getVersion(), props));
        props.set(KEY_WINGET_PACKAGE_URL, resolveTemplate(packager.getPackage().getUrl(), props));
        props.set(KEY_WINGET_PUBLISHER_NAME, resolveTemplate(packager.getPublisher().getName(), props));
        props.set(KEY_WINGET_PUBLISHER_URL, resolveTemplate(packager.getPublisher().getUrl(), props));
        props.set(KEY_WINGET_PUBLISHER_SUPPORT_URL, resolveTemplate(packager.getPublisher().getSupportUrl(), props));
        props.set(KEY_WINGET_INSTALLER_TYPE, packager.getInstaller().getType().formatted());
        if (null != packager.getInstaller().getScope()) {
            props.set(KEY_WINGET_SCOPE, packager.getInstaller().getScope().formatted());
        }
        props.set(KEY_WINGET_INSTALL_MODES, packager.getInstaller().getModes().stream()
            .map(org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode::formatted)
            .collect(toList()));
        if (null != packager.getInstaller().getUpgradeBehavior()) {
            props.set(KEY_WINGET_UPGRADE_BEHAVIOR, packager.getInstaller().getUpgradeBehavior().formatted());
        }
        props.set(KEY_WINGET_RELEASE_DATE, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        props.set(KEY_WINGET_INSTALLER_ARCHITECTURE, resolveArchitecture(props.get(KEY_DISTRIBUTION_ARTIFACT_PLATFORM)));

        props.set(KEY_WINGET_HAS_DEPENDENCIES, packager.getInstaller().getDependencies().hasDependencies());
        props.set(KEY_WINGET_HAS_WINDOWS_FEATURES, packager.getInstaller().getDependencies().hasWindowsFeatures());
        props.set(KEY_WINGET_HAS_WINDOWS_LIBRARIES, packager.getInstaller().getDependencies().hasWindowsLibraries());
        props.set(KEY_WINGET_HAS_EXTERNAL_DEPENDENCIES, packager.getInstaller().getDependencies().hasExternalDependencies());
        props.set(KEY_WINGET_HAS_PACKAGE_DEPENDENCIES, packager.getInstaller().getDependencies().hasPackageDependencies());
        props.set(KEY_WINGET_WINDOWS_FEATURES, packager.getInstaller().getDependencies().getWindowsFeatures());
        props.set(KEY_WINGET_WINDOWS_LIBRARIES, packager.getInstaller().getDependencies().getWindowsLibraries());
        props.set(KEY_WINGET_EXTERNAL_DEPENDENCIES, packager.getInstaller().getDependencies().getExternalDependencies());
        props.set(KEY_WINGET_PACKAGE_DEPENDENCIES, packager.getInstaller().getDependencies().getPackageDependencies());

        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JLINK) {
            List<Installer> installers = new ArrayList<>();
            for (Artifact artifact : collectArtifacts(distribution)) {
                if (!artifact.getPath().endsWith(ZIP.extension()) || isBlank(artifact.getPlatform())) {
                    continue;
                }

                String artifactFile = artifact.getEffectivePath().getFileName().toString();
                String architecture = resolveArchitecture(artifact.getPlatform());
                String url = resolveArtifactUrl(distribution, artifact);
                String fileName = getFilename(artifactFile, FileType.getSupportedExtensions());
                String checksum = artifact.getHash(Algorithm.SHA_256);
                String executableName = distribution.getExecutable().getName();
                String executablePath = fileName + "\\bin\\" + executableName + distribution.getExecutable().resolveWindowsExtension();

                installers.add(new Installer()
                    .withArchitecture(architecture)
                    .withUrl(url)
                    .withChecksum(checksum)
                    .withExecutablePath(executablePath)
                    .withExecutableName(executableName));
            }
            props.set(KEY_WINGET_INSTALLERS, installers);
        }
    }

    @Override
    protected String applyTemplate(String fileName, Reader reader, TemplateContext props) {
        fileName = trimTplExtension(fileName);
        if ("locale.yaml".equals(fileName)) {
            props.set(KEY_WINGET_MANIFEST_TYPE, "defaultLocale");
            props.set(KEY_WINGET_PACKAGE_LOCALE, packager.getDefaultLocale());
        } else if (fileName.startsWith("locale.")) {
            String locale = fileName.substring(7);
            locale = locale.substring(0, locale.length() - 5);
            props.set(KEY_WINGET_MANIFEST_TYPE, "locale");
            props.set(KEY_WINGET_PACKAGE_LOCALE, locale);
        }
        return super.applyTemplate(fileName, reader, props);
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             TemplateContext props,
                             Path outputDirectory,
                             String fileName) throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        String packageIdentifier = getPackager().getPackage().getIdentifier();

        if ("version.yaml".equals(fileName)) {
            fileName = packageIdentifier + ".yaml";
            outputDirectory = resolvePackageDirectory(outputDirectory, packageIdentifier);
        } else if ("installer.yaml".equals(fileName)) {
            fileName = packageIdentifier + ".installer.yaml";
            outputDirectory = resolvePackageDirectory(outputDirectory, packageIdentifier);
        } else if ("locale.yaml".equals(fileName)) {
            fileName = packageIdentifier + ".locale." + getPackager().getDefaultLocale() + ".yaml";
            outputDirectory = resolvePackageDirectory(outputDirectory, packageIdentifier);
        } else if (fileName.startsWith("locale") && fileName.endsWith(".yaml")) {
            fileName = packageIdentifier + "." + fileName;
            outputDirectory = resolvePackageDirectory(outputDirectory, packageIdentifier);
        }

        Path outputFile = outputDirectory.resolve(fileName);
        writeFile(content, outputFile);
    }

    private Path resolvePackageDirectory(Path outputDirectory, String packageIdentifier) {
        return outputDirectory.resolve("manifests/" + packageIdentifier.substring(0, 1).toLowerCase(Locale.ENGLISH) + "/" +
            packageIdentifier.replace(".", "/") + "/" +
            context.getModel().getProject().getResolvedVersion());
    }

    private String resolveArchitecture(String platform) {
        if (PlatformUtils.isIntel32(platform)) {
            return "x86";
        } else if (PlatformUtils.isIntel64(platform)) {
            return "x64";
        } else if (PlatformUtils.isArm32(platform)) {
            return "arm";
        } else if (PlatformUtils.isArm64(platform)) {
            return "arm64";
        }
        return "neutral";
    }

    private String resolveArtifactUrl(Distribution distribution, Artifact artifact) {
        return Artifacts.resolveDownloadUrl(context, org.jreleaser.model.api.packagers.WingetPackager.TYPE, distribution, artifact);
    }

    public static class Installer {
        private String architecture;
        private String url;
        private String checksum;
        private String executablePath;
        private String executableName;

        public String getArchitecture() {
            return architecture;
        }

        public Installer withArchitecture(String architecture) {
            this.architecture = architecture;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Installer withUrl(String url) {
            this.url = url;
            return this;
        }

        public String getChecksum() {
            return checksum;
        }

        public Installer withChecksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public String getExecutablePath() {
            return executablePath;
        }

        public Installer withExecutablePath(String executablePath) {
            this.executablePath = executablePath;
            return this;
        }

        public String getExecutableName() {
            return executableName;
        }

        public Installer withExecutableName(String executableName) {
            this.executableName = executableName;
            return this;
        }
    }
}
