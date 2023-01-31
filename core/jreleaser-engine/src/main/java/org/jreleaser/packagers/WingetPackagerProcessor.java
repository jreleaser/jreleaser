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
package org.jreleaser.packagers;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.WingetPackager;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.PlatformUtils;

import java.io.Reader;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_PLATFORM;
import static org.jreleaser.model.Constants.KEY_WINGET_AUTHOR;
import static org.jreleaser.model.Constants.KEY_WINGET_DEFAULT_LOCALE;
import static org.jreleaser.model.Constants.KEY_WINGET_HAS_TAGS;
import static org.jreleaser.model.Constants.KEY_WINGET_INSTALLER_ARCHITECTURE;
import static org.jreleaser.model.Constants.KEY_WINGET_INSTALLER_TYPE;
import static org.jreleaser.model.Constants.KEY_WINGET_INSTALL_MODES;
import static org.jreleaser.model.Constants.KEY_WINGET_MANIFEST_TYPE;
import static org.jreleaser.model.Constants.KEY_WINGET_MINIMUM_OS_VERSION;
import static org.jreleaser.model.Constants.KEY_WINGET_MONIKER;
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
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;

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
        props.set(KEY_WINGET_INSTALLER_TYPE, packager.getInstaller().getType().toString());
        props.set(KEY_WINGET_SCOPE, packager.getInstaller().getScope().toString());
        props.set(KEY_WINGET_INSTALL_MODES, packager.getInstaller().getModes());
        props.set(KEY_WINGET_UPGRADE_BEHAVIOR, packager.getInstaller().getUpgradeBehavior().toString());
        props.set(KEY_WINGET_RELEASE_DATE, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        String platform = props.get(KEY_DISTRIBUTION_ARTIFACT_PLATFORM);
        if (PlatformUtils.isIntel32(platform)) {
            platform = "x86";
        } else if (PlatformUtils.isIntel64(platform)) {
            platform = "x64";
        } else if (PlatformUtils.isArm32(platform)) {
            platform = "arm";
        } else if (PlatformUtils.isArm64(platform)) {
            platform = "arm64";
        }
        props.set(KEY_WINGET_INSTALLER_ARCHITECTURE, platform);
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
            fileName = packageIdentifier + ".version.yaml";
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
        return outputDirectory.resolve(packageIdentifier.substring(0, 1).toLowerCase(Locale.ENGLISH) + "/" +
            packageIdentifier.replace(".", "/") + "/" +
            context.getModel().getProject().getResolvedVersion());
    }
}
