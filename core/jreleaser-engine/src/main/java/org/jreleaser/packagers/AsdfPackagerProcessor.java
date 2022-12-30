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

import org.apache.commons.lang3.StringUtils;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.AsdfPackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;

import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.model.Constants.KEY_ASDF_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_ASDF_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_ASDF_DISTRIBUTION_URL;
import static org.jreleaser.model.Constants.KEY_ASDF_PLUGIN_REPO_URL;
import static org.jreleaser.model.Constants.KEY_ASDF_PLUGIN_TOOL_CHECK;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
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
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(props);
    }

    @Override
    protected void fillPackagerProperties(Map<String, Object> props, Distribution distribution) throws PackagerProcessingException {
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        String repoUrl = releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName());

        props.put(KEY_ASDF_PLUGIN_REPO_URL, repoUrl);
        props.put(KEY_ASDF_PLUGIN_TOOL_CHECK, resolveTemplate(packager.getToolCheck(), props));

        String str = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "$ASDF_INSTALL_VERSION");
        props.put(KEY_ASDF_DISTRIBUTION_ARTIFACT_FILE, str);
        str = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "$version");
        props.put(KEY_ASDF_DISTRIBUTION_ARTIFACT_FILE_NAME, str);
        str = (String) props.get(KEY_DISTRIBUTION_URL);
        str = str.replace(context.getModel().getProject().getEffectiveVersion(), "$version");
        props.put(KEY_ASDF_DISTRIBUTION_URL, str);
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             Map<String, Object> props,
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
