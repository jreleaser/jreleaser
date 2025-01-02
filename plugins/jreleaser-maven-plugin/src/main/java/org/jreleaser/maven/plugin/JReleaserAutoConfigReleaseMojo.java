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
package org.jreleaser.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jreleaser.engine.context.ModelAutoConfigurer;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.maven.plugin.internal.JReleaserLoggerAdapter;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.IoUtils.newPrintWriter;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * Create or update a release with auto-config enabled.
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@Mojo(threadSafe = true, name = "auto-config-release")
public class JReleaserAutoConfigReleaseMojo extends AbstractMojo {
    /**
     * Calculate full changelog since last non-snapshot release.
     */
    @Parameter(property = "jreleaser.project.snapshot.full.changelog")
    boolean projectSnapshotFullChangelog;
    /**
     * The project whose model will be checked.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(property = "jreleaser.output.directory", defaultValue = "${project.build.directory}/jreleaser")
    private File outputDirectory;
    /**
     * Skips remote operations.
     */
    @Parameter(property = "jreleaser.dry.run")
    private Boolean dryrun;
    /**
     * Searches for the Git root.
     */
    @Parameter(property = "jreleaser.git.root.search")
    private Boolean gitRootSearch;
    /**
     * Enable strict mode.
     */
    @Parameter(property = "jreleaser.strict")
    private Boolean strict;
    /**
     * The project name.
     */
    @Parameter(property = "jreleaser.project.name", defaultValue = "${project.artifactId}")
    private String projectName;
    /**
     * The project version.
     */
    @Parameter(property = "jreleaser.project.version", defaultValue = "${project.version}")
    private String projectVersion;
    /**
     * The project version pattern.
     */
    @Parameter(property = "jreleaser.project.version.pattern")
    private String projectVersionPattern;
    /**
     * The project snapshot pattern.
     */
    @Parameter(property = "jreleaser.project.snapshot.pattern")
    private String projectSnapshotPattern;
    /**
     * The project snapshot label.
     */
    @Parameter(property = "jreleaser.project.snapshot.label")
    private String projectSnapshotLabel;
    /**
     * The project copyright.
     */
    @Parameter(property = "jreleaser.project.copyright")
    private String projectCopyright;
    /**
     * The project description.
     */
    @Parameter(property = "jreleaser.project.description")
    private String projectDescription;
    /**
     * The project inception year.
     */
    @Parameter(property = "jreleaser.project.inception.year")
    private String projectInceptionYear;
    /**
     * The project stereotype.
     */
    @Parameter(property = "jreleaser.project.stereotype")
    private String projectStereotype;
    /**
     * The release tag.
     */
    @Parameter(property = "jreleaser.tag.name")
    private String tagName;
    /**
     * The previous release tag.
     */
    @Parameter(property = "jreleaser.previous.tag.name")
    private String previousTagName;
    /**
     * The release name.
     */
    @Parameter(property = "jreleaser.release.name")
    private String releaseName;
    /**
     * The release branch.
     */
    @Parameter(property = "jreleaser.release.branch")
    private String branch;
    /**
     * The milestone name.
     */
    @Parameter(property = "jreleaser.milestone.name")
    private String milestoneName;
    /**
     * If the release is a prerelease.
     */
    @Parameter(property = "jreleaser.prerelease")
    private Boolean prerelease;
    /**
     * The prerelease pattern.
     */
    @Parameter(property = "jreleaser.prerelease.pattern")
    private String prereleasePattern;
    /**
     * If the release is a draft.
     */
    @Parameter(property = "jreleaser.draft")
    private Boolean draft;
    /**
     * Overwrite an existing release.
     */
    @Parameter(property = "jreleaser.overwrite")
    private boolean overwrite;
    /**
     * Update an existing release.
     */
    @Parameter(property = "jreleaser.update")
    private boolean update;
    /**
     * Release section(s) to be updated.
     */
    @Parameter(property = "jreleaser.update.sections")
    private UpdateSection[] updateSections;
    /**
     * Skip tagging the release.
     */
    @Parameter(property = "jreleaser.skip.tag")
    private boolean skipTag;
    /**
     * Skip creating a release.
     */
    @Parameter(property = "jreleaser.skip.release")
    private boolean skipRelease;
    /**
     * Skip creating checksums.
     */
    @Parameter(property = "jreleaser.skip.checksums")
    private boolean skipChecksums;
    /**
     * Path to changelog file.
     */
    @Parameter(property = "jreleaser.changelog")
    private String changelog;
    /**
     * Format generated changelog.
     */
    @Parameter(property = "jreleaser.changelog.formatted")
    private boolean changelogFormatted;
    /**
     * Git username.
     */
    @Parameter(property = "jreleaser.username")
    private String username;
    /**
     * Commit author name.
     */
    @Parameter(property = "jreleaser.commit.author.name")
    private String commitAuthorName;
    /**
     * Commit author e-mail.
     */
    @Parameter(property = "jreleaser.commit.author.email")
    private String commitAuthorEmail;
    /**
     * Sign files.
     */
    @Parameter(property = "jreleaser.signing")
    private boolean signing;
    /**
     * Generate ascii armored signatures.
     */
    @Parameter(property = "jreleaser.armored")
    private boolean armored;
    /**
     * The project authors.
     */
    @Parameter(property = "jreleaser.authors")
    private String[] authors;
    /**
     * Input file(s) to be uploaded.
     */
    @Parameter(property = "jreleaser.files")
    private String[] files;
    /**
     * Input file(s) to be uploaded (as globs).
     */
    @Parameter(property = "jreleaser.globs")
    private String[] globs;
    /**
     * Activates paths matching the current platform.
     */
    @Parameter(property = "jreleaser.select.current.platform")
    private boolean selectCurrentPlatform;
    /**
     * Activates paths matching the given platform.
     */
    @Parameter(property = "jreleaser.select.platforms")
    private String[] selectPlatforms;
    /**
     * Activates paths not matching the given platform.
     */
    @Parameter(property = "jreleaser.reject.platforms")
    private String[] rejectedPlatforms;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());

        JReleaserContext context = ModelAutoConfigurer.builder()
            .logger(getLogger())
            .basedir(project.getBasedir().toPath())
            .outputDirectory(outputDirectory.toPath())
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
            .authors(collectAuthors())
            .files(collectFiles())
            .globs(collectGlobs())
            .selectedPlatforms(collectSelectedPlatforms())
            .rejectedPlatforms(collectRejectedPlatforms())
            .autoConfigure();

        Workflows.release(context).execute();
    }

    private JReleaserLogger getLogger() throws MojoExecutionException {
        return new JReleaserLoggerAdapter(createTracer(), getLog());
    }

    private PrintWriter createTracer() throws MojoExecutionException {
        try {
            java.nio.file.Files.createDirectories(outputDirectory.toPath());
            return newPrintWriter(new FileOutputStream(
                outputDirectory.toPath().resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new MojoExecutionException("Could not initialize trace file", e);
        }
    }

    private List<String> collectAuthors() {
        List<String> list = new ArrayList<>();
        if (null != authors && authors.length > 0) {
            Collections.addAll(list, authors);
        }
        return list;
    }

    private List<String> collectFiles() {
        List<String> list = new ArrayList<>();
        if (null != files && files.length > 0) {
            Collections.addAll(list, files);
        }
        return list;
    }

    private List<String> collectGlobs() {
        List<String> list = new ArrayList<>();
        if (null != globs && globs.length > 0) {
            Collections.addAll(list, globs);
        }
        return list;
    }

    private Set<org.jreleaser.model.UpdateSection> collectUpdateSections() {
        Set<org.jreleaser.model.UpdateSection> set = new LinkedHashSet<>();
        if (null != updateSections && updateSections.length > 0) {
            for (UpdateSection updateSection : updateSections) {
                set.add(org.jreleaser.model.UpdateSection.of(updateSection.name()));
            }
        }
        return set;
    }

    protected List<String> collectSelectedPlatforms() {
        boolean resolvedSelectCurrentPlatform = resolveBoolean(org.jreleaser.model.api.JReleaserContext.SELECT_CURRENT_PLATFORM, selectCurrentPlatform);
        if (resolvedSelectCurrentPlatform) return Collections.singletonList(PlatformUtils.getCurrentFull());

        List<String> list = new ArrayList<>();
        if (null != selectPlatforms && selectPlatforms.length > 0) {
            Collections.addAll(list, selectPlatforms);
        }
        return resolveCollection(org.jreleaser.model.api.JReleaserContext.SELECT_PLATFORMS, list);
    }

    protected List<String> collectRejectedPlatforms() {
        List<String> list = new ArrayList<>();
        if (null != rejectedPlatforms && rejectedPlatforms.length > 0) {
            Collections.addAll(list, rejectedPlatforms);
        }
        return resolveCollection(org.jreleaser.model.api.JReleaserContext.REJECT_PLATFORMS, list);
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
