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

import org.apache.commons.lang3.StringUtils;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.AsdfPackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;

import java.nio.file.Path;

import static org.jreleaser.model.Constants.KEY_ASDF_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_ASDF_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_ASDF_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME;
import static org.jreleaser.model.Constants.KEY_ASDF_DISTRIBUTION_URL;
import static org.jreleaser.model.Constants.KEY_ASDF_PLUGIN_REPOSITORY_URL;
import static org.jreleaser.model.Constants.KEY_ASDF_PLUGIN_REPO_URL;
import static org.jreleaser.model.Constants.KEY_ASDF_PLUGIN_TOOL_CHECK;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_URL;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class AsdfPackagerProcessor extends AbstractRepositoryPackagerProcessor<AsdfPackager> {
    public AsdfPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, TemplateContext props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(props);
    }

    @Override
    protected void fillPackagerProperties(TemplateContext props, Distribution distribution) {
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, distribution.getJava().getMainClass());
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_MODULE, distribution.getJava().getMainModule());
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        String repoUrl = releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName());

        props.set(KEY_ASDF_PLUGIN_REPO_URL, repoUrl);
        props.set(KEY_ASDF_PLUGIN_REPOSITORY_URL, repoUrl);
        props.set(KEY_ASDF_PLUGIN_TOOL_CHECK, resolveTemplate(packager.getToolCheck(), props));

        String str = props.get(KEY_DISTRIBUTION_ARTIFACT_FILE);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "$ASDF_INSTALL_VERSION");
        props.set(KEY_ASDF_DISTRIBUTION_ARTIFACT_FILE, str);
        str = props.get(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "$version");
        props.set(KEY_ASDF_DISTRIBUTION_ARTIFACT_FILE_NAME, str);
        str = props.get(KEY_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "$version");
        props.set(KEY_ASDF_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME, str);
        str = props.get(KEY_DISTRIBUTION_URL);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "$version");
        props.set(KEY_ASDF_DISTRIBUTION_URL, str);
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             TemplateContext props,
                             Path outputDirectory,
                             String fileName) throws PackagerProcessingException {
        Releaser<?> gitService = context.getModel().getRelease().getReleaser();
        if (fileName.contains("github") && !(gitService instanceof GithubReleaser)) {
            // skip
            return;
        } else if (fileName.contains("-github")) {
            fileName = StringUtils.remove(fileName, "-github");
        }

        fileName = trimTplExtension(fileName);

        Path outputFile = outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
