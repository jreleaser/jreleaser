/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.PlatformUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChocolateyToolProcessor extends AbstractRepositoryToolProcessor<Chocolatey> {
    public ChocolateyToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);

        if (tool.isRemoteBuild()) {
            // copy from prepare to package
            copyPreparedFiles(distribution, props);
            return true;
        }

        if (!PlatformUtils.isWindows()) {
            context.getLogger().debug("must run on Windows", getToolName());
            return false;
        }

        copyPreparedFiles(distribution, props);

        return createChocolateyPackage(distribution, props);
    }

    @Override
    protected void prepareWorkingCopy(Map<String, Object> props, Path directory, Distribution distribution) throws IOException {
        super.prepareWorkingCopy(props, directory.resolve(distribution.getName()), distribution);
    }

    @Override
    protected boolean doPublishDistribution(Distribution distribution, Releaser releaser, Map<String, Object> props) throws ToolProcessingException {
        if (tool.isRemoteBuild()) {
            return super.doPublishDistribution(distribution, releaser, props);
        }
        return publishChocolateyPackage(distribution, props);
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        props.put(Constants.KEY_CHOCOLATEY_BUCKET_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), tool.getBucket().getOwner(), tool.getBucket().getName()));
        props.put(Constants.KEY_CHOCOLATEY_BUCKET_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), tool.getBucket().getOwner(), tool.getBucket().getName()));

        props.put(Constants.KEY_CHOCOLATEY_USERNAME, getTool().getUsername());
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
            outputDirectory.resolve(distribution.getExecutable().concat(".nuspec")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    private boolean createChocolateyPackage(Distribution distribution, Map<String, Object> props) {
        context.getLogger().warn("local build is not yet supported.");
        return false;
    }

    private boolean publishChocolateyPackage(Distribution distribution, Map<String, Object> props) {
        context.getLogger().warn("local publication is not yet supported.");
        return false;
    }
}
