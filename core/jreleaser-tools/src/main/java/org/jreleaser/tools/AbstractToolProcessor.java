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
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Project;
import org.jreleaser.model.Release;
import org.jreleaser.model.Tool;
import org.jreleaser.util.Constants;
import org.jreleaser.util.Logger;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;
import static org.jreleaser.util.FileUtils.grantFullAccess;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractToolProcessor<T extends Tool> implements ToolProcessor<T> {
    private final Logger logger;
    private final JReleaserModel model;
    private final T tool;

    protected AbstractToolProcessor(Logger logger, JReleaserModel model, T tool) {
        this.logger = logger;
        this.model = model;
        this.tool = tool;
    }

    @Override
    public T getTool() {
        return tool;
    }

    @Override
    public String getToolName() {
        return tool.getToolName();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public JReleaserModel getModel() {
        return model;
    }

    @Override
    public boolean prepareDistribution(Distribution distribution, Map<String, Object> context) throws ToolProcessingException {
        Tool tool = distribution.getTool(getToolName());

        try {
            String distributionName = distribution.getName();
            getLogger().debug("Creating context for {}/{}", distributionName, getToolName());
            Map<String, Object> newContext = fillContext(distribution, context);
            if (newContext.isEmpty()) {
                logger.warn("Skipping {} tool for {} distribution", getToolName(), distributionName);
                return false;
            }
            getLogger().debug("Resolving templates for {}/{}", distributionName, getToolName());
            Map<String, Reader> templates = resolveAndMergeTemplates(logger, distribution.getType(), getToolName(), tool.getTemplateDirectory());
            for (Map.Entry<String, Reader> entry : templates.entrySet()) {
                getLogger().debug("Evaluating template {} for {}/{}", entry.getKey(), distributionName, getToolName());
                String content = applyTemplate(entry.getValue(), newContext);
                getLogger().debug("Writing template {} for {}/{}", entry.getKey(), distributionName, getToolName());
                writeFile(model.getProject(), distribution, content, context, entry.getKey());
            }
        } catch (IllegalArgumentException e) {
            throw new ToolProcessingException(e);
        }

        return true;
    }

    @Override
    public boolean packageDistribution(Distribution distribution, Map<String, Object> context) throws ToolProcessingException {
        try {
            String distributionName = distribution.getName();
            getLogger().debug("Creating context for {}/{}", distributionName, getToolName());
            Map<String, Object> newContext = fillContext(distribution, context);
            if (newContext.isEmpty()) {
                logger.warn("Skipping {} tool for {} distribution", getToolName(), distributionName);
                return false;
            }
            return doPackageDistribution(distribution, newContext);
        } catch (IllegalArgumentException e) {
            throw new ToolProcessingException(e);
        }
    }

    protected abstract boolean doPackageDistribution(Distribution distribution, Map<String, Object> context) throws ToolProcessingException;

    protected abstract void writeFile(Project project, Distribution distribution, String content, Map<String, Object> context, String fileName) throws ToolProcessingException;

    protected void writeFile(String content, Path outputFile) throws ToolProcessingException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (IOException e) {
            throw new ToolProcessingException("Unexpected error when writing to " + outputFile.toAbsolutePath(), e);
        }
    }

    protected Map<String, Object> fillContext(Distribution distribution, Map<String, Object> context) throws ToolProcessingException {
        Map<String, Object> newContext = new LinkedHashMap<>(context);
        getLogger().debug("Filling project properties into context");
        fillProjectProperties(newContext, model.getProject());
        getLogger().debug("Filling release properties into context");
        fillReleaseProperties(newContext, model.getRelease());
        getLogger().debug("Filling distribution properties into context");
        fillDistributionProperties(newContext, distribution, model.getRelease());
        getLogger().debug("Filling artifact properties into context");
        if (!verifyAndAddArtifacts(newContext, distribution)) {
            // we can't continue with this tool
            return Collections.emptyMap();
        }
        getLogger().debug("Filling tool properties into context");
        fillToolProperties(newContext, distribution);
        newContext.putAll(tool.getExtraProperties());
        return newContext;
    }

    protected void fillProjectProperties(Map<String, Object> context, Project project) {
        context.put(Constants.KEY_PROJECT_NAME, project.getName());
        context.put(Constants.KEY_PROJECT_NAME_CAPITALIZED, getClassNameForLowerCaseHyphenSeparatedName(project.getName()));
        context.put(Constants.KEY_PROJECT_VERSION, project.getVersion());
        context.put(Constants.KEY_PROJECT_DESCRIPTION, project.getDescription());
        context.put(Constants.KEY_PROJECT_LONG_DESCRIPTION, project.getLongDescription());
        context.put(Constants.KEY_PROJECT_WEBSITE, project.getWebsite());
        context.put(Constants.KEY_PROJECT_LICENSE, project.getLicense());
        context.put(Constants.KEY_JAVA_VERSION, project.getJavaVersion());
        context.put(Constants.KEY_PROJECT_AUTHORS_BY_SPACE, String.join(" ", project.getAuthors()));
        context.put(Constants.KEY_PROJECT_AUTHORS_BY_COMMA, String.join(",", project.getAuthors()));
        context.put(Constants.KEY_PROJECT_TAGS_BY_SPACE, String.join(" ", project.getTags()));
        context.put(Constants.KEY_PROJECT_TAGS_BY_COMMA, String.join(",", project.getTags()));
        context.putAll(project.getExtraProperties());
    }

    protected void fillReleaseProperties(Map<String, Object> context, Release release) {
        context.put(Constants.KEY_REPO_HOST, release.getGitService().getRepoHost());
        context.put(Constants.KEY_REPO_OWNER, release.getGitService().getRepoOwner());
        context.put(Constants.KEY_REPO_NAME, release.getGitService().getRepoName());
    }

    protected void fillDistributionProperties(Map<String, Object> context, Distribution distribution, Release release) {
        context.put(Constants.KEY_DISTRIBUTION_NAME, distribution.getName());
        context.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, distribution.getExecutable());
        context.put(Constants.KEY_DISTRIBUTION_TAGS_BY_SPACE, String.join(" ", distribution.getTags()));
        context.put(Constants.KEY_DISTRIBUTION_TAGS_BY_COMMA, String.join(",", distribution.getTags()));
        context.put(Constants.KEY_DISTRIBUTION_RELEASE_NOTES, applyTemplate(new StringReader(release.getGitService().getReleaseNotesUrlFormat()), context));
        context.put(Constants.KEY_DISTRIBUTION_ISSUE_TRACKER, applyTemplate(new StringReader(release.getGitService().getIssueTrackerUrlFormat()), context));
        context.put(Constants.KEY_DISTRIBUTION_LATEST_RELEASE, applyTemplate(new StringReader(release.getGitService().getLatestReleaseUrlFormat()), context));
        context.putAll(distribution.getExtraProperties());
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
        } catch (Exception e) {
            if (e instanceof ToolProcessingException) {
                throw (ToolProcessingException) e;
            }
            throw new ToolProcessingException("Unexpected error", e);
        }
    }

    private boolean verifyAndAddArtifacts(Map<String, Object> context,
                                          Distribution distribution) throws ToolProcessingException {
        Set<String> fileExtensions = resolveByExtensionsFor(distribution.getType());
        List<Artifact> artifacts = distribution.getArtifacts().stream()
            .filter(artifact -> fileExtensions.stream().anyMatch(ext -> artifact.getPath().endsWith(ext)))
            .collect(Collectors.toList());

        if (artifacts.size() == 0) {
            // we can't proceed
            logger.warn("No suitable artifacts found in distribution {} to be packaged with {}",
                distribution.getName(), capitalize(tool.getToolName()));
            return false;
        }

        for (int i = 0; i < artifacts.size(); i++) {
            Artifact artifact = artifacts.get(i);
            String classifier = isNotBlank(artifact.getOsClassifier()) ? capitalize(artifact.getOsClassifier()) : "";
            String artifactFileName = Paths.get(artifact.getPath()).getFileName().toString();
            context.put("artifact" + classifier + "JavaVersion", artifact.getJavaVersion());
            context.put("artifact" + classifier + "FileName", artifactFileName);
            context.put("artifact" + classifier + "Hash", artifact.getHash());
            Map<String, Object> newContext = new LinkedHashMap<>(context);
            newContext.put("artifactFileName", artifactFileName);
            String artifactUrl = applyTemplate(new StringReader(model.getRelease().getGitService().getDownloadUrlFormat()), newContext, "downloadUrl");
            context.put("artifact" + classifier + "Url", artifactUrl);

            if (0 == i) {
                context.put(Constants.KEY_DISTRIBUTION_URL, artifactUrl);
                context.put(Constants.KEY_DISTRIBUTION_SHA_256, artifact.getHash());
                context.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, artifact.getJavaVersion());
                context.put(Constants.KEY_DISTRIBUTION_FILE_NAME, artifactFileName);
            }
        }

        return true;
    }

    protected abstract Set<String> resolveByExtensionsFor(Distribution.DistributionType type);

    protected void info(ByteArrayOutputStream out) {
        log(out, logger::info);
    }

    protected void error(ByteArrayOutputStream err) {
        log(err, logger::error);
    }

    private void log(ByteArrayOutputStream stream, Consumer<? super String> consumer) {
        String str = stream.toString();
        if (isBlank(str)) return;

        Arrays.stream(str.split(System.lineSeparator()))
            .forEach(consumer);
    }
}
