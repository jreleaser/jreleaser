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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Tool;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.model.tool.spi.ToolProcessor;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.Version;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessInitException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
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

            Path prepareDirectory = (Path) props.get(Constants.KEY_PREPARE_DIRECTORY);
            Files.createDirectories(prepareDirectory);

            context.getLogger().debug("resolving templates for {}/{}", distributionName, getToolName());
            Map<String, Reader> templates = resolveAndMergeTemplates(context.getLogger(),
                distribution.getType().name(),
                getToolName(),
                context.getModel().getProject().isSnapshot(),
                context.getBasedir().resolve(getTool().getTemplateDirectory()));

            for (Map.Entry<String, Reader> entry : templates.entrySet()) {
                context.getLogger().debug("evaluating template {} for {}/{}", entry.getKey(), distributionName, getToolName());
                String content = applyTemplate(entry.getValue(), newProps);
                context.getLogger().debug("writing template {} for {}/{}", entry.getKey(), distributionName, getToolName());
                writeFile(context.getModel().getProject(), distribution, content, newProps, entry.getKey());
            }

            context.getLogger().debug("copying license files");
            Path outputDirectory = (Path) props.get(Constants.KEY_PREPARE_DIRECTORY);
            FileUtils.copyFiles(context.getLogger(),
                context.getBasedir(),
                outputDirectory, path -> path.getFileName().startsWith("LICENSE"));
        } catch (IllegalArgumentException | IOException e) {
            throw new ToolProcessingException(e);
        }

        return true;
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

            Path packageDirectory = (Path) props.get(Constants.KEY_PACKAGE_DIRECTORY);
            Files.createDirectories(packageDirectory);

            return doPackageDistribution(distribution, newProps);
        } catch (IllegalArgumentException | IOException e) {
            throw new ToolProcessingException(e);
        }
    }

    @Override
    public boolean uploadDistribution(Distribution distribution, Releaser releaser, Map<String, Object> props) throws ToolProcessingException {
        try {
            String distributionName = distribution.getName();
            context.getLogger().debug("creating props for {}/{}", distributionName, getToolName());
            Map<String, Object> newProps = fillProps(distribution, props);
            if (newProps.isEmpty()) {
                context.getLogger().warn("skipping {} distribution", distributionName);
                return false;
            }
            return doUploadDistribution(distribution, releaser, newProps);
        } catch (IllegalArgumentException e) {
            throw new ToolProcessingException(e);
        }
    }

    protected abstract boolean doPackageDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException;

    protected abstract boolean doUploadDistribution(Distribution distribution, Releaser releaser, Map<String, Object> props) throws ToolProcessingException;

    protected abstract void writeFile(Project project, Distribution distribution, String content, Map<String, Object> props, String fileName) throws ToolProcessingException;

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
        newProps.putAll(tool.getResolvedExtraProperties());
        if (isBlank(context.getModel().getRelease().getGitService().getReverseRepoHost())) {
            newProps.put(Constants.KEY_REVERSE_REPO_HOST,
                tool.getExtraProperties().get(Constants.KEY_REVERSE_REPO_HOST));
        }
        return newProps;
    }

    protected void fillDistributionProperties(Map<String, Object> props, Distribution distribution) {
        props.put(Constants.KEY_DISTRIBUTION_NAME, distribution.getName());
        props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, distribution.getExecutable());
        props.putAll(distribution.getJava().getResolvedExtraProperties());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID, distribution.getJava().getGroupId());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, distribution.getJava().getArtifactId());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, distribution.getJava().getVersion());
        Version jv = Version.of(distribution.getJava().getVersion());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, jv.getMajor());
        if (jv.hasMinor()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, jv.getMinor());
        if (jv.hasPatch()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, jv.getPatch());
        if (jv.hasTag()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, jv.getTag());
        if (jv.hasBuild()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, jv.getBuild());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS, distribution.getJava().getMainClass());
        props.put(Constants.KEY_DISTRIBUTION_TAGS_BY_SPACE, String.join(" ", distribution.getTags()));
        props.put(Constants.KEY_DISTRIBUTION_TAGS_BY_COMMA, String.join(",", distribution.getTags()));
        props.putAll(distribution.getResolvedExtraProperties());
    }

    protected abstract void fillToolProperties(Map<String, Object> context, Distribution distribution) throws ToolProcessingException;

    protected boolean executeCommand(List<String> cmd) throws ToolProcessingException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            int exitValue = new ProcessExecutor(cmd)
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
        } catch (Exception e) {
            if (e instanceof ToolProcessingException) {
                throw (ToolProcessingException) e;
            }
            throw new ToolProcessingException("Unexpected error", e);
        }
    }

    protected boolean executeCommandWithInput(List<String> cmd, InputStream in) throws ToolProcessingException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            int exitValue = new ProcessExecutor(cmd)
                .redirectOutput(out)
                .redirectError(err)
                .redirectInput(in)
                .execute()
                .getExitValue();

            info(out);
            error(err);

            if (exitValue == 0) return true;
            throw new ToolProcessingException("Command execution error. exitValue = " + exitValue);
        } catch (ProcessInitException e) {
            throw new ToolProcessingException("Unexpected error", e.getCause());
        } catch (Exception e) {
            if (e instanceof ToolProcessingException) {
                throw (ToolProcessingException) e;
            }
            throw new ToolProcessingException("Unexpected error", e);
        }
    }

    protected void copyPreparedFiles(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Path packageDirectory = (Path) props.get(Constants.KEY_PACKAGE_DIRECTORY);
        copyPreparedFiles(distribution, props, packageDirectory);
    }

    protected void copyPreparedFiles(Distribution distribution, Map<String, Object> props, Path outputDirectory) throws ToolProcessingException {
        Path prepareDirectory = (Path) props.get(Constants.KEY_PREPARE_DIRECTORY);
        try {
            if (!FileUtils.copyFilesRecursive(context.getLogger(), prepareDirectory, outputDirectory)) {
                throw new ToolProcessingException("Could not copy files from " +
                    context.getBasedir().relativize(prepareDirectory) + " to " +
                    context.getBasedir().relativize(outputDirectory));
            }
        } catch (IOException e) {
            throw new ToolProcessingException("Unexpected error when copying files from " +
                context.getBasedir().relativize(prepareDirectory) + " to " +
                context.getBasedir().relativize(outputDirectory), e);
        }
    }

    protected boolean verifyAndAddArtifacts(Map<String, Object> props,
                                            Distribution distribution) throws ToolProcessingException {
        Set<String> fileExtensions = tool.getSupportedExtensions();
        List<Artifact> artifacts = distribution.getArtifacts().stream()
            .filter(artifact -> fileExtensions.stream().anyMatch(ext -> artifact.getPath().endsWith(ext)))
            .filter(artifact -> tool.supportsPlatform(artifact.getPlatform()))
            .collect(Collectors.toList());

        if (artifacts.size() == 0) {
            // we can't proceed
            context.getLogger().warn("no suitable artifacts found in distribution {} to be packaged with {}",
                distribution.getName(), capitalize(tool.getName()));
            return false;
        }

        for (int i = 0; i < artifacts.size(); i++) {
            Artifact artifact = artifacts.get(i);
            String platform = isNotBlank(artifact.getPlatform()) ? capitalize(artifact.getPlatform()) : "";
            String artifactFileName = artifact.getEffectivePath(context).getFileName().toString();
            String artifactName = getFilename(artifactFileName);
            props.put("artifact" + platform + "Name", artifactName);
            props.put("artifact" + platform + "FileName", artifactFileName);
            props.put("artifact" + platform + "Hash", artifact.getHash());
            Map<String, Object> newProps = new LinkedHashMap<>(props);
            newProps.put(Constants.KEY_ARTIFACT_FILE_NAME, artifactFileName);
            String artifactUrl = applyTemplate(new StringReader(context.getModel().getRelease().getGitService().getDownloadUrlFormat()), newProps, "downloadUrl");
            props.put("artifact" + platform + "Url", artifactUrl);

            if (0 == i) {
                props.put(Constants.KEY_DISTRIBUTION_URL, artifactUrl);
                props.put(Constants.KEY_DISTRIBUTION_SHA_256, artifact.getHash());
                props.put(Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME, artifactFileName);
                props.put(Constants.KEY_DISTRIBUTION_ARTIFACT_NAME, artifactName);
                props.put(Constants.KEY_ARTIFACT_FILE_NAME, artifactFileName);
                props.put(Constants.KEY_ARTIFACT_NAME, artifactName);
            }
        }

        return true;
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
}
