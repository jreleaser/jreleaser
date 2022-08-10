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
import org.jreleaser.model.Active;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Project;
import org.jreleaser.util.CalVer;
import org.jreleaser.util.ChronVer;
import org.jreleaser.util.CustomVersion;
import org.jreleaser.util.Errors;
import org.jreleaser.util.JavaModuleVersion;
import org.jreleaser.util.JavaRuntimeVersion;
import org.jreleaser.util.SemVer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.jreleaser.model.Chocolatey.CHOCOLATEY_API_KEY;
import static org.jreleaser.model.Chocolatey.DEFAULT_CHOCOLATEY_PUSH_URL;
import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class ChocolateyValidator extends Validator {
    public static void validateChocolatey(JReleaserContext context, Distribution distribution, Chocolatey packager, Errors errors) {
        JReleaserModel model = context.getModel();
        Chocolatey parentPackager = model.getPackagers().getChocolatey();

        if (!packager.isActiveSet() && parentPackager.isActiveSet()) {
            packager.setActive(parentPackager.getActive());
        }
        Project project = context.getModel().getProject();
        if (!packager.resolveEnabled(project, distribution)) return;
        GitService service = model.getRelease().getGitService();
        if (!service.isReleaseSupported()) {
            packager.disable();
            return;
        }

        context.getLogger().debug("distribution.{}.chocolatey", distribution.getName());

        List<Artifact> candidateArtifacts = packager.resolveCandidateArtifacts(context, distribution);
        if (candidateArtifacts.size() == 0) {
            packager.setActive(Active.NEVER);
            packager.disable();
            return;
        } else if (candidateArtifacts.size() > 1) {
            errors.configuration(RB.$("validation_packager_multiple_artifacts", "distribution." + distribution.getName() + ".chocolatey"));
            packager.disable();
            return;
        }

        validateCommitAuthor(packager, parentPackager);
        Chocolatey.ChocolateyBucket bucket = packager.getBucket();
        bucket.resolveEnabled(model.getProject());
        validateTap(context, distribution, bucket, parentPackager.getBucket(), "chocolatey.bucket");
        validateTemplate(context, distribution, packager, parentPackager, errors);
        mergeExtraProperties(packager, parentPackager);
        validateContinueOnError(packager, parentPackager);
        if (isBlank(packager.getDownloadUrl())) {
            packager.setDownloadUrl(parentPackager.getDownloadUrl());
        }

        if (isBlank(packager.getPackageName())) {
            packager.setPackageName(parentPackager.getPackageName());
            if (isBlank(packager.getPackageName())) {
                packager.setPackageName(distribution.getName());
            }
        }

        if (isBlank(packager.getPackageVersion())) {
            packager.setPackageVersion(parentPackager.getPackageVersion());
            if (isBlank(packager.getPackageVersion())) {
                packager.setPackageVersion(model.getProject().getResolvedVersion());
            }
        }

        if (isBlank(packager.getUsername())) {
            packager.setUsername(service.getOwner());
        }
        if (!packager.isRemoteBuildSet() && parentPackager.isRemoteBuildSet()) {
            packager.setRemoteBuild(parentPackager.isRemoteBuild());
        }

        if (isBlank(packager.getTitle())) {
            packager.setTitle(parentPackager.getTitle());
        }
        if (isBlank(packager.getTitle())) {
            packager.setTitle(model.getProject().getName());
        }

        if (isBlank(packager.getIconUrl())) {
            packager.setIconUrl(parentPackager.getIconUrl());
        }

        if (isBlank(packager.getSource())) {
            packager.setSource(parentPackager.getSource());
        }
        if (isBlank(packager.getSource())) {
            packager.setSource(DEFAULT_CHOCOLATEY_PUSH_URL);
        }

        if (!packager.isRemoteBuild()) {
            if (isBlank(packager.getApiKey())) {
                packager.setApiKey(parentPackager.getApiKey());
            }

            packager.setApiKey(
                checkProperty(context,
                    CHOCOLATEY_API_KEY,
                    "chocolatey.apiKey",
                    packager.getApiKey(),
                    errors,
                    context.isDryrun()));
        }

        validateArtifactPlatforms(context, distribution, packager, candidateArtifacts, errors);

        // packageVersion must be #, #.#, #.#.#, #.#.#.#, #.#.#.yyyyMMdd
        // tag is allowed but only if separated by -
        try {
            String packageVersion = resolveTemplate(packager.getPackageVersion(), context.getModel().props());
            switch (project.versionPattern().getType()) {
                case SEMVER:
                    checkSemver(context, distribution, packager, SemVer.of(packageVersion), errors);
                    break;
                case JAVA_RUNTIME:
                    checkJavaRuntime(context, distribution, packager, JavaRuntimeVersion.of(packageVersion), errors);
                    break;
                case JAVA_MODULE:
                    checkJavaModule(context, distribution, packager, JavaModuleVersion.of(packageVersion), errors);
                    break;
                case CALVER:
                    checkCalVer(context, distribution, packager, CalVer.of(project.versionPattern().getFormat(), packageVersion), errors);
                    break;
                case CHRONVER:
                    checkChronVer(context, distribution, packager, ChronVer.of(packageVersion), errors);
                    break;
                case CUSTOM:
                default:
                    checkCustomVersion(context, distribution, packager, CustomVersion.of(packageVersion), errors);
            }
        } catch (IllegalArgumentException e) {
            // invalid
            errors.configuration(RB.$("validation_chocolatey_package_version", packager.getPackageVersion()));
        }
    }

    private static void checkSemver(JReleaserContext context, Distribution distribution, Chocolatey packager, SemVer version, Errors errors) {
        if (version.hasBuild()) {
            throw new IllegalArgumentException();
        }

        if (!version.hasTag() || "-".equals(version.getTagsep())) return;

        String tag = version.getTag();

        boolean integer = false;
        boolean date = false;
        // tag is either an integer
        try {
            Integer.parseInt(tag);
            integer = true;

            // or follows the yyyyMMdd format
            if (tag.length() == 8) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                try {
                    dateFormat.parse(tag);
                    date = true;
                } catch (ParseException e) {
                    throw new IllegalArgumentException();
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }

        if (!date && !integer) {
            throw new IllegalArgumentException();
        }
    }

    private static void checkJavaRuntime(JReleaserContext context, Distribution distribution, Chocolatey packager, JavaRuntimeVersion version, Errors errors) {
        if (version.hasBuild()) {
            throw new IllegalArgumentException();
        }

        if (!version.hasPrerelease() && version.hasOptional()) {
            throw new IllegalArgumentException();
        }
    }

    private static void checkJavaModule(JReleaserContext context, Distribution distribution, Chocolatey packager, JavaModuleVersion version, Errors errors) {
        if (version.hasBuild()) {
            throw new IllegalArgumentException();
        }
    }

    private static void checkCalVer(JReleaserContext context, Distribution distribution, Chocolatey packager, CalVer version, Errors errors) {

    }

    private static void checkChronVer(JReleaserContext context, Distribution distribution, Chocolatey packager, ChronVer version, Errors errors) {
        if (version.hasChangeset()) {
            ChronVer.Changeset changeset = version.getChangeset();
            if (changeset.hasTag() || changeset.hasChange2()) {
                throw new IllegalArgumentException();
            }
        }
    }

    private static void checkCustomVersion(JReleaserContext context, Distribution distribution, Chocolatey packager, CustomVersion version, Errors errors) {

    }

    public static void postValidateChocolatey(JReleaserContext context, Distribution distribution, Chocolatey packager, Errors errors) {
        if (!packager.isEnabled()) return;
        context.getLogger().debug("distribution.{}.chocolatey", distribution.getName());

        Project project = context.getModel().getProject();

        if (isBlank(project.getLinks().getLicense())) {
            errors.configuration(RB.$("ERROR_project_no_license_url"));
        }
    }
}
