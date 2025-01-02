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
import org.jreleaser.model.internal.packagers.SpecPackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.model.Constants.KEY_SPEC_BINARIES;
import static org.jreleaser.model.Constants.KEY_SPEC_DIRECTORIES;
import static org.jreleaser.model.Constants.KEY_SPEC_FILES;
import static org.jreleaser.model.Constants.KEY_SPEC_PACKAGE_NAME;
import static org.jreleaser.model.Constants.KEY_SPEC_RELEASE;
import static org.jreleaser.model.Constants.KEY_SPEC_REPOSITORY_CLONE_URL;
import static org.jreleaser.model.Constants.KEY_SPEC_REPOSITORY_URL;
import static org.jreleaser.model.Constants.KEY_SPEC_REQUIRES;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
public class SpecPackagerProcessor extends AbstractRepositoryPackagerProcessor<SpecPackager> {
    public SpecPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        setupFiles(distribution, props);
        super.doPrepareDistribution(distribution, props);
    }

    private void setupFiles(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        Artifact artifact = props.get(KEY_DISTRIBUTION_ARTIFACT);
        Path artifactPath = artifact.getResolvedPath(context, distribution);

        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY) {
            props.set(KEY_PROJECT_VERSION, context.getModel().getProject().version().toRpmVersion());
            props.set(KEY_SPEC_DIRECTORIES, emptyList());
            props.set(KEY_SPEC_BINARIES, singletonList(distribution.getExecutable().resolveExecutable("linux")));
            props.set(KEY_SPEC_FILES, emptyList());
            return;
        }

        try {
            FileUtils.CategorizedArchive categorizedArchive = FileUtils.categorizeUnixArchive(
                distribution.getExecutable().resolveWindowsExtension(),
                artifactPath);

            props.set(KEY_SPEC_DIRECTORIES, categorizedArchive.getDirectories());
            props.set(KEY_SPEC_BINARIES, categorizedArchive.getBinaries());
            props.set(KEY_SPEC_FILES, categorizedArchive.getFiles());
            props.set(KEY_PROJECT_VERSION, context.getModel().getProject().version().toRpmVersion());
        } catch (IOException e) {
            throw new PackagerProcessingException("ERROR", e);
        }
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
        props.set(KEY_SPEC_PACKAGE_NAME, packager.getPackageName());
        props.set(KEY_SPEC_RELEASE, packager.getRelease());
        props.set(KEY_SPEC_REQUIRES, packager.getRequires());

        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();
        props.set(KEY_SPEC_REPOSITORY_URL,
            releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.set(KEY_SPEC_REPOSITORY_CLONE_URL,
            releaser.getResolvedRepoCloneUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             TemplateContext props,
                             Path outputDirectory,
                             String fileName) throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = "app.spec".equals(fileName) ?
            outputDirectory.resolve(packager.getPackageName().concat(".spec")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
