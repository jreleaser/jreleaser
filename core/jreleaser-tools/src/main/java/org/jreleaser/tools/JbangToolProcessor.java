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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Jbang;
import org.jreleaser.model.Project;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_REVERSE_DOMAIN;
import static org.jreleaser.util.Constants.KEY_REVERSE_REPO_HOST;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JbangToolProcessor extends AbstractRepositoryToolProcessor<Jbang> {
    public JbangToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return distribution.getType() != Distribution.DistributionType.JLINK;
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);
        return true;
    }

    @Override
    protected boolean verifyAndAddArtifacts(Map<String, Object> props,
                                            Distribution distribution) throws ToolProcessingException {
        return true;
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        props.put(Constants.KEY_JBANG_CATALOG_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), tool.getCatalog().getOwner(), tool.getCatalog().getName()));
        props.put(Constants.KEY_JBANG_CATALOG_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), tool.getCatalog().getOwner(), tool.getCatalog().getName()));

        String aliasName = sanitizeAlias(tool.getAlias());
        String scriptName = aliasName;
        if (context.getModel().getProject().isSnapshot()) {
            aliasName += "-snapshot";
            scriptName += "_snapshot";
        }
        scriptName = sanitizeScriptName(scriptName);

        props.put(Constants.KEY_JBANG_ALIAS_NAME, aliasName);
        props.put(Constants.KEY_JBANG_SCRIPT_NAME, scriptName);

        String jbangDistributionGA = (String) tool.getResolvedExtraProperties().get(Constants.KEY_JBANG_DISTRIBUTION_GA);
        if (isBlank(jbangDistributionGA)) {
            if (context.getModel().getProject().isSnapshot()) {
                // if single
                // {{reverseRepoHost}}.{{repoOwner}}:{{distributionArtifactId}}
                // if multi-project
                // {{reverseRepoHost}}.{{repoOwner}}.{{repoName}}:{{distributionArtifactId}}

                String reverseRepoHost = gitService.getReverseRepoHost();
                if (tool.getExtraProperties().containsKey(KEY_REVERSE_DOMAIN)) {
                    reverseRepoHost = (String) tool.getExtraProperties().get(KEY_REVERSE_DOMAIN);
                } else if (isBlank(reverseRepoHost)) {
                    reverseRepoHost = (String) tool.getExtraProperties().get(KEY_REVERSE_REPO_HOST);
                }

                StringBuilder b = new StringBuilder(reverseRepoHost)
                    .append(".")
                    .append(gitService.getOwner());
                if (distribution.getJava().isMultiProject()) {
                    b.append(".")
                        .append(gitService.getName());
                }
                b.append(":")
                    .append(distribution.getJava().getArtifactId());

                jbangDistributionGA = b.toString();
            } else {
                jbangDistributionGA = distribution.getJava().getGroupId() +
                    ":" +
                    distribution.getJava().getArtifactId();
            }
        }
        props.put(Constants.KEY_JBANG_DISTRIBUTION_GA, jbangDistributionGA);
    }

    private String sanitizeAlias(String alias) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < alias.length(); i++) {
            char ch = alias.charAt(i);
            if (Character.isJavaIdentifierPart(ch) || ch == '-') {
                b.append(ch);
            }
        }
        return b.toString();
    }

    private String sanitizeScriptName(String scriptName) {
        scriptName = scriptName.replaceAll("-", "_");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < scriptName.length(); i++) {
            char ch = scriptName.charAt(i);
            if (Character.isJavaIdentifierPart(ch)) {
                b.append(ch);
            }
        }
        return b.toString();
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

        String scriptName = (String) props.get(Constants.KEY_JBANG_SCRIPT_NAME);
        Path outputFile = "jbang.java".equals(fileName) ?
            outputDirectory.resolve(scriptName.concat(".java")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    @Override
    protected void prepareWorkingCopy(Map<String, Object> props, Path directory, Distribution distribution) throws IOException {
        Path catalog = directory.resolve("jbang-catalog.json");

        if (catalog.toFile().exists()) {
            // read previous catalog
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode previous = objectMapper.readTree(Files.readAllBytes(catalog));

            // copy all files
            super.prepareWorkingCopy(props, directory, distribution);

            // read current catalog
            JsonNode current = objectMapper.readTree(Files.readAllBytes(catalog));

            // merge catalogs
            JsonNode merged = JsonUtils.merge(previous, current);

            // write merged catalog
            Files.write(catalog, merged.toPrettyString().getBytes());
        } else {
            // copy all files
            super.prepareWorkingCopy(props, directory, distribution);
        }
    }
}