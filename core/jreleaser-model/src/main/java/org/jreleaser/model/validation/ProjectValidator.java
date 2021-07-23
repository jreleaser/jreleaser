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
package org.jreleaser.model.validation;

import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.VersionPattern;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.Project.DEFAULT_SNAPSHOT_LABEL;
import static org.jreleaser.model.Project.DEFAULT_SNAPSHOT_PATTERN;
import static org.jreleaser.model.Project.PROJECT_NAME;
import static org.jreleaser.model.Project.PROJECT_SNAPSHOT_LABEL;
import static org.jreleaser.model.Project.PROJECT_SNAPSHOT_PATTERN;
import static org.jreleaser.model.Project.PROJECT_VERSION;
import static org.jreleaser.model.Project.PROJECT_VERSION_PATTERN;
import static org.jreleaser.util.StringUtils.isBlank;

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
                VersionPattern.SEMVER));

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

        boolean javaDistributions = context.getModel().getDistributions().values().stream()
            .map(Distribution::getType)
            .anyMatch(type -> type == Distribution.DistributionType.JAVA_BINARY ||
                type == Distribution.DistributionType.SINGLE_JAR ||
                type == Distribution.DistributionType.NATIVE_IMAGE ||
                type == Distribution.DistributionType.NATIVE_PACKAGE);
        boolean jlinkAssemblers = !context.getModel().getAssemble().getJlink().isEmpty();

        if ((mode == JReleaserContext.Mode.FULL && javaDistributions) || jlinkAssemblers) {
            validateJava(context, project, errors);
        }
    }

    public static void postValidateProject(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (mode != JReleaserContext.Mode.FULL) return;

        context.getLogger().debug("project");
        Project project = context.getModel().getProject();

        if (context.getModel().getActiveDistributions().isEmpty() && !context.getModel().getAnnounce().isEnabled()) {
            return;
        }

        if (isBlank(project.getDescription())) {
            errors.configuration("project.description must not be blank");
        }
        if (isBlank(project.getDocsUrl())) {
            project.setDocsUrl(project.getWebsite());
        }
        if (isBlank(project.getWebsite())) {
            errors.configuration("project.website must not be blank");
        }
        if (isBlank(project.getDocsUrl())) {
            errors.configuration("project.docsUrl must not be blank");
        }
        if (isBlank(project.getLicense())) {
            errors.configuration("project.license must not be blank");
        }
        if (isBlank(project.getLongDescription())) {
            project.setLongDescription(project.getDescription());
        }
        if (project.getAuthors().isEmpty()) {
            errors.configuration("project.authors must not be empty");
        }
        if (isBlank(project.getCopyright())) {
            if (project.getExtraProperties().containsKey("inceptionYear") &&
                !project.getAuthors().isEmpty()) {
                project.setCopyright(
                    project.getExtraProperties().get("inceptionYear") + " " +
                        String.join(",", project.getAuthors()));
            } else {
                context.nag("0.4.0", "project.copyright must not be blank");
                project.setCopyright("");
                // errors.configuration("project.copyright must not be blank");
            }
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
            errors.configuration("project.java.groupId must not be blank");
        }
        if (isBlank(project.getJava().getArtifactId())) {
            errors.configuration("project.java.artifactId must not be blank");
        }
        if (!project.getJava().isMultiProjectSet()) {
            project.getJava().setMultiProject(false);
        }
    }
}