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
package org.jreleaser.packagers;

import org.apache.commons.lang3.StringUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.engine.release.Releasers;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Icon;
import org.jreleaser.model.internal.common.Screenshot;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.AppImagePackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

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
import static org.jreleaser.model.Constants.KEY_APPIMAGE_REPO_NAME;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_REPO_OWNER;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_REQUIRES_TERMINAL;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_SCREENSHOTS;
import static org.jreleaser.model.Constants.KEY_APPIMAGE_URLS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_URL;
import static org.jreleaser.model.Constants.KEY_PROJECT_AUTHORS;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.StringUtils.getFilenameExtension;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class AppImagePackagerProcessor extends AbstractRepositoryPackagerProcessor<AppImagePackager> {
    public AppImagePackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        setupPrepare(distribution, props);
        super.doPrepareDistribution(distribution, props);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(props);
    }

    private void setupPrepare(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        try {
            props.put(KEY_APPIMAGE_RELEASES, Releasers.releaserFor(context)
                .listReleases(releaser.getOwner(), releaser.getName()).stream()
                .filter(r -> isReleaseIncluded(packager.getSkipReleases(), r.getVersion().toString()))
                .map(r -> Release.of(r.getUrl(), r.getVersion().toString(), r.getPublishedAt()))
                .collect(toList()));
        } catch (IOException e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }

        props.put(KEY_APPIMAGE_SCREENSHOTS, packager.getScreenshots().stream()
            .map(Screenshot::asScreenshotTemplate)
            .collect(toList()));

        context.getLogger().debug(RB.$("packager.fetch.icons"));
        props.put(KEY_APPIMAGE_ICONS, packager.getIcons());
        for (Icon icon : packager.getIcons()) {
            // check if exists
            String iconUrl = resolveTemplate(icon.getUrl(), props);
            String iconExt = getFilenameExtension(iconUrl);
            Path iconPath = Paths.get(packager.getTemplateDirectory(), "icons",
                icon.getWidth() + "x" + icon.getHeight(),
                distribution.getExecutable().getName() + "." + iconExt);
            iconPath = context.getBasedir().resolve(iconPath);

            if (!Files.exists(iconPath)) {
                // download
                context.getLogger().debug("{} -> {}", iconUrl, context.relativizeToBasedir(iconPath));
                try {
                    org.apache.commons.io.FileUtils.copyURLToFile(
                        new URL(iconUrl),
                        iconPath.toFile(),
                        20000,
                        60000);
                } catch (IOException e) {
                    throw new PackagerProcessingException(RB.$("ERROR_unexpected_download", iconUrl), e);
                }
            }
        }
    }

    @Override
    protected void fillPackagerProperties(Map<String, Object> props, Distribution distribution) throws PackagerProcessingException {
        props.put(KEY_PROJECT_AUTHORS, context.getModel().getProject().getAuthors());
        props.put(KEY_APPIMAGE_URLS, context.getModel().getProject().getLinks().asLinkTemplates());
        props.put(KEY_APPIMAGE_COMPONENT_ID, getPackager().getComponentId());
        props.put(KEY_APPIMAGE_CATEGORIES, getPackager().getCategories());
        props.put(KEY_APPIMAGE_CATEGORIES_BY_COMMA, String.join(",", getPackager().getCategories()));
        props.put(KEY_APPIMAGE_DEVELOPER_NAME, getPackager().getDeveloperName());
        props.put(KEY_APPIMAGE_REQUIRES_TERMINAL, getPackager().isRequiresTerminal());
        props.put(KEY_APPIMAGE_REPO_OWNER, packager.getRepository().getOwner());
        props.put(KEY_APPIMAGE_REPO_NAME, packager.getRepository().getName());

        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();
        String str = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "${DISTRIBUTION_VERSION}");
        props.put(KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE, str);
        str = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "${DISTRIBUTION_VERSION}");
        props.put(KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE_NAME, str);
        str = (String) props.get(KEY_DISTRIBUTION_URL);
        str = str.replace(releaser.getEffectiveTagName(context.getModel()), "${DISTRIBUTION_TAG}")
            .replace((String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE), "${DISTRIBUTION_FILE}");
        props.put(KEY_APPIMAGE_DISTRIBUTION_URL, str);
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             Map<String, Object> props,
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
                fileName = fileName.substring(distribution.getStereotype().toString().length() + 1);
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
    protected void writeFile(Distribution distribution, InputStream inputStream, Map<String, Object> props, Path outputDirectory, String fileName) throws PackagerProcessingException {
        Path outputFile = outputDirectory.resolve(fileName);

        if (fileName.endsWith("app.png")) {
            outputFile = outputDirectory.resolve(fileName.replace("app", distribution.getExecutable().getName()));
        }

        writeFile(inputStream, outputFile);
    }

    private boolean isReleaseIncluded(Set<String> skipReleases, String version) {
        if (null == skipReleases || skipReleases.isEmpty()) {
            return true;
        }

        // 1. exact match
        if (skipReleases.contains(version)) {
            return false;
        }

        // 2. regex match
        for (String regex : skipReleases) {
            Pattern p = Pattern.compile(regex);
            if (p.matcher(version).matches()) return false;
        }

        return true;
    }

    private Optional<Stereotype> resolveStereotype(String fileName) {
        for (Stereotype stereotype : packager.getSupportedStereotypes()) {
            if (fileName.startsWith(stereotype.toString() + "-")) {
                return Optional.of(stereotype);
            }
        }

        return Optional.empty();
    }

    public static class Release {
        private final String url;
        private final String version;
        private final String date;

        private Release(String url, String version, String date) {
            this.url = url;
            this.version = version;
            this.date = date;
        }

        public String getUrl() {
            return url;
        }

        public String getVersion() {
            return version;
        }

        public String getDate() {
            return date;
        }

        public static Release of(String url, String version, Date date) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return new Release(url, version, format.format(date));
        }
    }
}
