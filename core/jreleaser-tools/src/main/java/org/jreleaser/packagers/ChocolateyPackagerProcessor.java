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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Github;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.packager.spi.PackagerProcessingException;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.command.Command;

import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_CHOCOLATEY_BUCKET_REPO_CLONE_URL;
import static org.jreleaser.util.Constants.KEY_CHOCOLATEY_BUCKET_REPO_URL;
import static org.jreleaser.util.Constants.KEY_CHOCOLATEY_ICON_URL;
import static org.jreleaser.util.Constants.KEY_CHOCOLATEY_PACKAGE_NAME;
import static org.jreleaser.util.Constants.KEY_CHOCOLATEY_SOURCE;
import static org.jreleaser.util.Constants.KEY_CHOCOLATEY_TITLE;
import static org.jreleaser.util.Constants.KEY_CHOCOLATEY_USERNAME;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY;
import static org.jreleaser.util.Constants.KEY_PROJECT_LICENSE_URL;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChocolateyPackagerProcessor extends AbstractRepositoryPackagerProcessor<Chocolatey> {
    public ChocolateyPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);

        copyPreparedFiles(distribution, props);

        if (packager.isRemoteBuild()) {
            return;
        }

        if (!PlatformUtils.isWindows()) {
            context.getLogger().warn(RB.$("ERROR_packager_requires_platform", "Windows"));
            return;
        }

        createChocolateyPackage(distribution, props);
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        super.doPublishDistribution(distribution, props);

        if (packager.isRemoteBuild()) {
            return;
        }

        if (!PlatformUtils.isWindows()) {
            context.getLogger().warn(RB.$("ERROR_packager_requires_platform", "Windows"));
            return;
        }

        publishChocolateyPackage(distribution, props);
    }

    @Override
    protected void fillPackagerProperties(Map<String, Object> props, Distribution distribution) throws PackagerProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        if (!props.containsKey(KEY_PROJECT_LICENSE_URL) ||
            isBlank((String) props.get(KEY_PROJECT_LICENSE_URL))) {
            context.getLogger().warn(RB.$("ERROR_project_no_license_url"));
        }

        props.put(KEY_CHOCOLATEY_BUCKET_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), packager.getBucket().getOwner(), packager.getBucket().getResolvedName()));
        props.put(KEY_CHOCOLATEY_BUCKET_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), packager.getBucket().getOwner(), packager.getBucket().getResolvedName()));

        props.put(KEY_CHOCOLATEY_PACKAGE_NAME, getPackager().getPackageName());
        props.put(KEY_CHOCOLATEY_USERNAME, getPackager().getUsername());
        props.put(KEY_CHOCOLATEY_TITLE, getPackager().getTitle());
        props.put(KEY_CHOCOLATEY_ICON_URL, resolveTemplate(getPackager().getIconUrl(), props));
        props.put(KEY_CHOCOLATEY_SOURCE, packager.getSource());
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName) throws PackagerProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();
        if (fileName.contains(".github") && (!packager.isRemoteBuild() || !(gitService instanceof Github))) {
            // skip
            return;
        }

        fileName = trimTplExtension(fileName);

        Path outputFile = "binary.nuspec".equals(fileName) ?
            outputDirectory.resolve(distribution.getName()).resolve(packager.getPackageName().concat(".nuspec")) :
            fileName.endsWith(".ps1") ? outputDirectory.resolve(distribution.getName()).resolve(fileName) :
                outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    private void createChocolateyPackage(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        Path packageDirectory = (Path) props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        Path execDirectory = packageDirectory.resolve(distribution.getName());

        Command cmd = new Command("choco")
            .arg("pack")
            .arg(packager.getPackageName().concat(".nuspec"));

        executeCommand(execDirectory, cmd);
    }

    private void publishChocolateyPackage(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        Path packageDirectory = (Path) props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        Path execDirectory = packageDirectory.resolve(distribution.getName());

        Command cmd = new Command("choco")
            .arg("apikey")
            .arg("-k")
            .arg(packager.getResolvedApiKey())
            .arg("-source")
            .arg(packager.getSource());
        executeCommand(execDirectory, cmd);

        cmd = new Command("choco")
            .arg("push")
            .arg("$(ls *.nupkg | % {$_.FullName})")
            .arg("-s")
            .arg(packager.getSource());

        executeCommand(execDirectory, cmd);
    }
}
