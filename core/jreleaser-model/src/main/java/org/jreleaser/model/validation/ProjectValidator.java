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
package org.jreleaser.model.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.LicenseId;
import org.jreleaser.model.Project;
import org.jreleaser.model.VersionPattern;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.Project.DEFAULT_SNAPSHOT_LABEL;
import static org.jreleaser.model.Project.DEFAULT_SNAPSHOT_PATTERN;
import static org.jreleaser.model.Project.PROJECT_NAME;
import static org.jreleaser.model.Project.PROJECT_SNAPSHOT_FULL_CHANGELOG;
import static org.jreleaser.model.Project.PROJECT_SNAPSHOT_LABEL;
import static org.jreleaser.model.Project.PROJECT_SNAPSHOT_PATTERN;
import static org.jreleaser.model.Project.PROJECT_VERSION;
import static org.jreleaser.model.Project.PROJECT_VERSION_PATTERN;
import static org.jreleaser.util.FileUtils.findLicenseFile;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class ProjectValidator extends Validator {
    public static void validateProject(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("project");
        Project project = context.getModel().getProject();

        project.setName(
            checkProperty(context,
                PROJECT_NAME,
                "project.name",
                project.getName(),
                errors));

        project.setVersion(
            checkProperty(context,
                PROJECT_VERSION,
                "project.version",
                project.getVersion(),
                errors));

        project.setVersionPattern(
            checkProperty(context,
                PROJECT_VERSION_PATTERN,
                "project.versionPattern",
                project.getVersionPattern(),
                VersionPattern.Type.SEMVER.toString()));

        project.getSnapshot().setPattern(
            checkProperty(context,
                PROJECT_SNAPSHOT_PATTERN,
                "project.snapshot.pattern",
                project.getSnapshot().getPattern(),
                DEFAULT_SNAPSHOT_PATTERN));

        project.getSnapshot().setLabel(
            checkProperty(context,
                PROJECT_SNAPSHOT_LABEL,
                "project.snapshot.label",
                project.getSnapshot().getLabel(),
                DEFAULT_SNAPSHOT_LABEL));
        // eager resolve
        project.getSnapshot().getResolvedLabel(context.getModel());

        project.getSnapshot().setFullChangelog(
            checkProperty(context,
                PROJECT_SNAPSHOT_FULL_CHANGELOG,
                "project.snapshot.fullChangelog",
                project.getSnapshot().getFullChangelog(),
                false));

        if (project.versionPattern().getType() == VersionPattern.Type.CALVER) {
            if (isBlank(project.versionPattern().getFormat())) {
                errors.configuration(RB.$("validation_version_format_missing",
                    "project.versionPattern", VersionPattern.Type.CALVER.toString()));
            }
        }

        boolean javaDistributions = context.getModel().getDistributions().values().stream()
            .map(Distribution::getType)
            .anyMatch(type -> type == Distribution.DistributionType.JAVA_BINARY ||
                type == Distribution.DistributionType.SINGLE_JAR ||
                type == Distribution.DistributionType.NATIVE_IMAGE ||
                type == Distribution.DistributionType.NATIVE_PACKAGE);
        boolean javaAssemblers = !context.getModel().getAssemble().getJlink().isEmpty() ||
            !context.getModel().getAssemble().getJpackage().isEmpty() ||
            !context.getModel().getAssemble().getNativeImage().isEmpty();

        if ((mode.validateConfig() && javaDistributions) || javaAssemblers) {
            validateJava(context, project, errors);
        }

        validateScreenshots(context, mode, project.getScreenshots(), errors, "project");
    }

    public static void postValidateProject(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("project");
        Project project = context.getModel().getProject();

        // TODO: remove in 2.0.0
        if (null == project.getInceptionYear() &&
            project.getExtraProperties().containsKey("inceptionYear")) {
            project.setInceptionYear(project.getExtraProperty("inceptionYear"));
            context.nag("1.2.0","Use project.inceptionYear instead of project.extraProperties.inceptionYear");
        }

        if (isBlank(project.getCopyright())) {
            if (project.getInceptionYear() != null &&
                !project.getAuthors().isEmpty()) {
                project.setCopyright(
                    project.getInceptionYear() + " " +
                        String.join(",", project.getAuthors()));
            } else {
                context.nag("0.4.0", "project.copyright must not be blank");
                project.setCopyright("");
            }
        }

        if (isBlank(project.getLinks().getLicense())) {
            if (isNotBlank(project.getLicense())) {
                LicenseId.findByLiteral(project.getLicense()).ifPresent(licenseId ->
                    project.getLinks().setLicense(licenseId.url()));
            }
        }

        if (isBlank(project.getLinks().getLicense()) && context.getModel().getCommit() != null) {
            findLicenseFile(context.getBasedir())
                .ifPresent(path -> {
                    GitService service = context.getModel().getRelease().getGitService();
                    String srcUrl = service.getResolvedSrcUrl(context.getModel());
                    if (!srcUrl.endsWith("/")) srcUrl += "/";
                    srcUrl += path.getFileName().toString();
                    project.getLinks().setLicense(srcUrl);
                });
        }
        if (isBlank(project.getLinks().getVcsBrowser())) {
            project.getLinks().setVcsBrowser(context.getModel().getRelease().getGitService().getRepoUrl());
        }
        if (isBlank(project.getLinks().getBugTracker())) {
            project.getLinks().setBugTracker(context.getModel().getRelease().getGitService().getIssueTrackerUrl());
        }
        if (isBlank(project.getLinks().getDocumentation())) {
            project.getLinks().setDocumentation(project.getLinks().getHomepage());
        }

        if (!mode.validateConfig()) return;

        if (context.getModel().getActiveDistributions().isEmpty() && !context.getModel().getAnnounce().isEnabled()) {
            return;
        }

        if (isBlank(project.getDescription())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "project.description"));
        }
        if (isBlank(project.getLinks().getHomepage())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "project.website"));
        }
        if (isBlank(project.getLinks().getDocumentation())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "project.docsUrl"));
        }
        if (isBlank(project.getLicense())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "project.license"));
        }
        if (isBlank(project.getLongDescription())) {
            project.setLongDescription(project.getDescription());
        }
        if (project.getAuthors().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_blank", "project.authors"));
        }
    }

    private static void validateJava(JReleaserContext context, Project project, Errors errors) {
        context.getLogger().debug("project.java");
        if (!project.getJava().isSet()) return;

        project.getJava().setEnabled(true);

        if (isBlank(project.getJava().getArtifactId())) {
            project.getJava().setArtifactId(project.getName());
        }
        if (isBlank(project.getJava().getGroupId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "project.java.groupId"));
        }
        if (isBlank(project.getJava().getArtifactId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "project.java.artifactId"));
        }
        if (!project.getJava().isMultiProjectSet()) {
            project.getJava().setMultiProject(false);
        }
    }
}