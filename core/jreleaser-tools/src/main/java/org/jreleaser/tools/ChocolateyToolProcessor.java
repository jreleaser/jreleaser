/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.tools;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Github;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.command.Command;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.model.Chocolatey.SKIP_CHOCOLATEY;
import static org.jreleaser.model.util.Templates.resolve;
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
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChocolateyToolProcessor extends AbstractRepositoryToolProcessor<Chocolatey> {
    public ChocolateyToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);

        copyPreparedFiles(distribution, props);

        if (tool.isRemoteBuild()) {
            return;
        }

        if (!PlatformUtils.isWindows()) {
            context.getLogger().warn(RB.$("ERROR_tool_requires_platform", "Windows"));
            return;
        }

        createChocolateyPackage(distribution, props);
    }

    @Override
    protected void prepareWorkingCopy(Map<String, Object> props, Path directory, Distribution distribution) throws ToolProcessingException, IOException {
        super.prepareWorkingCopy(props, directory.resolve(distribution.getName()), distribution);
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        super.doPublishDistribution(distribution, props);

        if (tool.isRemoteBuild()) {
            return;
        }

        if (!PlatformUtils.isWindows()) {
            context.getLogger().warn(RB.$("ERROR_tool_requires_platform", "Windows"));
            return;
        }

        publishChocolateyPackage(distribution, props);
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        if (!props.containsKey(KEY_PROJECT_LICENSE_URL) ||
            isBlank((String) props.get(KEY_PROJECT_LICENSE_URL))) {
            context.getLogger().warn(RB.$("ERROR_project_no_license_url"));
        }

        props.put(KEY_CHOCOLATEY_BUCKET_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), tool.getBucket().getOwner(), tool.getBucket().getResolvedName()));
        props.put(KEY_CHOCOLATEY_BUCKET_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), tool.getBucket().getOwner(), tool.getBucket().getResolvedName()));

        props.put(KEY_CHOCOLATEY_PACKAGE_NAME, getTool().getPackageName());
        props.put(KEY_CHOCOLATEY_USERNAME, getTool().getUsername());
        props.put(KEY_CHOCOLATEY_TITLE, getTool().getTitle());
        props.put(KEY_CHOCOLATEY_ICON_URL, resolve(getTool().getIconUrl(), props));
        props.put(KEY_CHOCOLATEY_SOURCE, tool.getSource());
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName) throws ToolProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();
        if (fileName.contains(".github") && (!tool.isRemoteBuild() || !(gitService instanceof Github))) {
            // skip
            return;
        }

        fileName = trimTplExtension(fileName);

        Path outputFile = "binary.nuspec".equals(fileName) ?
            outputDirectory.resolve(tool.getPackageName().concat(".nuspec")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    private void createChocolateyPackage(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Path packageDirectory = (Path) props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);

        Command cmd = new Command("choco")
            .arg("pack")
            .arg(tool.getPackageName().concat(".nuspec"));

        executeCommand(packageDirectory, cmd);
    }

    private void publishChocolateyPackage(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Path packageDirectory = (Path) props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);

        Command cmd = new Command("choco")
            .arg("apikey")
            .arg("-k")
            .arg(tool.getResolvedApiKey())
            .arg("-source")
            .arg(tool.getSource());
        executeCommand(packageDirectory, cmd);

        cmd = new Command("choco")
            .arg("push")
            .arg("$(ls *.nupkg | % {$_.FullName})")
            .arg("-s")
            .arg(tool.getSource());

        executeCommand(packageDirectory, cmd);
    }

    @Override
    protected boolean isSkipped(Artifact artifact) {
        return isTrue(artifact.getExtraProperties().get(SKIP_CHOCOLATEY));
    }
}
