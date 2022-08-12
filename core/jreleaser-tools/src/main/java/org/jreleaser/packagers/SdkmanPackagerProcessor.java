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
package org.jreleaser.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Sdkman;
import org.jreleaser.model.packager.spi.PackagerProcessingException;
import org.jreleaser.model.util.SdkmanHelper;
import org.jreleaser.sdk.sdkman.MajorReleaseSdkmanCommand;
import org.jreleaser.sdk.sdkman.MinorReleaseSdkmanCommand;
import org.jreleaser.sdk.sdkman.SdkmanException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.Constants.KEY_SDKMAN_CANDIDATE;
import static org.jreleaser.util.Constants.KEY_SDKMAN_RELEASE_NOTES_URL;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public class SdkmanPackagerProcessor extends AbstractPackagerProcessor<Sdkman> {
    public SdkmanPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        // noop
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        // noop
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        org.jreleaser.model.Sdkman sdkman = distribution.getSdkman();

        Map<String, String> platforms = new LinkedHashMap<>();
        // collect artifacts by supported SDKMAN! platform
        SdkmanHelper.collectArtifacts(context, distribution, platforms);

        try {
            String candidate = isNotBlank(sdkman.getCandidate()) ? sdkman.getCandidate().trim() : context.getModel().getProject().getName();
            String releaseNotesUrl = resolveTemplate(sdkman.getReleaseNotesUrl(), props);

            switch (sdkman.getCommand()) {
                case MAJOR:
                    context.getLogger().info(RB.$("sdkman.publish.major"), candidate);
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
                        .skipAnnounce(true)
                        .build()
                        .execute();
                    break;
                case MINOR:
                    context.getLogger().info(RB.$("sdkman.publish.minor"), candidate);
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
                        .skipAnnounce(true)
                        .build()
                        .execute();
                    break;
            }

            sdkman.mutate(() -> sdkman.setPublished(true));
        } catch (SdkmanException e) {
            throw new PackagerProcessingException(e);
        }
    }

    @Override
    protected void fillPackagerProperties(Map<String, Object> props, Distribution distribution, ProcessingStep processingStep) throws PackagerProcessingException {
        props.put(KEY_SDKMAN_CANDIDATE, packager.getCandidate());
        props.put(KEY_SDKMAN_RELEASE_NOTES_URL, resolveTemplate(packager.getReleaseNotesUrl(), props));
    }
}
