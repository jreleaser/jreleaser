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

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Spec;
import org.jreleaser.model.packager.spi.PackagerProcessingException;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT;
import static org.jreleaser.util.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.util.Constants.KEY_SPEC_BINARIES;
import static org.jreleaser.util.Constants.KEY_SPEC_DIRECTORIES;
import static org.jreleaser.util.Constants.KEY_SPEC_FILES;
import static org.jreleaser.util.Constants.KEY_SPEC_PACKAGE_NAME;
import static org.jreleaser.util.Constants.KEY_SPEC_RELEASE;
import static org.jreleaser.util.Constants.KEY_SPEC_REQUIRES;
import static org.jreleaser.util.StringUtils.getFilename;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
public class SpecPackagerProcessor extends AbstractRepositoryPackagerProcessor<Spec> {
    public SpecPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        setupJavaBinary(distribution, props);
        super.doPrepareDistribution(distribution, props);
    }

    private void setupJavaBinary(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        Artifact artifact = (Artifact) props.get(KEY_DISTRIBUTION_ARTIFACT);
        Path artifactPath = artifact.getResolvedPath(context, distribution);
        String artifactFileName = getFilename(artifactPath.getFileName().toString(), packager.getSupportedExtensions(distribution));

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

            props.put(KEY_PROJECT_VERSION, context.getModel().getProject().version().toRpmVersion());
            props.put(KEY_SPEC_DIRECTORIES, directories);
            props.put(KEY_SPEC_BINARIES, binaries);
            props.put(KEY_SPEC_FILES, files);
        } catch (IOException e) {
            throw new PackagerProcessingException("ERROR", e);
        }
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);
    }

    @Override
    protected void fillPackagerProperties(Map<String, Object> props, Distribution distribution, ProcessingStep processingStep) throws PackagerProcessingException {
        props.put(KEY_SPEC_PACKAGE_NAME, packager.getPackageName());
        props.put(KEY_SPEC_RELEASE, packager.getRelease());
        props.put(KEY_SPEC_REQUIRES, packager.getRequires());
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName)
        throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = "app.spec".equals(fileName) ?
            outputDirectory.resolve(packager.getPackageName().concat(".spec")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
