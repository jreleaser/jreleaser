/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT;
import static org.jreleaser.model.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.model.Constants.KEY_SPEC_BINARIES;
import static org.jreleaser.model.Constants.KEY_SPEC_DIRECTORIES;
import static org.jreleaser.model.Constants.KEY_SPEC_FILES;
import static org.jreleaser.model.Constants.KEY_SPEC_PACKAGE_NAME;
import static org.jreleaser.model.Constants.KEY_SPEC_RELEASE;
import static org.jreleaser.model.Constants.KEY_SPEC_REQUIRES;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.StringUtils.getFilename;

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
        String artifactFileName = getFilename(artifactPath.getFileName().toString(), packager.getSupportedFileExtensions(distribution.getType()));

        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY) {
            props.set(KEY_PROJECT_VERSION, context.getModel().getProject().version().toRpmVersion());
            props.set(KEY_SPEC_DIRECTORIES, emptyList());
            props.set(KEY_SPEC_BINARIES, singletonList(distribution.getExecutable().resolveExecutable("linux")));
            props.set(KEY_SPEC_FILES, emptyList());
            return;
        }

        try {
            List<String> entries = FileUtils.inspectArchive(artifactPath);

            Set<String> directories = new LinkedHashSet<>();
            List<String> binaries = new ArrayList<>();
            List<String> files = new ArrayList<>();

            entries.stream()
                // skip Windows executables
                .filter(e -> !e.endsWith(distribution.getExecutable().resolveWindowsExtension()))
                // skip directories
                .filter(e -> !e.endsWith("/"))
                // remove root from name
                .map(e -> e.substring(artifactFileName.length() + 1))
                // match only binaries
                .filter(e -> e.startsWith("bin/"))
                .sorted()
                .forEach(entry -> {
                    String[] parts = entry.split("/");
                    binaries.add(parts[1]);
                });

            entries.stream()
                // skip Windows executables
                .filter(e -> !e.endsWith(distribution.getExecutable().resolveWindowsExtension()))
                // skip directories
                .filter(e -> !e.endsWith("/"))
                // remove root from name
                .map(e -> e.substring(artifactFileName.length() + 1))
                // skip executables
                .filter(e -> !e.startsWith("bin/"))
                .sorted()
                .forEach(entry -> {
                    String[] parts = entry.split("/");
                    if (parts.length > 1) directories.add(parts[0]);
                    files.add(entry);
                });

            props.set(KEY_PROJECT_VERSION, context.getModel().getProject().version().toRpmVersion());
            props.set(KEY_SPEC_DIRECTORIES, directories);
            props.set(KEY_SPEC_BINARIES, binaries);
            props.set(KEY_SPEC_FILES, files);
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
        props.set(KEY_SPEC_PACKAGE_NAME, packager.getPackageName());
        props.set(KEY_SPEC_RELEASE, packager.getRelease());
        props.set(KEY_SPEC_REQUIRES, packager.getRequires());
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
