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
package org.jreleaser.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.jreleaser.ant.tasks.internal.JReleaserLoggerAdapter;
import org.jreleaser.engine.context.ModelAutoConfigurer;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.util.Env;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.StringUtils;
import org.jreleaser.workflow.Workflows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.FileUtils.resolveOutputDirectory;
import static org.jreleaser.util.IoUtils.newPrintWriter;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class JReleaserAutoConfigReleaseTask extends Task {
    private final List<String> authors = new ArrayList<>();
    private final List<String> selectPlatforms = new ArrayList<>();
    private final List<String> rejectPlatforms = new ArrayList<>();
    private final List<String> globs = new ArrayList<>();
    private final List<String> updateSections = new ArrayList<>();
    private Path outputDir;
    private boolean selectCurrentPlatform;
    private JReleaserLogger logger;
    private Path actualBasedir;
    private File basedir;
    private Boolean dryrun;
    private Boolean gitRootSearch;
    private Boolean strict;
    private String projectName;
    private String projectVersion;
    private String projectVersionPattern;
    private String projectSnapshotPattern;
    private String projectSnapshotLabel;
    private boolean projectSnapshotFullChangelog;
    private String projectCopyright;
    private String projectDescription;
    private String projectInceptionYear;
    private String projectStereotype;
    private String tagName;
    private String previousTagName;
    private String releaseName;
    private String branch;
    private String milestoneName;
    private Boolean prerelease;
    private String prereleasePattern;
    private Boolean draft;
    private boolean overwrite;
    private boolean update;
    private boolean skipTag;
    private boolean skipRelease;
    private boolean skipChecksums;
    private String changelog;
    private boolean changelogFormatted;
    private String username;
    private String commitAuthorName;
    private String commitAuthorEmail;
    private boolean signing;
    private boolean armored;
    private FileSet fileSet;

    public void setDryrun(Boolean dryrun) {
        this.dryrun = dryrun;
    }

    public void setGitRootSearch(Boolean gitRootSearch) {
        this.gitRootSearch = gitRootSearch;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }

    public void setOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public void setProjectVersionPattern(String projectVersionPattern) {
        this.projectVersionPattern = projectVersionPattern;
    }

    public void setProjectSnapshotPattern(String projectSnapshotPattern) {
        this.projectSnapshotPattern = projectSnapshotPattern;
    }

    public void setProjectSnapshotLabel(String projectSnapshotLabel) {
        this.projectSnapshotLabel = projectSnapshotLabel;
    }

    public void setProjectSnapshotFullChangelog(boolean projectSnapshotFullChangelog) {
        this.projectSnapshotFullChangelog = projectSnapshotFullChangelog;
    }

    public void setProjectCopyright(String projectCopyright) {
        this.projectCopyright = projectCopyright;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public void setProjectInceptionYear(String projectInceptionYear) {
        this.projectInceptionYear = projectInceptionYear;
    }

    public void setProjectStereotype(String projectStereotype) {
        this.projectStereotype = projectStereotype;
    }

    public void setAuthors(List<String> authors) {
        if (null != authors) {
            this.authors.addAll(authors);
        }
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public void setPreviousTagName(String previousTagName) {
        this.previousTagName = previousTagName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public void setPrerelease(Boolean prerelease) {
        this.prerelease = prerelease;
    }

    public void setPrereleasePattern(String prereleasePattern) {
        this.prereleasePattern = prereleasePattern;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setUpdateSections(List<String> updateSections) {
        if (null != updateSections) {
            this.updateSections.addAll(updateSections);
        }
    }

    public void setSkipTag(boolean skipTag) {
        this.skipTag = skipTag;
    }

    public void setSkipRelease(boolean skipRelease) {
        this.skipRelease = skipRelease;
    }

    public void setSkipChecksums(boolean skipChecksums) {
        this.skipChecksums = skipChecksums;
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

    public void setGlobs(List<String> globs) {
        if (null != globs) {
            this.globs.addAll(globs);
        }
    }

    public void setSelectCurrentPlatform(boolean selectCurrentPlatform) {
        this.selectCurrentPlatform = selectCurrentPlatform;
    }

    public void setSelectPlatforms(List<String> selectPlatforms) {
        if (null != selectPlatforms) {
            this.selectPlatforms.addAll(selectPlatforms);
        }
    }

    public void setRejectPlatforms(List<String> rejectPlatforms) {
        if (null != rejectPlatforms) {
            this.rejectPlatforms.addAll(rejectPlatforms);
        }
    }

    @Override
    public void execute() throws BuildException {
        Banner.display(newPrintWriter(System.err));

        basedir();
        initLogger();

        JReleaserContext context = ModelAutoConfigurer.builder()
            .logger(logger)
            .basedir(actualBasedir)
            .outputDirectory(getOutputDirectory())
            .dryrun(dryrun)
            .gitRootSearch(gitRootSearch)
            .strict(strict)
            .projectName(projectName)
            .projectVersion(projectVersion)
            .projectVersionPattern(projectVersionPattern)
            .projectSnapshotPattern(projectSnapshotPattern)
            .projectSnapshotLabel(projectSnapshotLabel)
            .projectSnapshotFullChangelog(projectSnapshotFullChangelog)
            .projectCopyright(projectCopyright)
            .projectDescription(projectDescription)
            .projectInceptionYear(projectInceptionYear)
            .projectStereotype(projectStereotype)
            .authors(authors)
            .tagName(tagName)
            .previousTagName(previousTagName)
            .releaseName(releaseName)
            .branch(branch)
            .milestoneName(milestoneName)
            .prerelease(prerelease)
            .prereleasePattern(prereleasePattern)
            .draft(draft)
            .overwrite(overwrite)
            .update(update)
            .updateSections(collectUpdateSections())
            .skipTag(skipTag)
            .skipRelease(skipRelease)
            .skipChecksums(skipChecksums)
            .changelog(changelog)
            .changelogFormatted(changelogFormatted)
            .username(username)
            .commitAuthorName(commitAuthorName)
            .commitAuthorEmail(commitAuthorEmail)
            .signing(signing)
            .armored(armored)
            .files(fileSet.stream().map(Resource::getName).collect(toList()))
            .globs(globs)
            .selectedPlatforms(collectSelectedPlatforms())
            .rejectedPlatforms(collectRejectedPlatforms())
            .autoConfigure();

        Workflows.release(context).execute();
    }

    private Set<UpdateSection> collectUpdateSections() {
        Set<UpdateSection> set = new LinkedHashSet<>();
        for (String updateSection : updateSections) {
            set.add(UpdateSection.of(updateSection.trim()));
        }
        return set;
    }

    private void basedir() {
        String resolvedBasedir = Env.resolve(org.jreleaser.model.api.JReleaserContext.BASEDIR, null != basedir ? basedir.getPath() : "");
        actualBasedir = (isNotBlank(resolvedBasedir) ? Paths.get(resolvedBasedir) : Paths.get(".")).normalize();
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
            return newPrintWriter(new FileOutputStream(
                getOutputDirectory().resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize trace file", e);
        }
    }

    private Path getOutputDirectory() {
        return resolveOutputDirectory(actualBasedir, outputDir, "build");
    }

    protected List<String> collectSelectedPlatforms() {
        boolean resolvedSelectCurrentPlatform = resolveBoolean(org.jreleaser.model.api.JReleaserContext.SELECT_CURRENT_PLATFORM, selectCurrentPlatform);
        if (resolvedSelectCurrentPlatform) return Collections.singletonList(PlatformUtils.getCurrentFull());
        return resolveCollection(org.jreleaser.model.api.JReleaserContext.SELECT_PLATFORMS, selectPlatforms);
    }

    protected List<String> collectRejectedPlatforms() {
        return resolveCollection(org.jreleaser.model.api.JReleaserContext.REJECT_PLATFORMS, rejectPlatforms);
    }

    protected boolean resolveBoolean(String key, Boolean value) {
        if (null != value) return value;
        String resolvedValue = Env.resolve(key, "");
        return isNotBlank(resolvedValue) && Boolean.parseBoolean(resolvedValue);
    }

    protected List<String> resolveCollection(String key, List<String> values) {
        if (!values.isEmpty()) return values;
        String resolvedValue = Env.resolve(key, "");
        if (isBlank(resolvedValue)) return Collections.emptyList();
        return Arrays.stream(resolvedValue.trim().split(","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .collect(toList());
    }
}
