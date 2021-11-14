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
import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.command.Command;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChocolateyToolProcessor extends AbstractRepositoryToolProcessor<Chocolatey> {
    private static final String CHOCOLATEY_PUSH_URL = "https://push.chocolatey.org/";

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

        if (!props.containsKey(Constants.KEY_PROJECT_LICENSE_URL) ||
            isBlank((String) props.get(Constants.KEY_PROJECT_LICENSE_URL))) {
            context.getLogger().warn(RB.$("ERROR_project_no_license_url"));
        }

        props.put(Constants.KEY_CHOCOLATEY_BUCKET_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), tool.getBucket().getOwner(), tool.getBucket().getResolvedName()));
        props.put(Constants.KEY_CHOCOLATEY_BUCKET_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), tool.getBucket().getOwner(), tool.getBucket().getResolvedName()));

        props.put(Constants.KEY_CHOCOLATEY_USERNAME, getTool().getUsername());
        props.put(Constants.KEY_CHOCOLATEY_TITLE, getTool().getTitle());
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = "binary.nuspec".equals(fileName) ?
            outputDirectory.resolve(distribution.getName().concat(".nuspec")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    private void createChocolateyPackage(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Path packageDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY);

        Command cmd = new Command("choco")
            .arg("pack")
            .arg(distribution.getName().concat(".nuspec"));

        executeCommand(packageDirectory, cmd);
    }

    private void publishChocolateyPackage(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Path packageDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY);

        Command cmd = new Command("choco")
            .arg("apikey")
            .arg("-k")
            .arg(tool.getResolvedApiKey())
            .arg("-source")
            .arg(CHOCOLATEY_PUSH_URL);
        executeCommand(packageDirectory, cmd);

        cmd = new Command("choco")
            .arg("push")
            .arg("$(ls *.nupkg | % {$_.FullName})")
            .arg("-s")
            .arg(CHOCOLATEY_PUSH_URL);

        executeCommand(packageDirectory, cmd);
    }
}
