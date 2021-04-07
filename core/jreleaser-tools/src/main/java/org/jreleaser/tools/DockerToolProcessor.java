/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.jreleaser.model.Docker;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.PlatformUtils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.MustacheUtils.applyTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class DockerToolProcessor extends AbstractToolProcessor<Docker> {
    public DockerToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        try {
            // copy files
            Path workingDirectory = prepareAssembly(distribution, props);

            int i = 0;
            for (String imageName : getTool().getImageNames()) {
                imageName = applyTemplate(new StringReader(imageName), props, "image" + (i++));

                // command line
                List<String> cmd = createCommand(props);
                if (!cmd.contains("-q") && !cmd.contains("--quiet")) {
                    cmd.add("-q");
                }
                cmd.add("-f");
                cmd.add(workingDirectory.resolve("Dockerfile").toAbsolutePath().toString());
                cmd.add("-t");
                cmd.add(imageName);
                cmd.add(workingDirectory.toAbsolutePath().toString());
                context.getLogger().debug(String.join(" ", cmd));

                context.getLogger().info(" - " + imageName);
                // execute
                executeCommand(cmd);
            }
        } catch (IOException e) {
            throw new ToolProcessingException(e);
        }

        return true;
    }

    private Path prepareAssembly(Distribution distribution, Map<String, Object> props) throws IOException, ToolProcessingException {
        Path workingDirectory = Files.createTempDirectory("jreleaser-docker");
        copyPreparedFiles(distribution, props, workingDirectory);
        Path assemblyDirectory = workingDirectory.resolve("assembly");
        Files.createDirectories(assemblyDirectory);

        Set<String> fileExtensions = tool.getSupportedExtensions();
        List<Artifact> artifacts = distribution.getArtifacts().stream()
            .filter(artifact -> fileExtensions.stream().anyMatch(ext -> artifact.getPath().endsWith(ext)))
            .collect(Collectors.toList());

        for (Artifact artifact : artifacts) {
            Path artifactPath = artifact.getResolvedPath(context);
            Files.copy(artifactPath, assemblyDirectory.resolve(artifactPath.getFileName()), REPLACE_EXISTING);
        }

        return workingDirectory;
    }

    private List<String> createCommand(Map<String, Object> props) {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker" + (PlatformUtils.isWindows() ? ".exe" : ""));
        cmd.add("-l");
        cmd.add("error");
        cmd.add("build");
        for (int i = 0; i < getTool().getBuildArgs().size(); i++) {
            String arg = getTool().getBuildArgs().get(i);
            if (arg.contains("{{")) {
                cmd.add(applyTemplate(new StringReader(arg), props, "arg" + i));
            } else {
                cmd.add(arg);
            }
        }
        return cmd;
    }

    @Override
    public boolean uploadDistribution(Distribution distribution, Releaser releaser, Map<String, Object> props) throws ToolProcessingException {
        context.getLogger().warn("Publication of docker images is not yet supported.");
        return false;
    }

    @Override
    protected boolean doUploadDistribution(Distribution distribution, Releaser releaser, Map<String, Object> props) throws ToolProcessingException {
        return false;
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        props.put(Constants.KEY_DOCKER_BASE_IMAGE,
            applyTemplate(new StringReader(getTool().getBaseImage()), props, "baseImage"));

        List<String> labels = new ArrayList<>();
        getTool().getLabels().forEach((label, value) -> labels.add("!!\"" + label + "\"=\"" +
            applyTemplate(new StringReader(value), props, label) + "\"!!"));
        props.put(Constants.KEY_DOCKER_LABELS, labels);
    }

    @Override
    protected void writeFile(Project project, Distribution distribution, String content, Map<String, Object> props, String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = (Path) props.get(Constants.KEY_PREPARE_DIRECTORY);
        Path outputFile = outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
