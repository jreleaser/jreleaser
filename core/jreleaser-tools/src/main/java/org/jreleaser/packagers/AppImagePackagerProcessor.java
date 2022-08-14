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
import org.jreleaser.model.AppImage;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Github;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.packager.spi.PackagerProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_CATEGORIES;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_CATEGORIES_BY_COMMA;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_COMPONENT_ID;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_DEVELOPER_NAME;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_DISTRIBUTION_URL;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_RELEASES;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_REPO_NAME;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_REPO_OWNER;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_REQUIRES_TERMINAL;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_SCREENSHOTS;
import static org.jreleaser.util.Constants.KEY_APPIMAGE_URLS;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_URL;
import static org.jreleaser.util.Constants.KEY_PROJECT_AUTHORS;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class AppImagePackagerProcessor extends AbstractRepositoryPackagerProcessor<AppImage> {
    public AppImagePackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);
    }

    @Override
    protected void fillPackagerProperties(Map<String, Object> props, Distribution distribution, ProcessingStep processingStep) throws PackagerProcessingException {
        props.put(KEY_PROJECT_AUTHORS, context.getModel().getProject().getAuthors());
        props.put(KEY_APPIMAGE_URLS, context.getModel().getProject().getLinks().asAppdataLinks());
        props.put(KEY_APPIMAGE_COMPONENT_ID, getPackager().getComponentId());
        props.put(KEY_APPIMAGE_CATEGORIES, getPackager().getCategories());
        props.put(KEY_APPIMAGE_CATEGORIES_BY_COMMA, String.join(",", getPackager().getCategories()));
        props.put(KEY_APPIMAGE_DEVELOPER_NAME, getPackager().getDeveloperName());
        props.put(KEY_APPIMAGE_REQUIRES_TERMINAL, getPackager().isRequiresTerminal());
        props.put(KEY_APPIMAGE_REPO_OWNER, packager.getRepository().getOwner());
        props.put(KEY_APPIMAGE_REPO_NAME, packager.getRepository().getName());

        GitService gitService = context.getModel().getRelease().getGitService();
        String str = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "${DISTRIBUTION_VERSION}");
        props.put(KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE, str);
        str = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "${DISTRIBUTION_VERSION}");
        props.put(KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE_NAME, str);
        str = (String) props.get(KEY_DISTRIBUTION_URL);
        str = str.replace(gitService.getEffectiveTagName(context.getModel()), "${DISTRIBUTION_TAG}")
            .replace((String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE), "${DISTRIBUTION_FILE}");
        props.put(KEY_APPIMAGE_DISTRIBUTION_URL, str);

        if (processingStep == ProcessingStep.PREPARE) {
            try {
                props.put(KEY_APPIMAGE_RELEASES, Releasers.releaserFor(context)
                    .listReleases(gitService.getOwner(), gitService.getName())
                    .stream().map(r -> Release.of(r.getUrl(), r.getVersion().toString(), r.getPublishedAt()))
                    .collect(toList()));
            } catch (IOException e) {
                throw new PackagerProcessingException(RB.$("ERROR_unexpected_error"), e);
            }

            props.put(KEY_APPIMAGE_SCREENSHOTS, packager.getScreenshots());
        }
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName) throws PackagerProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();
        if (fileName.contains("github") && !(gitService instanceof Github)) {
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
            outputFile = outputDirectory.resolve(fileName.replace("app", distribution.getExecutable().getName()));
        } else if ("appdata.xml".equals(fileName)) {
            outputFile = outputDirectory.resolve(getPackager().getComponentId() + ".appdata.xml");
        }

        writeFile(content, outputFile);
    }

    protected void writeFile(Project project, Distribution distribution, InputStream inputStream, Map<String, Object> props, Path outputDirectory, String fileName) throws PackagerProcessingException {
        Path outputFile = outputDirectory.resolve(fileName);

        if (fileName.endsWith("app.png")) {
            outputFile = outputDirectory.resolve(fileName.replace("app", distribution.getExecutable().getName()));
        }

        writeFile(inputStream, outputFile);
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
            SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
            return new Release(url, version, format.format(date));
        }
    }
}
