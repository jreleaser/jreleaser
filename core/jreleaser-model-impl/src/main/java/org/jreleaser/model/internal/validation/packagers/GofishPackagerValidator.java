/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.internal.packagers.GofishPackager;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.util.Errors;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.model.internal.validation.common.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.internal.validation.common.TemplateValidator.validateTemplate;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateCommitAuthor;
import static org.jreleaser.model.internal.validation.common.Validator.validateContinueOnError;
import static org.jreleaser.model.internal.validation.common.Validator.validateTap;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public final class GofishPackagerValidator {
    private GofishPackagerValidator() {
        // noop
    }

    public static void validateGofish(JReleaserContext context, Distribution distribution, GofishPackager packager, Errors errors) {
        context.getLogger().debug("distribution.{}." + packager.getType(), distribution.getName());
        JReleaserModel model = context.getModel();
        GofishPackager parentPackager = model.getPackagers().getGofish();

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

        nag("Gofish is deprecated since 1.1.0 and will be removed in 2.0.0");

        List<Artifact> candidateArtifacts = packager.resolveCandidateArtifacts(context, distribution);
        if (candidateArtifacts.isEmpty()) {
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.no.artifacts", distribution.getName(),
                packager.getType(), packager.getSupportedFileExtensions(distribution.getType())));
            packager.disable();
            return;
        } else if (candidateArtifacts.stream()
            .filter(artifact -> isBlank(artifact.getPlatform()))
            .count() > 1) {
            errors.configuration(RB.$("validation_packager_multiple_artifacts", "distribution." + distribution.getName() + ".gofish"));
            context.getLogger().debug(RB.$("validation.disabled.multiple.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.multiple.artifacts", distribution.getName(),
                packager.getType(), candidateArtifacts.stream()
                    .filter(artifact -> isBlank(artifact.getPlatform()))
                    .map(Artifact::getPath)
                    .collect(toList())));
            packager.disable();
            return;
        }

        validateCommitAuthor(packager, parentPackager);
        GofishPackager.GofishRepository repository = packager.getRepository();
        validateTap(context, distribution, repository, parentPackager.getRepository(), "gofish.repository");
        validateTemplate(context, distribution, packager, parentPackager, errors);
        mergeExtraProperties(packager, parentPackager);
        validateContinueOnError(packager, parentPackager);
        if (isBlank(packager.getDownloadUrl())) {
            packager.setDownloadUrl(parentPackager.getDownloadUrl());
        }
        validateArtifactPlatforms(distribution, packager, candidateArtifacts, errors);
    }
}
