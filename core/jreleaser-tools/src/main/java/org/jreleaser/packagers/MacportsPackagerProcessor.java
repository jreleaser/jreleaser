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

import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Macports;
import org.jreleaser.model.Project;
import org.jreleaser.model.packager.spi.PackagerProcessingException;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.jreleaser.model.Macports.APP_NAME;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_NAME;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_VERSION;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_URL;
import static org.jreleaser.util.Constants.KEY_MACPORTS_APP_NAME;
import static org.jreleaser.util.Constants.KEY_MACPORTS_CATEGORIES;
import static org.jreleaser.util.Constants.KEY_MACPORTS_DISTNAME;
import static org.jreleaser.util.Constants.KEY_MACPORTS_DISTRIBUTION_URL;
import static org.jreleaser.util.Constants.KEY_MACPORTS_JAVA_VERSION;
import static org.jreleaser.util.Constants.KEY_MACPORTS_MAINTAINERS;
import static org.jreleaser.util.Constants.KEY_MACPORTS_PACKAGE_NAME;
import static org.jreleaser.util.Constants.KEY_MACPORTS_REPOSITORY_REPO_CLONE_URL;
import static org.jreleaser.util.Constants.KEY_MACPORTS_REPOSITORY_REPO_URL;
import static org.jreleaser.util.Constants.KEY_MACPORTS_REVISION;
import static org.jreleaser.util.Constants.KEY_PROJECT_LONG_DESCRIPTION;
import static org.jreleaser.util.MustacheUtils.passThrough;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
public class MacportsPackagerProcessor extends AbstractRepositoryPackagerProcessor<Macports> {
    private static final String LINE_SEPARATOR = " \\\n                 ";

    public MacportsPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);
    }

    @Override
    protected void fillPackagerProperties(Map<String, Object> props, Distribution distribution, ProcessingStep processingStep) throws PackagerProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        props.put(KEY_MACPORTS_REPOSITORY_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.put(KEY_MACPORTS_REPOSITORY_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));

        List<String> longDescription = Arrays.asList(context.getModel().getProject().getLongDescription().split("\\n"));

        props.put(KEY_MACPORTS_PACKAGE_NAME, packager.getPackageName());
        props.put(KEY_MACPORTS_REVISION, packager.getRevision());
        props.put(KEY_MACPORTS_CATEGORIES, String.join(" ", packager.getCategories()));
        props.put(KEY_MACPORTS_MAINTAINERS, passThrough(String.join(LINE_SEPARATOR, packager.getResolvedMaintainers(context))));
        props.put(KEY_PROJECT_LONG_DESCRIPTION, passThrough(String.join(LINE_SEPARATOR, longDescription)));
        if (distribution.getType() == Distribution.DistributionType.JAVA_BINARY) {
            props.put(KEY_MACPORTS_JAVA_VERSION, resolveJavaVersion(distribution));
        }
        if (packager.getExtraProperties().containsKey(APP_NAME)) {
            props.put(KEY_MACPORTS_APP_NAME, resolveTemplate(packager.getExtraProperty(APP_NAME), props));
        }

        String distributionUrl = (String) props.get(KEY_DISTRIBUTION_URL);
        String artifactFile = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE);
        if (distributionUrl.endsWith(artifactFile)) {
            distributionUrl = distributionUrl.substring(0, distributionUrl.length() - artifactFile.length() - 1);
        }
        distributionUrl = distributionUrl.replace(context.getModel().getProject().getEffectiveVersion(), "${version}");
        props.put(KEY_MACPORTS_DISTRIBUTION_URL, distributionUrl);

        String artifactFileName = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME);
        String artifactName = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_NAME);
        String artifactVersion = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_VERSION);
        props.put(KEY_MACPORTS_DISTNAME, artifactFileName.replace(artifactName, "${name}")
            .replace(artifactVersion, "${version}"));
    }

    private String resolveJavaVersion(Distribution distribution) {
        String version = distribution.getJava().getVersion();
        if ("8".equals(version)) return "1.8+";
        try {
            Integer.parseInt(version);
            return version + "+";
        } catch (NumberFormatException ignored) {
            // noop
        }

        return version;
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName)
        throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = "Portfile".equals(fileName) ?
            outputDirectory.resolve("ports")
                .resolve(packager.getCategories().get(0))
                .resolve(packager.getPackageName())
                .resolve(fileName) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
