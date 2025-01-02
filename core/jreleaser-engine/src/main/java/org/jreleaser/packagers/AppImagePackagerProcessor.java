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

import org.apache.commons.lang3.StringUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.engine.release.Releasers;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Screenshot;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.AppImagePackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_CATEGORIES;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_CATEGORIES_BY_COMMA;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_COMPONENT_ID;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_DEVELOPER_NAME;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_DISTRIBUTION_URL;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_ICONS;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_RELEASES;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_REPOSITORY_NAME;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_REPOSITORY_OWNER;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_REPO_NAME;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_REPO_OWNER;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_REQUIRES_TERMINAL;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_SCREENSHOTS;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_URLS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_URL;
import static org.jreleaser.model.Constants.KEY_PROJECT_AUTHORS;
import static org.jreleaser.packagers.AppdataUtils.isReleaseIncluded;
import static org.jreleaser.packagers.AppdataUtils.resolveIcons;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class AppImagePackagerProcessor extends AbstractRepositoryPackagerProcessor<AppImagePackager> {
    public AppImagePackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        setupPrepare(distribution, props);
        super.doPrepareDistribution(distribution, props);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, TemplateContext props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(props);
    }

    private void setupPrepare(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        try {
            props.set(KEY_APPIMAGE_RELEASES, Releasers.releaserFor(context)
                .listReleases(releaser.getOwner(), releaser.getName()).stream()
                .filter(r -> isReleaseIncluded(packager.getSkipReleases(), r.getVersion().toString()))
                .map(r -> AppdataUtils.Release.of(r.getUrl(), r.getVersion().toString(), r.getPublishedAt()))
                .collect(toList()));
        } catch (IOException e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }

        props.set(KEY_APPIMAGE_SCREENSHOTS, packager.getScreenshots().stream()
            .map(Screenshot::asScreenshotTemplate)
            .collect(toList()));

        context.getLogger().debug(RB.$("packager.fetch.icons"));
        props.set(KEY_APPIMAGE_ICONS, packager.getIcons());
        resolveIcons(context, packager, distribution, props, packager.getIcons());
    }

    @Override
    protected void fillPackagerProperties(TemplateContext props, Distribution distribution) {
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, distribution.getJava().getMainClass());
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_MODULE, distribution.getJava().getMainModule());
        props.set(KEY_PROJECT_AUTHORS, context.getModel().getProject().getAuthors());
        props.set(KEY_APPIMAGE_URLS, context.getModel().getProject().getLinks().asLinkTemplates(false));
        props.set(KEY_APPIMAGE_COMPONENT_ID, getPackager().getComponentId());
        props.set(KEY_APPIMAGE_CATEGORIES, getPackager().getCategories());
        props.set(KEY_APPIMAGE_CATEGORIES_BY_COMMA, String.join(",", getPackager().getCategories()));
        props.set(KEY_APPIMAGE_DEVELOPER_NAME, getPackager().getDeveloperName());
        props.set(KEY_APPIMAGE_REQUIRES_TERMINAL, getPackager().isRequiresTerminal());
        props.set(KEY_APPIMAGE_REPO_OWNER, packager.getRepository().getOwner());
        props.set(KEY_APPIMAGE_REPO_NAME, packager.getRepository().getName());
        props.set(KEY_APPIMAGE_REPOSITORY_OWNER, packager.getRepository().getOwner());
        props.set(KEY_APPIMAGE_REPOSITORY_NAME, packager.getRepository().getName());

        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();
        String str = props.get(KEY_DISTRIBUTION_ARTIFACT_FILE);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "${DISTRIBUTION_VERSION}");
        props.set(KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE, str);
        str = props.get(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "${DISTRIBUTION_VERSION}");
        props.set(KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE_NAME, str);
        str = props.get(KEY_DISTRIBUTION_URL);
        str = str.replace(releaser.getEffectiveTagName(context.getModel()), "${DISTRIBUTION_TAG}")
            .replace(props.get(KEY_DISTRIBUTION_ARTIFACT_FILE), "${DISTRIBUTION_FILE}");
        props.set(KEY_APPIMAGE_DISTRIBUTION_URL, str);
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             TemplateContext props,
                             Path outputDirectory,
                             String fileName) throws PackagerProcessingException {
        Releaser<?> gitService = context.getModel().getRelease().getReleaser();
        if (fileName.contains("github") && !(gitService instanceof GithubReleaser)) {
            // skip
            return;
        } else if (fileName.contains("-github")) {
            fileName = StringUtils.remove(fileName, "-github");
        }

        fileName = trimTplExtension(fileName);
        Optional<Stereotype> stereotype = resolveStereotype(fileName);
        if (stereotype.isPresent()) {
            if (distribution.getStereotype() == stereotype.get()) {
                fileName = fileName.substring(distribution.getStereotype().formatted().length() + 1);
            } else {
                // skip it
                return;
            }
        }

        Path outputFile = outputDirectory.resolve(fileName);

        if ("app.desktop".equals(fileName)) {
            outputFile = outputDirectory.resolve(distribution.getExecutable().getName() + ".desktop");
        } else if ("appdata.xml".equals(fileName)) {
            outputFile = outputDirectory.resolve(getPackager().getComponentId() + ".appdata.xml");
        }

        writeFile(content, outputFile);
    }

    @Override
    protected void writeFile(Distribution distribution, InputStream inputStream, TemplateContext props, Path outputDirectory, String fileName) throws PackagerProcessingException {
        Path outputFile = outputDirectory.resolve(fileName);

        if (fileName.endsWith("app.png")) {
            outputFile = outputDirectory.resolve(fileName.replace("app", distribution.getExecutable().getName()));
        }

        writeFile(inputStream, outputFile);
    }
}
