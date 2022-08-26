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
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Flatpak;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Github;
import org.jreleaser.model.Icon;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Screenshot;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.packager.spi.PackagerProcessingException;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.MustacheUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT;
import static org.jreleaser.util.Constants.KEY_FLATPACK_ICONS;
import static org.jreleaser.util.Constants.KEY_FLATPAK_BINARIES;
import static org.jreleaser.util.Constants.KEY_FLATPAK_CATEGORIES;
import static org.jreleaser.util.Constants.KEY_FLATPAK_CATEGORIES_BY_COMMA;
import static org.jreleaser.util.Constants.KEY_FLATPAK_CATEGORIES_BY_SEMICOLON;
import static org.jreleaser.util.Constants.KEY_FLATPAK_COMPONENT_ID;
import static org.jreleaser.util.Constants.KEY_FLATPAK_DEVELOPER_NAME;
import static org.jreleaser.util.Constants.KEY_FLATPAK_DIRECTORIES;
import static org.jreleaser.util.Constants.KEY_FLATPAK_FILES;
import static org.jreleaser.util.Constants.KEY_FLATPAK_HAS_SDK_EXTENSIONS;
import static org.jreleaser.util.Constants.KEY_FLATPAK_HAS_SDK_FINISH_ARGS;
import static org.jreleaser.util.Constants.KEY_FLATPAK_INCLUDE_OPENJDK;
import static org.jreleaser.util.Constants.KEY_FLATPAK_RELEASES;
import static org.jreleaser.util.Constants.KEY_FLATPAK_REPO_NAME;
import static org.jreleaser.util.Constants.KEY_FLATPAK_REPO_OWNER;
import static org.jreleaser.util.Constants.KEY_FLATPAK_RUNTIME;
import static org.jreleaser.util.Constants.KEY_FLATPAK_RUNTIME_VERSION;
import static org.jreleaser.util.Constants.KEY_FLATPAK_SCREENSHOTS;
import static org.jreleaser.util.Constants.KEY_FLATPAK_SDK;
import static org.jreleaser.util.Constants.KEY_FLATPAK_SDK_EXTENSIONS;
import static org.jreleaser.util.Constants.KEY_FLATPAK_SDK_FINISH_ARGS;
import static org.jreleaser.util.Constants.KEY_FLATPAK_URLS;
import static org.jreleaser.util.Constants.KEY_PROJECT_AUTHORS;
import static org.jreleaser.util.Constants.SKIP_OPENJDK;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.getFilenameExtension;
import static org.jreleaser.util.StringUtils.isFalse;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class FlatpakPackagerProcessor extends AbstractRepositoryPackagerProcessor<Flatpak> {
    public FlatpakPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        setupPrepare(distribution, props);
        super.doPrepareDistribution(distribution, props);
    }

    private void setupPrepare(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        Artifact artifact = (Artifact) props.get(KEY_DISTRIBUTION_ARTIFACT);
        Path artifactPath = artifact.getResolvedPath(context, distribution);
        String artifactFileName = getFilename(artifactPath.getFileName().toString(), packager.getSupportedExtensions(distribution));

        try {
            List<String> entries = FileUtils.inspectArchive(artifactPath);

            Set<String> directories = new LinkedHashSet<>();
            List<String> binaries = new ArrayList<>();
            List<String> files = new ArrayList<>();

            entries.stream()
                // skip Windows executables
                .filter(e -> !e.endsWith(distribution.getExecutable().resolveWindowsExtension()))
                // skip directories
                .filter(e -> !e.endsWith("/"))
                // remove root from name
                .map(e -> e.substring(artifactFileName.length() + 1))
                // match only binaries
                .filter(e -> e.startsWith("bin/"))
                .sorted()
                .forEach(entry -> {
                    String[] parts = entry.split("/");
                    binaries.add(parts[1]);
                });

            entries.stream()
                // skip Windows executables
                .filter(e -> !e.endsWith(distribution.getExecutable().resolveWindowsExtension()))
                // skip directories
                .filter(e -> !e.endsWith("/"))
                // remove root from name
                .map(e -> e.substring(artifactFileName.length() + 1))
                // skip executables
                .filter(e -> !e.startsWith("bin/"))
                .sorted()
                .forEach(entry -> {
                    String[] parts = entry.split("/");
                    if (parts.length > 1) directories.add(parts[0]);
                    files.add(entry);
                });

            props.put(KEY_FLATPAK_DIRECTORIES, directories);
            props.put(KEY_FLATPAK_BINARIES, binaries);
            props.put(KEY_FLATPAK_FILES, files);
        } catch (IOException e) {
            throw new PackagerProcessingException("ERROR", e);
        }

        GitService gitService = context.getModel().getRelease().getGitService();

        try {
            props.put(KEY_FLATPAK_RELEASES, Releasers.releaserFor(context)
                .listReleases(gitService.getOwner(), gitService.getName())
                .stream().map(r -> Release.of(r.getUrl(), r.getVersion().toString(), r.getPublishedAt()))
                .collect(toList()));
        } catch (IOException e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }

        props.put(KEY_FLATPAK_SCREENSHOTS, packager.getScreenshots().stream()
            .map(Screenshot::asScreenshotTemplate)
            .collect(toList()));

        context.getLogger().debug(RB.$("packager.fetch.icons"));
        props.put(KEY_FLATPACK_ICONS, packager.getIcons());
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
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);
    }

    @Override
    protected void fillPackagerProperties(Map<String, Object> props, Distribution distribution) throws PackagerProcessingException {
        props.put(KEY_PROJECT_AUTHORS, context.getModel().getProject().getAuthors());
        props.put(KEY_FLATPAK_URLS, context.getModel().getProject().getLinks().asLinkTemplates());
        props.put(KEY_FLATPAK_COMPONENT_ID, getPackager().getComponentId());
        props.put(KEY_FLATPAK_CATEGORIES, getPackager().getCategories());
        props.put(KEY_FLATPAK_CATEGORIES_BY_COMMA, String.join(",", getPackager().getCategories()));
        props.put(KEY_FLATPAK_CATEGORIES_BY_SEMICOLON, String.join(";", getPackager().getCategories()) +
            (getPackager().getCategories().size() > 1 ? ";" : ""));
        props.put(KEY_FLATPAK_DEVELOPER_NAME, getPackager().getDeveloperName());
        props.put(KEY_FLATPAK_REPO_OWNER, packager.getRepository().getOwner());
        props.put(KEY_FLATPAK_REPO_NAME, packager.getRepository().getName());
        props.put(KEY_FLATPAK_HAS_SDK_EXTENSIONS, !packager.getSdkExtensions().isEmpty());
        props.put(KEY_FLATPAK_SDK_EXTENSIONS, packager.getSdkExtensions());
        props.put(KEY_FLATPAK_HAS_SDK_FINISH_ARGS, !packager.getFinishArgs().isEmpty());
        props.put(KEY_FLATPAK_SDK_FINISH_ARGS, packager.getFinishArgs().stream()
            .map(MustacheUtils::passThrough)
            .collect(toList()));
        props.put(KEY_FLATPAK_RUNTIME, packager.getRuntime().runtime());
        props.put(KEY_FLATPAK_RUNTIME_VERSION, packager.getRuntimeVersion());
        props.put(KEY_FLATPAK_SDK, packager.getRuntime().sdk());
        props.put(KEY_FLATPAK_INCLUDE_OPENJDK, isFalse(packager.getExtraProperties().get(SKIP_OPENJDK)));
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

        switch (fileName) {
            case "app.yml":
                outputFile = outputDirectory.resolve(getPackager().getComponentId() + ".yml");
                break;
            case "app.desktop":
                outputFile = outputDirectory.resolve(getPackager().getComponentId() + ".desktop");
                break;
            case "metainfo.xml":
                outputFile = outputDirectory.resolve(getPackager().getComponentId() + ".metainfo.xml");
                break;
        }

        writeFile(content, outputFile);
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
