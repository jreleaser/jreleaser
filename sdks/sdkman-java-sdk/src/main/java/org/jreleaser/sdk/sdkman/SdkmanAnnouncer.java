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
package org.jreleaser.sdk.sdkman;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserCommand;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Sdkman;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.model.util.SdkmanHelper.collectArtifacts;
import static org.jreleaser.util.Constants.MAGIC_SET;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class SdkmanAnnouncer implements Announcer {
    private final JReleaserContext context;

    SdkmanAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.SdkmanAnnouncer.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getSdkman().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Map<String, Distribution> distributions = context.getModel().getActiveDistributions().stream()
            .filter(d -> d.getSdkman().isEnabled())
            .filter(d -> !JReleaserCommand.supportsPublish(context.getCommand()) || d.getSdkman().isPublished())
            .collect(Collectors.toMap(distribution -> {
                Sdkman sdkman = distribution.getSdkman();
                return isNotBlank(sdkman.getCandidate()) ? sdkman.getCandidate().trim() : context.getModel().getProject().getName();
            }, distribution -> distribution));

        Boolean set = (Boolean) context.getModel().getAnnounce().getSdkman().getExtraProperties().remove(MAGIC_SET);
        if (distributions.isEmpty()) {
            if (set == null || !set) {
                announceProject();
            } else {
                context.getLogger().debug(RB.$("announcers.announcer.disabled"));
            }
            return;
        }

        boolean failures = false;
        for (Map.Entry<String, Distribution> e : distributions.entrySet()) {
            String candidate = e.getKey();
            Distribution distribution = e.getValue();

            Sdkman sdkman = distribution.getSdkman();
            Map<String, Object> props = context.fullProps();
            props.putAll(distribution.props());
            String releaseNotesUrl = resolveTemplate(sdkman.getReleaseNotesUrl(), props);
            String command = sdkman.getCommand().name().toLowerCase();

            context.getLogger().info(RB.$("sdkman.release.announce"), command, candidate);
            try {
                AnnounceSdkmanCommand.builder(context.getLogger())
                    .connectTimeout(sdkman.getConnectTimeout())
                    .readTimeout(sdkman.getReadTimeout())
                    .consumerKey(context.isDryrun() ? "**UNDEFINED**" : sdkman.getResolvedConsumerKey())
                    .consumerToken(context.isDryrun() ? "**UNDEFINED**" : sdkman.getResolvedConsumerToken())
                    .candidate(candidate)
                    .version(context.getModel().getProject().getVersion())
                    .releaseNotesUrl(releaseNotesUrl)
                    .dryrun(context.isDryrun())
                    .build()
                    .execute();
            } catch (SdkmanException x) {
                context.getLogger().warn(x.getMessage().trim());
                failures = true;
            }
        }

        if (failures) {
            throw new AnnounceException(RB.$("ERROR_sdkman_announce"));
        }
    }

    private void announceProject() throws AnnounceException {
        org.jreleaser.model.SdkmanAnnouncer sdkman = context.getModel().getAnnounce().getSdkman();

        Map<String, String> platforms = new LinkedHashMap<>();
        // collect artifacts by supported SDKMAN! platform
        for (Distribution distribution : context.getModel().getActiveDistributions()) {
            if (!isDistributionSupported(distribution)) {
                continue;
            }
            collectArtifacts(context, distribution, platforms);
        }

        if (platforms.isEmpty()) {
            context.getLogger().warn(RB.$("sdkman.no.suitable.artifacts"));
            return;
        }

        try {
            String candidate = isNotBlank(sdkman.getCandidate()) ? sdkman.getCandidate().trim() : context.getModel().getProject().getName();
            String releaseNotesUrl = resolveTemplate(sdkman.getReleaseNotesUrl(), context.fullProps());

            if (sdkman.isMajor()) {
                context.getLogger().info(RB.$("sdkman.release.announce.major"), candidate);
                MajorReleaseSdkmanCommand.builder(context.getLogger())
                    .connectTimeout(sdkman.getConnectTimeout())
                    .readTimeout(sdkman.getReadTimeout())
                    .consumerKey(context.isDryrun() ? "**UNDEFINED**" : sdkman.getResolvedConsumerKey())
                    .consumerToken(context.isDryrun() ? "**UNDEFINED**" : sdkman.getResolvedConsumerToken())
                    .candidate(candidate)
                    .version(context.getModel().getProject().getVersion())
                    .platforms(platforms)
                    .releaseNotesUrl(releaseNotesUrl)
                    .dryrun(context.isDryrun())
                    .build()
                    .execute();
            } else {
                context.getLogger().info(RB.$("sdkman.release.announce.minor"), candidate);
                MinorReleaseSdkmanCommand.builder(context.getLogger())
                    .connectTimeout(sdkman.getConnectTimeout())
                    .readTimeout(sdkman.getReadTimeout())
                    .consumerKey(context.isDryrun() ? "**UNDEFINED**" : sdkman.getResolvedConsumerKey())
                    .consumerToken(context.isDryrun() ? "**UNDEFINED**" : sdkman.getResolvedConsumerToken())
                    .candidate(candidate)
                    .version(context.getModel().getProject().getVersion())
                    .platforms(platforms)
                    .releaseNotesUrl(releaseNotesUrl)
                    .dryrun(context.isDryrun())
                    .build()
                    .execute();
            }
        } catch (SdkmanException e) {
            throw new AnnounceException(e);
        }
    }

    private boolean isDistributionSupported(Distribution distribution) {
        return (distribution.getType() == Distribution.DistributionType.JAVA_BINARY ||
            distribution.getType() == Distribution.DistributionType.JLINK ||
            distribution.getType() == Distribution.DistributionType.NATIVE_IMAGE) &&
            !isTrue(distribution.getExtraProperties().get("skipSdkman"));
    }
}
