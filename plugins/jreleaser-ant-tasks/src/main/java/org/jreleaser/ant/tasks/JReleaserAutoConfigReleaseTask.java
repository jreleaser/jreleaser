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
package org.jreleaser.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.jreleaser.ant.tasks.internal.JReleaserLoggerAdapter;
import org.jreleaser.engine.context.ModelAutoConfigurer;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.JReleaserLogger;
import org.jreleaser.workflow.Workflows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class JReleaserAutoConfigReleaseTask extends Task {
    private JReleaserLogger logger;
    private Path actualBasedir;
    private File basedir;
    private boolean dryrun;
    private String projectName;
    private String projectVersion;
    private String tagName;
    private String releaseName;
    private String milestoneName;
    private boolean prerelease;
    private boolean draft;
    private boolean overwrite;
    private boolean update;
    private boolean skipTag;
    private String changelog;
    private boolean changelogFormatted;
    private String username;
    private String commitAuthorName;
    private String commitAuthorEmail;
    private boolean signing;
    private boolean armored;
    private FileSet fileSet;

    public void setDryrun(boolean dryrun) {
        this.dryrun = dryrun;
    }

    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public void setPrerelease(boolean prerelease) {
        this.prerelease = prerelease;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setSkipTag(boolean skipTag) {
        this.skipTag = skipTag;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public void setChangelogFormatted(boolean changelogFormatted) {
        this.changelogFormatted = changelogFormatted;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCommitAuthorName(String commitAuthorName) {
        this.commitAuthorName = commitAuthorName;
    }

    public void setCommitAuthorEmail(String commitAuthorEmail) {
        this.commitAuthorEmail = commitAuthorEmail;
    }

    public void setSigning(boolean signing) {
        this.signing = signing;
    }

    public void setArmored(boolean armored) {
        this.armored = armored;
    }

    public void setFileSet(FileSet fileSet) {
        this.fileSet = fileSet;
    }

    @Override
    public void execute() throws BuildException {
        Banner.display(new PrintWriter(System.out, true));

        basedir();
        initLogger();

        JReleaserContext context = ModelAutoConfigurer.builder()
            .logger(logger)
            .basedir(actualBasedir)
            .outputDirectory(getOutputDirectory())
            .dryrun(dryrun)
            .projectName(projectName)
            .projectVersion(projectVersion)
            .tagName(tagName)
            .releaseName(releaseName)
            .milestoneName(milestoneName)
            .prerelease(prerelease)
            .draft(draft)
            .overwrite(overwrite)
            .update(update)
            .skipTag(skipTag)
            .changelog(changelog)
            .changelogFormatted(changelogFormatted)
            .username(username)
            .commitAuthorName(commitAuthorName)
            .commitAuthorEmail(commitAuthorEmail)
            .signing(signing)
            .armored(armored)
            .files(fileSet.stream().map(Resource::getName).collect(toList()))
            .autoConfigure();

        Workflows.release(context).execute();
    }

    private void basedir() {
        actualBasedir = null != basedir ? basedir.toPath() : Paths.get(".").normalize();
        if (!Files.exists(actualBasedir)) {
            throw new IllegalStateException("Missing required option: 'basedir'");
        }
    }

    private JReleaserLogger initLogger() {
        if (null == logger) {
            logger = new JReleaserLoggerAdapter(createTracer(), getProject());
        }
        return logger;
    }

    private PrintWriter createTracer() {
        try {
            Files.createDirectories(getOutputDirectory());
            return new PrintWriter(new FileOutputStream(
                getOutputDirectory().resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize trace file", e);
        }
    }

    private Path getOutputDirectory() {
        return actualBasedir.resolve("out").resolve("jreleaser");
    }
}
