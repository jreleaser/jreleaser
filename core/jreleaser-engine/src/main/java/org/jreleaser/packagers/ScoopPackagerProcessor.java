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

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.ScoopPackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;

import java.nio.file.Path;

import static org.jreleaser.model.Constants.KEY_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_PROJECT_EFFECTIVE_VERSION;
import static org.jreleaser.model.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.model.Constants.KEY_SCOOP_AUTOUPDATE_EXTRACT_DIR;
import static org.jreleaser.model.Constants.KEY_SCOOP_AUTOUPDATE_URL;
import static org.jreleaser.model.Constants.KEY_SCOOP_BUCKET_REPO_CLONE_URL;
import static org.jreleaser.model.Constants.KEY_SCOOP_BUCKET_REPO_URL;
import static org.jreleaser.model.Constants.KEY_SCOOP_CHECKVER_URL;
import static org.jreleaser.model.Constants.KEY_SCOOP_PACKAGE_NAME;
import static org.jreleaser.model.Constants.KEY_SCOOP_REPOSITORY_CLONE_URL;
import static org.jreleaser.model.Constants.KEY_SCOOP_REPOSITORY_URL;
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
    protected void doPackageDistribution(Distribution distribution, TemplateContext props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(props);
    }

    @Override
    protected void fillPackagerProperties(TemplateContext props, Distribution distribution) {
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, distribution.getJava().getMainClass());
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_MODULE, distribution.getJava().getMainModule());
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        props.set(KEY_SCOOP_BUCKET_REPO_URL,
            releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.set(KEY_SCOOP_BUCKET_REPO_CLONE_URL,
            releaser.getResolvedRepoCloneUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.set(KEY_SCOOP_REPOSITORY_URL,
            releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.set(KEY_SCOOP_REPOSITORY_CLONE_URL,
            releaser.getResolvedRepoCloneUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));

        props.set(KEY_SCOOP_PACKAGE_NAME, packager.getPackageName());
        props.set(KEY_SCOOP_CHECKVER_URL, resolveCheckverUrl(props));
        props.set(KEY_SCOOP_AUTOUPDATE_URL, resolveAutoupdateUrl(props, distribution));
        String autoupdateExtractDir = props.get(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME);
        autoupdateExtractDir = autoupdateExtractDir.replace(context.getModel().getProject().getEffectiveVersion(), "$version");
        props.set(KEY_SCOOP_AUTOUPDATE_EXTRACT_DIR, autoupdateExtractDir);
    }

    private Object resolveCheckverUrl(TemplateContext props) {
        if (!getPackager().getCheckverUrl().contains("{{")) {
            return getPackager().getCheckverUrl();
        }
        return resolveTemplate(getPackager().getCheckverUrl(), props);
    }

    private Object resolveAutoupdateUrl(TemplateContext props, Distribution distribution) {
        String url = getPackager().getAutoupdateUrl();
        if (isBlank(url)) {
            Artifact artifact = props.get(KEY_DISTRIBUTION_ARTIFACT);
            url = Artifacts.resolveDownloadUrl(context, org.jreleaser.model.api.packagers.ScoopPackager.TYPE, distribution, artifact);
        }

        String artifactFile = props.get(KEY_DISTRIBUTION_ARTIFACT_FILE);
        String projectVersion = props.get(KEY_PROJECT_VERSION);
        String tagName = props.get(KEY_TAG_NAME);
        url = url.replace(projectVersion, "$version");
        artifactFile = artifactFile.replace(projectVersion, "$version");
        tagName = tagName.replace(projectVersion, "$version");

        TemplateContext copy = new TemplateContext(props);
        copy.set(KEY_PROJECT_VERSION, "$version");
        copy.set(KEY_PROJECT_EFFECTIVE_VERSION, "$version");
        copy.set(KEY_TAG_NAME, tagName);
        copy.set(KEY_ARTIFACT_FILE, artifactFile);
        return resolveTemplate(url, copy);
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             TemplateContext props,
                             Path outputDirectory,
                             String fileName) throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = "manifest.json".equals(fileName) ?
            outputDirectory.resolve("bucket").resolve(packager.getPackageName().concat(".json")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
