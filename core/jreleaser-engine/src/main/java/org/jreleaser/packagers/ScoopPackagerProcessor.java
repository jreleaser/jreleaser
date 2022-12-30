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

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.ScoopPackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.model.Constants.KEY_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_PROJECT_EFFECTIVE_VERSION;
import static org.jreleaser.model.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.model.Constants.KEY_SCOOP_AUTOUPDATE_EXTRACT_DIR;
import static org.jreleaser.model.Constants.KEY_SCOOP_AUTOUPDATE_URL;
import static org.jreleaser.model.Constants.KEY_SCOOP_BUCKET_REPO_CLONE_URL;
import static org.jreleaser.model.Constants.KEY_SCOOP_BUCKET_REPO_URL;
import static org.jreleaser.model.Constants.KEY_SCOOP_CHECKVER_URL;
import static org.jreleaser.model.Constants.KEY_SCOOP_PACKAGE_NAME;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ScoopPackagerProcessor extends AbstractRepositoryPackagerProcessor<ScoopPackager> {
    public ScoopPackagerProcessor(JReleaserContext context) {
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

        props.put(KEY_SCOOP_BUCKET_REPO_URL,
            releaser.getResolvedRepoUrl(context.getModel(), packager.getBucket().getOwner(), packager.getBucket().getResolvedName()));
        props.put(KEY_SCOOP_BUCKET_REPO_CLONE_URL,
            releaser.getResolvedRepoCloneUrl(context.getModel(), packager.getBucket().getOwner(), packager.getBucket().getResolvedName()));

        props.put(KEY_SCOOP_PACKAGE_NAME, packager.getPackageName());
        props.put(KEY_SCOOP_CHECKVER_URL, resolveCheckverUrl(props));
        props.put(KEY_SCOOP_AUTOUPDATE_URL, resolveAutoupdateUrl(props, distribution));
        String autoupdateExtractDir = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME);
        autoupdateExtractDir = autoupdateExtractDir.replace(context.getModel().getProject().getEffectiveVersion(), "$version");
        props.put(KEY_SCOOP_AUTOUPDATE_EXTRACT_DIR, autoupdateExtractDir);
    }

    private Object resolveCheckverUrl(Map<String, Object> props) {
        if (!getPackager().getCheckverUrl().contains("{{")) {
            return getPackager().getCheckverUrl();
        }
        return resolveTemplate(getPackager().getCheckverUrl(), props);
    }

    private Object resolveAutoupdateUrl(Map<String, Object> props, Distribution distribution) {
        String url = getPackager().getAutoupdateUrl();
        if (isBlank(url)) {
            Artifact artifact = (Artifact) props.get(KEY_DISTRIBUTION_ARTIFACT);
            url = Artifacts.resolveDownloadUrl(context, org.jreleaser.model.api.packagers.ScoopPackager.TYPE, distribution, artifact);
        }

        String artifactFile = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE);
        String projectVersion = (String) props.get(KEY_PROJECT_VERSION);
        String tagName = (String) props.get(KEY_TAG_NAME);
        url = url.replace(projectVersion, "$version");
        artifactFile = artifactFile.replace(projectVersion, "$version");
        tagName = tagName.replace(projectVersion, "$version");

        Map<String, Object> copy = new LinkedHashMap<>(props);
        copy.put(KEY_PROJECT_VERSION, "$version");
        copy.put(KEY_PROJECT_EFFECTIVE_VERSION, "$version");
        copy.put(KEY_TAG_NAME, tagName);
        copy.put(KEY_ARTIFACT_FILE, artifactFile);
        return resolveTemplate(url, copy);
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName) throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = "manifest.json".equals(fileName) ?
            outputDirectory.resolve("bucket").resolve(packager.getPackageName().concat(".json")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
