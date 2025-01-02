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
import org.jreleaser.model.internal.packagers.SdkmanPackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.util.Errors;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.Constants.MAGIC_SET;
import static org.jreleaser.model.api.packagers.SdkmanPackager.SDKMAN_CONSUMER_KEY;
import static org.jreleaser.model.api.packagers.SdkmanPackager.SDKMAN_CONSUMER_TOKEN;
import static org.jreleaser.model.internal.validation.common.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateContinueOnError;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public final class SdkmanPackagerValidator {
    private SdkmanPackagerValidator() {
        // noop
    }

    public static void validateSdkman(JReleaserContext context, Distribution distribution, SdkmanPackager packager, Errors errors) {
        context.getLogger().debug("distribution.{}." + packager.getType(), distribution.getName());
        JReleaserModel model = context.getModel();
        SdkmanPackager parentPackager = model.getPackagers().getSdkman();

        boolean packagerSet = packager.isActiveSet();
        boolean parentPackagerSet = parentPackager.isActiveSet();
        packager.getExtraProperties().put(MAGIC_SET, packagerSet || parentPackagerSet);

        resolveActivatable(context, packager, "distributions." + distribution.getName() + "." + packager.getType(), parentPackager);
        if (!packager.resolveEnabled(context.getModel().getProject(), distribution)) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }
        BaseReleaser<?, ?> service = model.getRelease().getReleaser();
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
        }

        mergeExtraProperties(packager, parentPackager);
        validateContinueOnError(packager, parentPackager);
        if (isBlank(packager.getDownloadUrl())) {
            packager.setDownloadUrl(parentPackager.getDownloadUrl());
        }

        if (null == packager.getCommand()) {
            packager.setCommand(parentPackager.getCommand());
            if (null == packager.getCommand()) {
                packager.setCommand(org.jreleaser.model.Sdkman.Command.MAJOR);
            }
        }

        if (isBlank(packager.getCandidate())) {
            packager.setCandidate(parentPackager.getCandidate());
            if (isBlank(packager.getCandidate())) {
                packager.setCandidate(distribution.getName());
            }
        }

        if (isBlank(packager.getReleaseNotesUrl())) {
            packager.setReleaseNotesUrl(parentPackager.getReleaseNotesUrl());
            if (isBlank(packager.getReleaseNotesUrl())) {
                packager.setReleaseNotesUrl(service.getReleaseNotesUrl());
            }
        }

        if (isBlank(packager.getConsumerKey())) {
            packager.setConsumerKey(parentPackager.getConsumerKey());
        }
        if (isBlank(packager.getConsumerToken())) {
            packager.setConsumerToken(parentPackager.getConsumerToken());
        }

        String baseKey = "distributions." + distribution.getName();
        packager.setConsumerKey(
            checkProperty(context,
                listOf(
                    baseKey + ".sdkman.consumer.key",
                    SDKMAN_CONSUMER_KEY),
                baseKey + ".sdkman.consumerKey",
                packager.getConsumerKey(),
                errors,
                context.isDryrun()));

        packager.setConsumerToken(
            checkProperty(context,
                listOf(
                    baseKey + ".sdkman.consumer.token",
                    SDKMAN_CONSUMER_TOKEN),
                baseKey + ".sdkman.consumerToken",
                packager.getConsumerToken(),
                errors,
                context.isDryrun()));

        validateTimeout(packager);

        validateArtifactPlatforms(distribution, packager, candidateArtifacts, errors);
    }

    public static void postValidateSdkman(JReleaserContext context, Errors errors) {
        Map<String, List<Distribution>> map = context.getModel().getActiveDistributions().stream()
            .peek(distribution -> {
                if (distribution.getSdkman().getExtraProperties().containsKey(MAGIC_SET)) {
                    boolean set = (boolean) distribution.getSdkman().getExtraProperties().remove(MAGIC_SET);
                    if (set) {
                        context.getModel().getAnnounce().getSdkman().getExtraProperties().put(MAGIC_SET, set);
                    }
                }
            })
            .filter(d -> d.getSdkman().isEnabled())
            .collect(groupingBy(d -> d.getSdkman().getCandidate()));

        map.forEach((candidate, distributions) -> {
            if (distributions.size() > 1) {
                errors.configuration(RB.$("validation_sdkman_multiple_definition", candidate,
                    distributions.stream().map(Distribution::getName).collect(Collectors.joining(", "))));
            }
        });
    }
}
