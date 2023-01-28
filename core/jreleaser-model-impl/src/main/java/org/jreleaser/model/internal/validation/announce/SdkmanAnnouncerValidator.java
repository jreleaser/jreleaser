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
package org.jreleaser.model.internal.validation.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.SdkmanAnnouncer;
import org.jreleaser.model.internal.packagers.SdkmanPackager;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.Constants.MAGIC_SET;
import static org.jreleaser.model.api.packagers.SdkmanPackager.SDKMAN_CONSUMER_KEY;
import static org.jreleaser.model.api.packagers.SdkmanPackager.SDKMAN_CONSUMER_TOKEN;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class SdkmanAnnouncerValidator {
    private SdkmanAnnouncerValidator() {
        // noop
    }

    public static void validateSdkmanAnnouncer(JReleaserContext context, SdkmanAnnouncer sdkman, Errors errors) {
        context.getLogger().debug("announce.sdkman");
        // activate if there are any active distributions with Sdkman packager enabled
        context.getModel().getActiveDistributions().stream()
            .filter(d -> d.getSdkman().isEnabled())
            .findFirst()
            .ifPresent(distribution -> sdkman.setActive(Active.ALWAYS));

        if (!sdkman.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }
        if (!context.getModel().getRelease().getReleaser().isReleaseSupported()) {
            context.getLogger().debug(RB.$("validation.disabled.release"));
            sdkman.disable();
            return;
        }

        Boolean set = (Boolean) sdkman.getExtraProperties().get(MAGIC_SET);
        if (null != set && set) {
            context.getLogger().debug(RB.$("validation.disabled"));
            sdkman.disable();
            return;
        }

        sdkman.setConsumerKey(
            checkProperty(context,
                listOf(
                    "announce.sdkman.consumer.key",
                    SDKMAN_CONSUMER_KEY),
                "announce.sdkman.consumerKey",
                sdkman.getConsumerKey(),
                errors,
                context.isDryrun()));

        sdkman.setConsumerToken(
            checkProperty(context,
                listOf(
                    "announce.sdkman.consumer.token",
                    SDKMAN_CONSUMER_TOKEN),
                "announce.sdkman.consumerToken",
                sdkman.getConsumerToken(),
                errors,
                context.isDryrun()));

        SdkmanPackager sdkmanPackager = context.getModel().getPackagers().getSdkman();
        if (isBlank(sdkman.getConsumerKey()) && sdkmanPackager.isEnabled()) {
            sdkman.setConsumerKey(sdkmanPackager.getConsumerKey());
        }
        if (isBlank(sdkman.getConsumerToken()) && sdkmanPackager.isEnabled()) {
            sdkman.setConsumerToken(sdkmanPackager.getConsumerToken());
        }

        if (isBlank(sdkman.getReleaseNotesUrl())) {
            sdkman.setReleaseNotesUrl(context.getModel().getRelease().getReleaser().getReleaseNotesUrl());
        }

        if (context.getModel().getActiveDistributions().isEmpty()) {
            errors.warning(RB.$("validation_skdman_disable"));
            sdkman.disable();
        }

        if (null == sdkman.getCommand()) {
            sdkman.setCommand(org.jreleaser.model.Sdkman.Command.MAJOR);
        }

        validateTimeout(sdkman);
    }
}