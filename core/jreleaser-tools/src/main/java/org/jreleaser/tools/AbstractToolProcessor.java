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
import org.jreleaser.model.Tool;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.model.tool.spi.ToolProcessor;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessInitException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.templates.TemplateUtils.resolveAndMergeTemplates;
import static org.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;
import static org.jreleaser.util.FileUtils.grantFullAccess;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractToolProcessor<T extends Tool> implements ToolProcessor<T> {
    protected final JReleaserContext context;
    protected T tool;

    protected AbstractToolProcessor(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public T getTool() {
        return tool;
    }

    @Override
    public void setTool(T tool) {
        this.tool = tool;
    }

    @Override
    public String getToolName() {
        return tool.getName();
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return true;
    }

    @Override
    public boolean prepareDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        try {
            String distributionName = distribution.getName();
            context.getLogger().debug("creating props for {}/{}", distributionName, getToolName());
            Map<String, Object> newProps = fillProps(distribution, props);
            if (newProps.isEmpty()) {
                context.getLogger().warn("Skipping {} distribution", distributionName);
                return false;
            }

            doPrepareDistribution(distribution, newProps, distributionName,
                getPrepareDirectory(props), getTool().getTemplateDirectory(), getToolName());
        } catch (IllegalArgumentException | IOException e) {
            throw new ToolProcessingException(e);
        }

        return true;
    }

    protected void doPrepareDistribution(Distribution distribution,
                                         Map<String, Object> props,
                                         String distributionName,
                                         Path prepareDirectory,
                                         String templateDirectory,
                                         String toolName) throws IOException, ToolProcessingException {
        // cleanup from previous session
        FileUtils.deleteFiles(prepareDirectory);
        Files.createDirectories(prepareDirectory);

        context.getLogger().debug("resolving templates for {}/{}", distributionName, toolName);
        Map<String, Reader> templates = resolveAndMergeTemplates(context.getLogger(),
            distribution.getType().name(),
            // leave this one be!
            getToolName(),
            context.getModel().getProject().isSnapshot(),
            context.getBasedir().resolve(templateDirectory));

        for (Map.Entry<String, Reader> entry : templates.entrySet()) {
            context.getLogger().debug("evaluating template {} for {}/{}", entry.getKey(), distributionName, toolName);
            String content = applyTemplate(entry.getValue(), props);
            context.getLogger().debug("writing template {} for {}/{}", entry.getKey(), distributionName, toolName);
            writeFile(context.getModel().getProject(), distribution, content, props, prepareDirectory, entry.getKey());
        }

        context.getLogger().debug("copying license files");
        FileUtils.copyFiles(context.getLogger(),
            context.getBasedir(),
            prepareDirectory, path -> path.getFileName().startsWith("LICENSE"));
    }

    @Override
    public boolean packageDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        try {
            String distributionName = distribution.getName();
            context.getLogger().debug("creating props for {}/{}", distributionName, getToolName());
            Map<String, Object> newProps = fillProps(distribution, props);
            if (newProps.isEmpty()) {
                context.getLogger().warn("skipping {} distribution", distributionName);
                return false;
            }

            return doPackageDistribution(distribution, newProps, getPackageDirectory(props));
        } catch (IllegalArgumentException e) {
            throw new ToolProcessingException(e);
        }
    }

    @Override
    public boolean publishDistribution(Distribution distribution, Releaser releaser, Map<String, Object> props) throws ToolProcessingException {
        try {
            String distributionName = distribution.getName();
            context.getLogger().debug("creating props for {}/{}", distributionName, getToolName());
            Map<String, Object> newProps = fillProps(distribution, props);
            if (newProps.isEmpty()) {
                context.getLogger().warn("skipping {} distribution", distributionName);
                return false;
            }

            return doPublishDistribution(distribution, releaser, newProps);
        } catch (IllegalArgumentException e) {
            throw new ToolProcessingException(e);
        }
    }

    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        try {
            // cleanup from previous session
            FileUtils.deleteFiles(packageDirectory);
            Files.createDirectories(packageDirectory);
            return true;
        } catch (IOException e) {
            throw new ToolProcessingException(e);
        }
    }

    protected abstract boolean doPublishDistribution(Distribution distribution, Releaser releaser, Map<String, Object> props) throws ToolProcessingException;

    protected abstract void writeFile(Project project, Distribution distribution, String content, Map<String, Object> props, Path outputDirectory, String fileName) throws ToolProcessingException;

    protected void writeFile(String content, Path outputFile) throws ToolProcessingException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (IOException e) {
            throw new ToolProcessingException("Unexpected error when writing to " + outputFile.toAbsolutePath(), e);
        }
    }

    protected Map<String, Object> fillProps(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Map<String, Object> newProps = new LinkedHashMap<>(props);
        context.getLogger().debug("filling distribution properties into props");
        fillDistributionProperties(newProps, distribution);
        context.getLogger().debug("filling git properties into props");
        context.getModel().getRelease().getGitService().fillProps(newProps, context.getModel());
        context.getLogger().debug("filling artifact properties into props");
        if (!verifyAndAddArtifacts(newProps, distribution)) {
            // we can't continue with this tool
            return Collections.emptyMap();
        }
        context.getLogger().debug("filling tool properties into props");
        fillToolProperties(newProps, distribution);
        applyTemplates(newProps, tool.getResolvedExtraProperties());
        if (isBlank(context.getModel().getRelease().getGitService().getReverseRepoHost())) {
            newProps.put(Constants.KEY_REVERSE_REPO_HOST,
                tool.getExtraProperties().get(Constants.KEY_REVERSE_REPO_HOST));
        }
        return newProps;
    }

    protected void fillDistributionProperties(Map<String, Object> props, Distribution distribution) {
        props.putAll(distribution.props());
    }

    protected abstract void fillToolProperties(Map<String, Object> context, Distribution distribution) throws ToolProcessingException;

    protected boolean executeCommand(ProcessExecutor processExecutor) throws ToolProcessingException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            int exitValue = processExecutor
                .redirectOutput(out)
                .redirectError(err)
                .execute()
                .getExitValue();

            info(out);
            error(err);

            if (exitValue == 0) return true;
            throw new ToolProcessingException("Command execution error. exitValue = " + exitValue);
        } catch (ProcessInitException e) {
            throw new ToolProcessingException("Unexpected error", e.getCause());
        } catch (ToolProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new ToolProcessingException("Unexpected error", e);
        }
    }

    protected boolean executeCommand(Path directory, List<String> cmd) throws ToolProcessingException {
        return executeCommand(new ProcessExecutor(cmd)
            .directory(directory.toFile()));
    }

    protected boolean executeCommand(List<String> cmd) throws ToolProcessingException {
        return executeCommand(new ProcessExecutor(cmd));
    }

    protected boolean executeCommandWithInput(List<String> cmd, InputStream in) throws ToolProcessingException {
        return executeCommand(new ProcessExecutor(cmd)
            .redirectInput(in));
    }

    protected void copyPreparedFiles(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Path packageDirectory = getPackageDirectory(props);
        copyPreparedFiles(distribution, props, packageDirectory);
    }

    protected void copyPreparedFiles(Distribution distribution, Map<String, Object> props, Path outputDirectory) throws ToolProcessingException {
        Path prepareDirectory = getPrepareDirectory(props);
        try {
            if (!FileUtils.copyFilesRecursive(context.getLogger(), prepareDirectory, outputDirectory)) {
                throw new ToolProcessingException("Could not copy files from " +
                    context.relativizeToBasedir(prepareDirectory) + " to " +
                    context.relativizeToBasedir(outputDirectory));
            }
        } catch (IOException e) {
            throw new ToolProcessingException("Unexpected error when copying files from " +
                context.relativizeToBasedir(prepareDirectory) + " to " +
                context.relativizeToBasedir(outputDirectory), e);
        }
    }

    protected boolean verifyAndAddArtifacts(Map<String, Object> props,
                                            Distribution distribution) throws ToolProcessingException {
        return verifyAndAddArtifacts(props, distribution, collectArtifacts(distribution));
    }

    protected boolean verifyAndAddArtifacts(Map<String, Object> props,
                                            Distribution distribution,
                                            List<Artifact> artifacts) throws ToolProcessingException {
        if (artifacts.size() == 0) {
            // we can't proceed
            context.getLogger().warn("no suitable artifacts found in distribution {} to be packaged with {}",
                distribution.getName(), capitalize(tool.getName()));
            return false;
        }

        for (int i = 0; i < artifacts.size(); i++) {
            Artifact artifact = artifacts.get(i);
            String platform = isNotBlank(artifact.getPlatform()) ? capitalize(artifact.getPlatform()) : "";
            // add extra properties without clobbering existing keys
            Map<String, Object> artifactProps = artifact.getResolvedExtraProperties("artifact" + platform);
            artifactProps.keySet().stream()
                .filter(k -> !props.containsKey(k))
                .forEach(k -> props.put(k, artifactProps.get(k)));
            String artifactFileName = artifact.getEffectivePath(context).getFileName().toString();
            String artifactName = getFilename(artifactFileName);
            props.put("artifact" + platform + "Name", artifactName);
            props.put("artifact" + platform + "FileName", artifactFileName);
            for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                props.put("artifact" + platform + "Checksum" + capitalize(algorithm.formatted()), artifact.getHash(algorithm));
            }
            Map<String, Object> newProps = new LinkedHashMap<>(props);
            newProps.put(Constants.KEY_ARTIFACT_FILE_NAME, artifactFileName);
            String artifactUrl = applyTemplate(context.getModel().getRelease().getGitService().getDownloadUrl(), newProps, "downloadUrl");
            props.put("artifact" + platform + "Url", artifactUrl);

            if (0 == i) {
                props.put(Constants.KEY_DISTRIBUTION_URL, artifactUrl);
                props.put(Constants.KEY_DISTRIBUTION_SHA_256, artifact.getHash(Algorithm.SHA_256));
                for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                    props.put("distributionChecksum"  + capitalize(algorithm.formatted()), artifact.getHash(algorithm));
                }
                props.put(Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME, artifactFileName);
                props.put(Constants.KEY_DISTRIBUTION_ARTIFACT_NAME, artifactName);
                props.put(Constants.KEY_ARTIFACT_FILE_NAME, artifactFileName);
                props.put(Constants.KEY_ARTIFACT_NAME, artifactName);
                // add extra properties without clobbering existing keys
                Map<String, Object> aprops = artifact.getResolvedExtraProperties();
                applyTemplates(aprops, aprops);
                aprops.keySet().stream()
                    .filter(k -> !props.containsKey(k))
                    .forEach(k -> props.put(k, aprops.get(k)));
            }
        }

        return true;
    }

    protected List<Artifact> collectArtifacts(Distribution distribution) {
        Set<String> fileExtensions = tool.getSupportedExtensions();
        return distribution.getArtifacts().stream()
            .filter(artifact -> fileExtensions.stream().anyMatch(ext -> artifact.getPath().endsWith(ext)))
            .filter(artifact -> tool.supportsPlatform(artifact.getPlatform()))
            .collect(Collectors.toList());
    }

    protected void info(ByteArrayOutputStream out) {
        log(out, context.getLogger()::info);
    }

    protected void error(ByteArrayOutputStream err) {
        log(err, context.getLogger()::error);
    }

    private void log(ByteArrayOutputStream stream, Consumer<? super String> consumer) {
        String str = stream.toString();
        if (isBlank(str)) return;

        Arrays.stream(str.split(System.lineSeparator()))
            .forEach(consumer);
    }

    protected Path getPrepareDirectory(Map<String, Object> props) {
        return (Path) props.get(Constants.KEY_DISTRIBUTION_PREPARE_DIRECTORY);
    }

    protected Path getPackageDirectory(Map<String, Object> props) {
        return (Path) props.get(Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
    }
}
