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
import org.jreleaser.model.internal.packagers.MacportsPackager;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.Errors;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.api.packagers.MacportsPackager.APP_NAME;
import static org.jreleaser.model.internal.validation.common.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.internal.validation.common.TemplateValidator.validateTemplate;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateCommitAuthor;
import static org.jreleaser.model.internal.validation.common.Validator.validateContinueOnError;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.isGraalVMDistribution;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
public final class MacportsPackagerValidator {
    private MacportsPackagerValidator() {
        // noop
    }

    public static void validateMacports(JReleaserContext context, Distribution distribution, MacportsPackager packager, Errors errors) {
        context.getLogger().debug("distribution.{}." + packager.getType(), distribution.getName());
        JReleaserModel model = context.getModel();
        MacportsPackager parentPackager = model.getPackagers().getMacports();

        resolveActivatable(context, packager, "distributions." + distribution.getName() + "." + packager.getType(), parentPackager);
        if (!packager.resolveEnabled(context.getModel().getProject(), distribution)) {
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
            errors.configuration(RB.$("validation_packager_multiple_artifacts", "distribution." + distribution.getName() + ".macports"));
            context.getLogger().debug(RB.$("validation.disabled.multiple.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.multiple.artifacts", distribution.getName(),
                packager.getType(), candidateArtifacts.stream()
                    .map(Artifact::getPath)
                    .collect(toList())));
            packager.disable();
            return;
        } else {
            // activate rmd160 checksum
            context.getModel().getChecksum().getAlgorithms().add(Algorithm.RMD160);
        }

        if (null == packager.getRevision()) {
            packager.setRevision(parentPackager.getRevision());
        }
        if (null == packager.getRevision()) {
            packager.setRevision(0);
        }

        if (packager.getMaintainers().isEmpty()) {
            packager.setMaintainers(parentPackager.getMaintainers());
        }
        if (packager.getCategories().isEmpty()) {
            packager.setCategories(parentPackager.getCategories());
        }
        if (packager.getCategories().isEmpty()) {
            packager.setCategories(Collections.singletonList("devel"));
        }
        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE) {
            if (!packager.getExtraProperties().containsKey(APP_NAME) &&
                parentPackager.getExtraProperties().containsKey(APP_NAME)) {
                packager.getExtraProperties().put(APP_NAME, parentPackager.getExtraProperties().get(APP_NAME));
            }
            if (!packager.getExtraProperties().containsKey(APP_NAME)) {
                packager.getExtraProperties().put(APP_NAME, distribution.getName() + ".app");
            }
        }

        validateCommitAuthor(packager, parentPackager);
        MacportsPackager.MacportsRepository repository = packager.getRepository();
        Validator.validateRepository(context, distribution, repository, parentPackager.getRepository(), "macports.repository");
        validateTemplate(context, distribution, packager, parentPackager, errors);
        mergeExtraProperties(packager, parentPackager);
        validateContinueOnError(packager, parentPackager);

        if (isBlank(packager.getPackageName())) {
            packager.setPackageName(parentPackager.getPackageName());
            if (isBlank(packager.getPackageName())) {
                packager.setPackageName(distribution.getName());
            }
        }

        validateArtifactPlatforms(distribution, packager, candidateArtifacts, errors);

        // TODO: remove in 2.0.0
        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR ||
            isGraalVMDistribution(distribution)) {
            if (isBlank(distribution.getJava().getVersion())) {
                errors.configuration(RB.$("validation_is_missing", "distribution." + distribution.getName() + ".java.version"));
            }
        }

        if (errors.hasErrors()) packager.disable();
    }
}
