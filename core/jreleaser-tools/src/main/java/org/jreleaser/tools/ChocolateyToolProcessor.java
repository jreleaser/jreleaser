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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChocolateyToolProcessor extends AbstractToolProcessor<Chocolatey> {
    public ChocolateyToolProcessor(JReleaserContext context, Chocolatey tool) {
        super(context, tool);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        if (tool.isRemoteBuild()) {
            // copy from prepare to package
            copyPreparedFiles(distribution, props);
            return true;
        }

        if (!PlatformUtils.isWindows()) {
            context.getLogger().debug("Tool {} must run on Windows", getToolName());
            return false;
        }

        copyPreparedFiles(distribution, props);

        return createChocolateyPackage(distribution, props);
    }

    @Override
    protected boolean doUploadDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        if (tool.isRemoteBuild()) {
            super.doUploadDistribution(distribution, props);
        }
        return uploadChocolateyPackage(distribution, props);
    }

    @Override
    protected String getUploadRepositoryName(Distribution distribution) {
        return null;
    }

    @Override
    protected Set<String> resolveByExtensionsFor(Distribution.DistributionType type) {
        Set<String> set = new LinkedHashSet<>();
        set.add(".zip");
        return set;
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        props.put(Constants.KEY_CHOCOLATEY_USERNAME, getTool().getUsername());
    }

    @Override
    protected void writeFile(Project project, Distribution distribution, String content, Map<String, Object> props, String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = (Path) props.get(Constants.KEY_PREPARE_DIRECTORY);
        Path outputFile = "binary.nuspec".equals(fileName) ?
            outputDirectory.resolve(distribution.getExecutable().concat(".nuspec")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    private boolean createChocolateyPackage(Distribution distribution, Map<String, Object> props) {
        context.getLogger().warn("Local build of chocolatey packages is not yet supported.");
        return false;
    }

    private boolean uploadChocolateyPackage(Distribution distribution, Map<String, Object> props) {
        context.getLogger().warn("Local publication of chocolatey packages is not yet supported.");
        return false;
    }
}
