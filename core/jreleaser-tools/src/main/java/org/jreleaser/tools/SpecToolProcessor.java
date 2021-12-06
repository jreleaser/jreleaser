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
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Spec;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.model.Spec.SKIP_SPEC;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
public class SpecToolProcessor extends AbstractRepositoryToolProcessor<Spec> {
    public SpecToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        setupJavaBinary(distribution, props);
        super.doPrepareDistribution(distribution, props);
    }

    private void setupJavaBinary(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Artifact artifact = (Artifact) props.get(Constants.KEY_DISTRIBUTION_ARTIFACT);
        Path artifactPath = artifact.getResolvedPath();
        String artifactName = getFilename(artifactPath.getFileName().toString(), tool.getSupportedExtensions());

        try {
            List<String> entries = FileUtils.inspectArchive(artifactPath);

            Set<String> directories = new LinkedHashSet<>();
            List<String> files = new ArrayList<>();

            entries.stream()
                // skip Windows
                .filter(e -> !e.endsWith(distribution.getExecutableExtension()))
                // skip executable
                .filter(e -> !e.endsWith("bin/" + distribution.getExecutable()))
                // skip directories
                .filter(e -> !e.endsWith("/"))
                // remove root from name
                .map(e -> e.substring(artifactName.length() + 1))
                .sorted()
                .forEach(entry -> {
                    String[] parts = entry.split("/");
                    if (parts.length > 1) directories.add(parts[0]);
                    files.add(entry.replace(context.getModel().getProject().getResolvedVersion(),
                        "%{version}")
                        .replace(context.getModel().getProject().getEffectiveVersion(),
                            "%{version}"));
                });

            props.put(Constants.KEY_SPEC_DIRECTORIES, directories);
            props.put(Constants.KEY_SPEC_FILES, files);
        } catch (IOException e) {
            throw new ToolProcessingException("ERROR", e);
        }
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        props.put(Constants.KEY_SPEC_RELEASE, tool.getRelease());
        props.put(Constants.KEY_SPEC_REQUIRES, tool.getRequires());
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

        Path outputFile = "app.spec".equals(fileName) ?
            outputDirectory.resolve(distribution.getName().concat(".spec")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    @Override
    protected boolean isSkipped(Artifact artifact) {
        return isTrue(artifact.getExtraProperties().get(SKIP_SPEC));
    }
}
