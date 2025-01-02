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
package org.jreleaser.sdk.sdkman;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.SdkmanPackager;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.model.Constants.MAGIC_SET;
import static org.jreleaser.model.internal.util.SdkmanHelper.collectArtifacts;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class SdkmanAnnouncer implements Announcer<org.jreleaser.model.api.announce.SdkmanAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.SdkmanAnnouncer sdkman;

    public SdkmanAnnouncer(JReleaserContext context) {
        this.context = context;
        this.sdkman = context.getModel().getAnnounce().getSdkman();
    }

    @Override
    public org.jreleaser.model.api.announce.SdkmanAnnouncer getAnnouncer() {
        return sdkman.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.SdkmanAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return sdkman.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Map<String, Distribution> distributions = context.getModel().getActiveDistributions().stream()
            .filter(d -> d.getSdkman().isEnabled())
            .filter(d -> !JReleaserCommand.supportsPublish(context.getCommand()) || d.getSdkman().isPublished())
            .collect(Collectors.toMap(distribution -> {
                SdkmanPackager sdkman = distribution.getSdkman();
                return isNotBlank(sdkman.getCandidate()) ? sdkman.getCandidate().trim() : context.getModel().getProject().getName();
            }, distribution -> distribution));

        Boolean set = (Boolean) sdkman.getExtraProperties().get(MAGIC_SET);
        sdkman.getExtraProperties().remove(MAGIC_SET);

        if (distributions.isEmpty()) {
            if (null == set || !set) {
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

            SdkmanPackager sdkman = distribution.getSdkman();
            TemplateContext props = context.fullProps();
            props.setAll(distribution.props());
            String releaseNotesUrl = resolveTemplate(sdkman.getReleaseNotesUrl(), props);
            String command = sdkman.getCommand().name().toLowerCase(Locale.ENGLISH);

            context.getLogger().info(RB.$("sdkman.release.announce"), command, candidate);
            try {
                AnnounceSdkmanCommand.builder(context.asImmutable())
                    .connectTimeout(sdkman.getConnectTimeout())
                    .readTimeout(sdkman.getReadTimeout())
                    .consumerKey(context.isDryrun() ? "**UNDEFINED**" : sdkman.getConsumerKey())
                    .consumerToken(context.isDryrun() ? "**UNDEFINED**" : sdkman.getConsumerToken())
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
                MajorReleaseSdkmanCommand.builder(context.asImmutable())
                    .connectTimeout(sdkman.getConnectTimeout())
                    .readTimeout(sdkman.getReadTimeout())
                    .consumerKey(context.isDryrun() ? "**UNDEFINED**" : sdkman.getConsumerKey())
                    .consumerToken(context.isDryrun() ? "**UNDEFINED**" : sdkman.getConsumerToken())
                    .candidate(candidate)
                    .version(context.getModel().getProject().getVersion())
                    .platforms(platforms)
                    .releaseNotesUrl(releaseNotesUrl)
                    .dryrun(context.isDryrun())
                    .build()
                    .execute();
            } else {
                context.getLogger().info(RB.$("sdkman.release.announce.minor"), candidate);
                MinorReleaseSdkmanCommand.builder(context.asImmutable())
                    .connectTimeout(sdkman.getConnectTimeout())
                    .readTimeout(sdkman.getReadTimeout())
                    .consumerKey(context.isDryrun() ? "**UNDEFINED**" : sdkman.getConsumerKey())
                    .consumerToken(context.isDryrun() ? "**UNDEFINED**" : sdkman.getConsumerToken())
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
        return (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JLINK ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.BINARY) &&
            !isTrue(distribution.getExtraProperties().get("skipSdkman"));
    }
}
