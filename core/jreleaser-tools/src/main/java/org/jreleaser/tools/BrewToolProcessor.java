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

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Brew;
import org.jreleaser.model.Cask;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class BrewToolProcessor extends AbstractRepositoryToolProcessor<Brew> {
    public BrewToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);
        return true;
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        props.put(Constants.KEY_BREW_FORMULA_NAME, tool.getResolvedFormulaName(props));

        props.put(Constants.KEY_HOMEBREW_TAP_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), tool.getTap().getOwner(), tool.getTap().getName()));
        props.put(Constants.KEY_HOMEBREW_TAP_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), tool.getTap().getOwner(), tool.getTap().getName()));

        props.put(Constants.KEY_BREW_HAS_LIVECHECK, tool.hasLivecheck());
        if (tool.hasLivecheck()) {
            props.put(Constants.KEY_BREW_LIVECHECK, tool.getLivecheck().stream()
                .map(line -> applyTemplate(line, props))
                .map(MustacheUtils::passThrough)
                .collect(Collectors.toList()));
        }

        Cask cask = tool.getCask();
        if (cask.isEnabled()) {
            props.put(Constants.KEY_BREW_CASK_NAME, cask.getResolvedCaskName(props));
            props.put(Constants.KEY_BREW_CASK_DISPLAY_NAME, cask.getResolvedDisplayName(props));
            props.put(Constants.KEY_BREW_CASK_HAS_UNINSTALL, !cask.getUninstallItems().isEmpty());
            props.put(Constants.KEY_BREW_CASK_HAS_PKG, isNotBlank(cask.getPkgName()));
            if (isNotBlank(cask.getPkgName())) {
                props.put(Constants.KEY_BREW_CASK_PKG, cask.getResolvedPkgName(props));
            }
            props.put(Constants.KEY_BREW_CASK_HAS_APP, isNotBlank(cask.getAppName()));
            if (isNotBlank(cask.getAppName())) {
                props.put(Constants.KEY_BREW_CASK_APP, cask.getResolvedAppName(props));
            }
            props.put(Constants.KEY_BREW_CASK_UNINSTALL, cask.getUninstallItems());
            props.put(Constants.KEY_BREW_CASK_HAS_ZAP, !cask.getZapItems().isEmpty());
            props.put(Constants.KEY_BREW_CASK_ZAP, cask.getZapItems());
            String appcast = cask.getResolvedAppcast(props);
            props.put(Constants.KEY_BREW_CASK_HAS_APPCAST, isNotBlank(appcast));
            props.put(Constants.KEY_BREW_CASK_APPCAST, appcast);

            for (Artifact artifact : distribution.getArtifacts()) {
                if (!artifact.isActive()) continue;
                if (artifact.getPath().endsWith(".zip") && !isTrue(artifact.getExtraProperties().get("skipBrew"))) {
                    String artifactFileName = artifact.getEffectivePath(context).getFileName().toString();
                    Map<String, Object> newProps = new LinkedHashMap<>(props);
                    newProps.put(Constants.KEY_ARTIFACT_FILE_NAME, artifactFileName);
                    props.put(Constants.KEY_DISTRIBUTION_ARTIFACT_NAME, getFilename(artifactFileName));
                    String artifactUrl = applyTemplate(context.getModel().getRelease().getGitService().getDownloadUrl(), newProps);
                    props.put(Constants.KEY_DISTRIBUTION_URL, artifactUrl);
                    props.put(Constants.KEY_BREW_CASK_HAS_BINARY, true);
                    break;
                }
            }
        } else if ((distribution.getType() == Distribution.DistributionType.JAVA_BINARY ||
            distribution.getType() == Distribution.DistributionType.SINGLE_JAR) &&
            !isTrue(tool.getExtraProperties().get("javaSkip")) &&
            !isTrue(tool.getExtraProperties().get("skipJava"))) {
            tool.addDependency("openjdk@" + props.get(Constants.KEY_DISTRIBUTION_JAVA_VERSION));
        }

        props.put(Constants.KEY_BREW_DEPENDENCIES, tool.getDependenciesAsList()
            .stream()
            // prevent Mustache from converting quotes into &quot;
            .map(dependency -> passThrough(dependency.toString()))
            .collect(Collectors.toList()));
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

        if (tool.getCask().isEnabled()) {
            if ("formula.rb".equals(fileName)) return;
            Path outputFile = "cask.rb".equals(fileName) ?
                outputDirectory.resolve("Casks").resolve(tool.getCask().getResolvedCaskName(props).concat(".rb")) :
                outputDirectory.resolve(fileName);
            writeFile(content, outputFile);
        } else {
            if ("cask.rb".equals(fileName)) return;
            Path outputFile = "formula.rb".equals(fileName) ?
                outputDirectory.resolve("Formula").resolve(distribution.getExecutable().concat(".rb")) :
                outputDirectory.resolve(fileName);
            writeFile(content, outputFile);
        }
    }
}
