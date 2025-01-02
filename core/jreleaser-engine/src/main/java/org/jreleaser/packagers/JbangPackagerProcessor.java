/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.JbangPackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jreleaser.model.Constants.KEY_JBANG_ALIAS_NAME;
import static org.jreleaser.model.Constants.KEY_JBANG_CATALOG_REPO_CLONE_URL;
import static org.jreleaser.model.Constants.KEY_JBANG_CATALOG_REPO_URL;
import static org.jreleaser.model.Constants.KEY_JBANG_DISTRIBUTION_GA;
import static org.jreleaser.model.Constants.KEY_JBANG_REPOSITORY_CLONE_URL;
import static org.jreleaser.model.Constants.KEY_JBANG_REPOSITORY_URL;
import static org.jreleaser.model.Constants.KEY_JBANG_SCRIPT_NAME;
import static org.jreleaser.model.Constants.KEY_REVERSE_DOMAIN;
import static org.jreleaser.model.Constants.KEY_REVERSE_REPO_HOST;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JbangPackagerProcessor extends AbstractRepositoryPackagerProcessor<JbangPackager> {
    public JbangPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return distribution.getType() != org.jreleaser.model.Distribution.DistributionType.JLINK;
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, TemplateContext props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(props);
    }

    @Override
    protected boolean verifyAndAddArtifacts(TemplateContext props, Distribution distribution) {
        return true;
    }

    @Override
    protected void fillPackagerProperties(TemplateContext props, Distribution distribution) {
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        props.set(KEY_JBANG_CATALOG_REPO_URL,
            releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.set(KEY_JBANG_CATALOG_REPO_CLONE_URL,
            releaser.getResolvedRepoCloneUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));

        props.set(KEY_JBANG_REPOSITORY_URL,
            releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.set(KEY_JBANG_REPOSITORY_CLONE_URL,
            releaser.getResolvedRepoCloneUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));

        String aliasName = sanitizeAlias(packager.getAlias());
        String scriptName = aliasName;
        if (context.getModel().getProject().isSnapshot()) {
            aliasName += "-snapshot";
            scriptName += "_snapshot";
        }
        scriptName = sanitizeScriptName(scriptName);

        props.set(KEY_JBANG_ALIAS_NAME, aliasName);
        props.set(KEY_JBANG_SCRIPT_NAME, scriptName);

        String jbangDistributionGA = (String) packager.resolvedExtraProperties().get(KEY_JBANG_DISTRIBUTION_GA);
        if (isBlank(jbangDistributionGA)) {
            if (context.getModel().getProject().isSnapshot()) {
                // if single
                // {{reverseRepoHost}}.{{repoOwner}}:{{distributionArtifactId}}
                // if multi-project
                // {{reverseRepoHost}}.{{repoOwner}}.{{repoName}}:{{distributionArtifactId}}

                String reverseRepoHost = releaser.getReverseRepoHost();
                if (packager.getExtraProperties().containsKey(KEY_REVERSE_DOMAIN)) {
                    reverseRepoHost = (String) packager.getExtraProperties().get(KEY_REVERSE_DOMAIN);
                } else if (isBlank(reverseRepoHost)) {
                    reverseRepoHost = (String) packager.getExtraProperties().get(KEY_REVERSE_REPO_HOST);
                }

                StringBuilder b = new StringBuilder(reverseRepoHost)
                    .append(".")
                    .append(releaser.getOwner());
                if (distribution.getJava().isMultiProject()) {
                    b.append(".")
                        .append(releaser.getName());
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
        props.set(KEY_JBANG_DISTRIBUTION_GA, jbangDistributionGA);
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
        scriptName = scriptName.replace("-", "_");
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
    protected void writeFile(Distribution distribution,
                             String content,
                             TemplateContext props,
                             Path outputDirectory,
                             String fileName)
        throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        String scriptName = props.get(KEY_JBANG_SCRIPT_NAME);
        Path outputFile = "jbang.java".equals(fileName) ?
            outputDirectory.resolve(scriptName.concat(".java")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    @Override
    protected void prepareWorkingCopy(TemplateContext props, Path directory, Distribution distribution) throws IOException {
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
            Files.write(catalog, merged.toPrettyString().getBytes(UTF_8));
        } else {
            // copy all files
            super.prepareWorkingCopy(props, directory, distribution);
        }
    }
}