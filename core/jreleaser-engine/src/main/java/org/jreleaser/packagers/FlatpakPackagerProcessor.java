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
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Screenshot;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.FlatpakPackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_FLATPACK_ICONS;
import static org.jreleaser.model.Constants.KEY_FLATPAK_BINARIES;
import static org.jreleaser.model.Constants.KEY_FLATPAK_CATEGORIES;
import static org.jreleaser.model.Constants.KEY_FLATPAK_CATEGORIES_BY_COMMA;
import static org.jreleaser.model.Constants.KEY_FLATPAK_CATEGORIES_BY_SEMICOLON;
import static org.jreleaser.model.Constants.KEY_FLATPAK_COMPONENT_ID;
import static org.jreleaser.model.Constants.KEY_FLATPAK_DEVELOPER_NAME;
import static org.jreleaser.model.Constants.KEY_FLATPAK_DIRECTORIES;
import static org.jreleaser.model.Constants.KEY_FLATPAK_FILES;
import static org.jreleaser.model.Constants.KEY_FLATPAK_HAS_SDK_EXTENSIONS;
import static org.jreleaser.model.Constants.KEY_FLATPAK_HAS_SDK_FINISH_ARGS;
import static org.jreleaser.model.Constants.KEY_FLATPAK_INCLUDE_OPENJDK;
import static org.jreleaser.model.Constants.KEY_FLATPAK_RELEASES;
import static org.jreleaser.model.Constants.KEY_FLATPAK_REPOSITORY_NAME;
import static org.jreleaser.model.Constants.KEY_FLATPAK_REPOSITORY_OWNER;
import static org.jreleaser.model.Constants.KEY_FLATPAK_REPO_NAME;
import static org.jreleaser.model.Constants.KEY_FLATPAK_REPO_OWNER;
import static org.jreleaser.model.Constants.KEY_FLATPAK_RUNTIME;
import static org.jreleaser.model.Constants.KEY_FLATPAK_RUNTIME_VERSION;
import static org.jreleaser.model.Constants.KEY_FLATPAK_SCREENSHOTS;
import static org.jreleaser.model.Constants.KEY_FLATPAK_SDK;
import static org.jreleaser.model.Constants.KEY_FLATPAK_SDK_EXTENSIONS;
import static org.jreleaser.model.Constants.KEY_FLATPAK_SDK_FINISH_ARGS;
import static org.jreleaser.model.Constants.KEY_FLATPAK_URLS;
import static org.jreleaser.model.Constants.KEY_PROJECT_AUTHORS;
import static org.jreleaser.model.Constants.KEY_SPEC_BINARIES;
import static org.jreleaser.model.Constants.KEY_SPEC_DIRECTORIES;
import static org.jreleaser.model.Constants.KEY_SPEC_FILES;
import static org.jreleaser.model.Constants.SKIP_OPENJDK;
import static org.jreleaser.packagers.AppdataUtils.isReleaseIncluded;
import static org.jreleaser.packagers.AppdataUtils.resolveIcons;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class FlatpakPackagerProcessor extends AbstractRepositoryPackagerProcessor<FlatpakPackager> {
    public FlatpakPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        setupPrepare(distribution, props);
        super.doPrepareDistribution(distribution, props);
    }

    private void setupPrepare(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        Artifact artifact = props.get(KEY_DISTRIBUTION_ARTIFACT);
        Path artifactPath = artifact.getResolvedPath(context, distribution);

        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY) {
            props.set(KEY_SPEC_DIRECTORIES, emptyList());
            props.set(KEY_SPEC_BINARIES, singletonList(distribution.getExecutable().resolveExecutable("linux")));
            props.set(KEY_SPEC_FILES, emptyList());
        } else {
            try {
                FileUtils.CategorizedArchive categorizedArchive = FileUtils.categorizeUnixArchive(
                    distribution.getExecutable().resolveWindowsExtension(),
                    artifactPath);

                props.set(KEY_FLATPAK_DIRECTORIES, categorizedArchive.getDirectories());
                props.set(KEY_FLATPAK_BINARIES, categorizedArchive.getBinaries());
                props.set(KEY_FLATPAK_FILES, categorizedArchive.getFiles());
            } catch (IOException e) {
                throw new PackagerProcessingException("ERROR", e);
            }
        }

        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        try {
            props.set(KEY_FLATPAK_RELEASES, Releasers.releaserFor(context)
                .listReleases(releaser.getOwner(), releaser.getName()).stream()
                .filter(r -> isReleaseIncluded(packager.getSkipReleases(), r.getVersion().toString()))
                .map(r -> AppdataUtils.Release.of(r.getUrl(), r.getVersion().toString(), r.getPublishedAt()))
                .collect(toList()));
        } catch (IOException e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }

        props.set(KEY_FLATPAK_SCREENSHOTS, packager.getScreenshots().stream()
            .map(Screenshot::asScreenshotTemplate)
            .collect(toList()));

        context.getLogger().debug(RB.$("packager.fetch.icons"));
        props.set(KEY_FLATPACK_ICONS, packager.getIcons());
        resolveIcons(context, packager, distribution, props, packager.getIcons());
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
        props.set(KEY_PROJECT_AUTHORS, context.getModel().getProject().getAuthors());
        props.set(KEY_FLATPAK_URLS, context.getModel().getProject().getLinks().asLinkTemplates(true));
        props.set(KEY_FLATPAK_COMPONENT_ID, getPackager().getComponentId());
        props.set(KEY_FLATPAK_CATEGORIES, getPackager().getCategories());
        props.set(KEY_FLATPAK_CATEGORIES_BY_COMMA, String.join(",", getPackager().getCategories()));
        props.set(KEY_FLATPAK_CATEGORIES_BY_SEMICOLON, String.join(";", getPackager().getCategories()) +
            (getPackager().getCategories().size() > 1 ? ";" : ""));
        props.set(KEY_FLATPAK_DEVELOPER_NAME, getPackager().getDeveloperName());
        props.set(KEY_FLATPAK_REPO_OWNER, packager.getRepository().getOwner());
        props.set(KEY_FLATPAK_REPO_NAME, packager.getRepository().getName());
        props.set(KEY_FLATPAK_REPOSITORY_OWNER, packager.getRepository().getOwner());
        props.set(KEY_FLATPAK_REPOSITORY_NAME, packager.getRepository().getName());
        props.set(KEY_FLATPAK_HAS_SDK_EXTENSIONS, !packager.getSdkExtensions().isEmpty());
        props.set(KEY_FLATPAK_SDK_EXTENSIONS, packager.getSdkExtensions());
        props.set(KEY_FLATPAK_HAS_SDK_FINISH_ARGS, !packager.getFinishArgs().isEmpty());
        props.set(KEY_FLATPAK_SDK_FINISH_ARGS, packager.getFinishArgs().stream()
            .map(MustacheUtils::passThrough)
            .collect(toList()));
        props.set(KEY_FLATPAK_RUNTIME, packager.getRuntime().runtime());
        props.set(KEY_FLATPAK_RUNTIME_VERSION, packager.getRuntimeVersion());
        props.set(KEY_FLATPAK_SDK, packager.getRuntime().sdk());
        props.set(KEY_FLATPAK_INCLUDE_OPENJDK, isFalse(packager.getExtraProperties().get(SKIP_OPENJDK)));
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

        if ("app.yml".equals(fileName)) {
            outputFile = outputDirectory.resolve(getPackager().getComponentId() + ".yml");
        } else if ("app.desktop".equals(fileName)) {
            outputFile = outputDirectory.resolve(getPackager().getComponentId() + ".desktop");
        } else if ("metainfo.xml".equals(fileName)) {
            outputFile = outputDirectory.resolve(getPackager().getComponentId() + ".metainfo.xml");
        }

        writeFile(content, outputFile);
    }
}
