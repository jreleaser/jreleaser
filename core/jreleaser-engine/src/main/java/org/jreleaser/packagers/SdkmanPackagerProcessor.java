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
package org.jreleaser.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Sdkman;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.SdkmanPackager;
import org.jreleaser.model.internal.util.SdkmanHelper;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.sdkman.MajorReleaseSdkmanCommand;
import org.jreleaser.sdk.sdkman.MinorReleaseSdkmanCommand;
import org.jreleaser.sdk.sdkman.SdkmanException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.model.Constants.KEY_SDKMAN_CANDIDATE;
import static org.jreleaser.model.Constants.KEY_SDKMAN_RELEASE_NOTES_URL;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public class SdkmanPackagerProcessor extends AbstractPackagerProcessor<SdkmanPackager> {
    public SdkmanPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        // noop
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        // noop
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        SdkmanPackager sdkman = distribution.getSdkman();

        Map<String, String> platforms = new LinkedHashMap<>();
        // collect artifacts by supported SDKMAN! platform
        SdkmanHelper.collectArtifacts(context, distribution, platforms);

        try {
            String candidate = isNotBlank(sdkman.getCandidate()) ? sdkman.getCandidate().trim() : context.getModel().getProject().getName();
            String releaseNotesUrl = resolveTemplate(sdkman.getReleaseNotesUrl(), props);

            if (sdkman.getCommand() == Sdkman.Command.MAJOR) {
                context.getLogger().info(RB.$("sdkman.publish.major"), candidate);
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
                    .skipAnnounce(false)
                    .build()
                    .execute();
            } else if (sdkman.getCommand() == Sdkman.Command.MINOR) {
                context.getLogger().info(RB.$("sdkman.publish.minor"), candidate);
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
                    .skipAnnounce(false)
                    .build()
                    .execute();
            }

            sdkman.setPublished(true);
        } catch (SdkmanException e) {
            throw new PackagerProcessingException(e);
        }
    }

    @Override
    protected void fillPackagerProperties(TemplateContext props, Distribution distribution) {
        props.set(KEY_SDKMAN_CANDIDATE, packager.getCandidate());
        props.set(KEY_SDKMAN_RELEASE_NOTES_URL, resolveTemplate(packager.getReleaseNotesUrl(), props));
    }
}
