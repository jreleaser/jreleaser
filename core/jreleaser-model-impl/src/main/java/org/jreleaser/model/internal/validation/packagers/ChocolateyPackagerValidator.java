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
package org.jreleaser.model.internal.validation.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.ChocolateyPackager;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;
import org.jreleaser.version.ChronVer;
import org.jreleaser.version.JavaModuleVersion;
import org.jreleaser.version.JavaRuntimeVersion;
import org.jreleaser.version.SemanticVersion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.api.packagers.ChocolateyPackager.CHOCOLATEY_API_KEY;
import static org.jreleaser.model.api.packagers.ChocolateyPackager.DEFAULT_CHOCOLATEY_PUSH_URL;
import static org.jreleaser.model.internal.validation.common.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.internal.validation.common.TemplateValidator.validateTemplate;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateCommitAuthor;
import static org.jreleaser.model.internal.validation.common.Validator.validateContinueOnError;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class ChocolateyPackagerValidator {
    private ChocolateyPackagerValidator() {
        // noop
    }

    public static void validateChocolatey(JReleaserContext context, Distribution distribution, ChocolateyPackager packager, Errors errors) {
        context.getLogger().debug("distribution.{}." + packager.getType(), distribution.getName());
        JReleaserModel model = context.getModel();
        ChocolateyPackager parentPackager = model.getPackagers().getChocolatey();

        resolveActivatable(context, packager, "distributions." + distribution.getName() + "." + packager.getType(), parentPackager);
        Project project = context.getModel().getProject();
        if (!packager.resolveEnabled(project, distribution)) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }
        Releaser<?> service = model.getRelease().getReleaser();
        if (!service.isReleaseSupported()) {
            context.getLogger().debug(RB.$("validation.disabled.release"));
            packager.disable();
            return;
        }

        List<Artifact> candidateArtifacts = packager.resolveCandidateArtifacts(context, distribution);
        if (candidateArtifacts.isEmpty()) {
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.no.artifacts", distribution.getName(),
                packager.getType(), packager.getSupportedFileExtensions(distribution.getType())));
            packager.disable();
            return;
        } else if (candidateArtifacts.size() > 1) {
            errors.configuration(RB.$("validation_packager_multiple_artifacts", "distribution." + distribution.getName() + ".chocolatey"));
            context.getLogger().debug(RB.$("validation.disabled.multiple.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.multiple.artifacts", distribution.getName(),
                packager.getType(), candidateArtifacts.stream()
                    .map(Artifact::getPath)
                    .collect(toList())));
            packager.disable();
            return;
        }

        validateCommitAuthor(packager, parentPackager);
        ChocolateyPackager.ChocolateyRepository bucket = packager.getRepository();
        Validator.validateRepository(context, distribution, bucket, parentPackager.getRepository(), "chocolatey.repository");
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
                    listOf(
                        "distributions." + distribution.getName() + ".chocolatey.api.key",
                        CHOCOLATEY_API_KEY),
                    "distributions." + distribution.getName() + ".chocolatey.apiKey",
                    packager.getApiKey(),
                    errors,
                    context.isDryrun()));
        }

        validateArtifactPlatforms(distribution, packager, candidateArtifacts, errors);

        // packageVersion must be #, #.#, #.#.#, #.#.#.#, #.#.#.yyyyMMdd
        // tag is allowed but only if separated by -
        try {
            String packageVersion = resolveTemplate(packager.getPackageVersion(), context.getModel().props());
            switch (project.versionPattern().getType()) {
                case SEMVER:
                    checkSemver(SemanticVersion.of(packageVersion));
                    break;
                case JAVA_RUNTIME:
                    checkJavaRuntime(JavaRuntimeVersion.of(packageVersion));
                    break;
                case JAVA_MODULE:
                    checkJavaModule(JavaModuleVersion.of(packageVersion));
                    break;
                case CHRONVER:
                    checkChronVer(ChronVer.of(packageVersion));
                    break;
                case CALVER:
                case CUSTOM:
                default:
                    // noop
            }
        } catch (IllegalArgumentException e) {
            // invalid
            errors.configuration(RB.$("validation_chocolatey_package_version", packager.getPackageVersion()));
        }
    }

    private static void checkSemver(SemanticVersion version) {
        if (version.hasBuild()) {
            throw new IllegalArgumentException();
        }

        if (!version.hasTag() || "-".equals(version.getTagsep())) return;

        String tag = version.getTag();

        // tag is either an integer
        try {
            Integer.parseInt(tag);

            // or follows the yyyyMMdd format
            if (tag.length() == 8) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                try {
                    dateFormat.parse(tag);
                } catch (ParseException e) {
                    // potentially tag could be an 8 digit integer that does not
                    // map to a date in yyyyMMdd format
                    throw new IllegalArgumentException();
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
    }

    private static void checkJavaRuntime(JavaRuntimeVersion version) {
        if (version.hasBuild()) {
            throw new IllegalArgumentException();
        }

        if (!version.hasPrerelease() && version.hasOptional()) {
            throw new IllegalArgumentException();
        }
    }

    private static void checkJavaModule(JavaModuleVersion version) {
        if (version.hasBuild()) {
            throw new IllegalArgumentException();
        }
    }

    private static void checkChronVer(ChronVer version) {
        if (version.hasChangeset()) {
            ChronVer.Changeset changeset = version.getChangeset();
            if (changeset.hasTag() || changeset.hasChange2()) {
                throw new IllegalArgumentException();
            }
        }
    }

    public static void postValidateChocolatey(JReleaserContext context, Distribution distribution, ChocolateyPackager packager, Errors errors) {
        if (!packager.isEnabled()) return;
        context.getLogger().debug("distribution.{}.chocolatey", distribution.getName());

        Project project = context.getModel().getProject();

        if (distribution.getTags().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", "distribution.tags"));
        }

        if (isBlank(project.getLinks().getLicense())) {
            errors.configuration(RB.$("ERROR_project_no_license_url"));
        }
    }
}
