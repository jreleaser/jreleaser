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

import org.jreleaser.model.Brew;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class BrewToolProcessor extends AbstractRepositoryToolProcessor<Brew> {
    public BrewToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        copyPreparedFiles(distribution, props);
        return true;
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        Project project = context.getModel().getProject();
        GitService gitService = context.getModel().getRelease().getGitService();

        props.put(Constants.KEY_BREW_FORMULA_NAME, tool.getResolvedFormulaName(props));

        props.put(Constants.KEY_HOMEBREW_TAP_REPO_URL,
            gitService.getResolvedRepoUrl(project, tool.getTap().getOwner(), tool.getTap().getName()));
        props.put(Constants.KEY_HOMEBREW_TAP_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(project, tool.getTap().getOwner(), tool.getTap().getName()));

        if (distribution.getType() == Distribution.DistributionType.JAVA_BINARY ||
            distribution.getType() == Distribution.DistributionType.SINGLE_JAR) {
            getTool().addDependency("openjdk@" + props.get(Constants.KEY_DISTRIBUTION_JAVA_VERSION));
        }

        props.put(Constants.KEY_BREW_DEPENDENCIES, getTool().getDependenciesAsList()
            .stream()
            // prevent Mustache from converting quotes into &quot;
            .map(dependency -> MustacheUtils.passThrough(dependency.toString()))
            .collect(Collectors.toList()));
    }

    @Override
    protected void writeFile(Project project, Distribution distribution, String content, Map<String, Object> props, String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = (Path) props.get(Constants.KEY_PREPARE_DIRECTORY);
        Path outputFile = "formula.rb".equals(fileName) ?
            outputDirectory.resolve("Formula").resolve(distribution.getExecutable().concat(".rb")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
