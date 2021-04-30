/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Sdkman;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SdkmanAnnouncer implements Announcer {
    private final JReleaserContext context;

    SdkmanAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.Sdkman.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getSdkman().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Sdkman sdkman = context.getModel().getAnnounce().getSdkman();

        Map<String, String> platforms = new LinkedHashMap<>();
        // collect artifacts by supported SDKMAN! platform
        for (Distribution distribution : context.getModel().getActiveDistributions()) {
            if (!isDistributionSupported(distribution)) continue;
            for (Artifact artifact : distribution.getArtifacts()) {
                // only zips are supported
                if (!artifact.getPath().endsWith(".zip")) {
                    context.getLogger().debug("Artifact {} is not suitable for Sdkman publication. Skipping.",
                        artifact.getEffectivePath(context, distribution).getFileName());
                    continue;
                }

                String platform = mapPlatform(artifact.getPlatform());
                String url = artifactUrl(distribution, artifact);
                if (platforms.containsKey(platform)) {
                    context.getLogger().warn("Platform {}: {} will replace {}", platform, url, platforms.get(platform));
                }
                platforms.put(platform, url);
            }
        }

        if (platforms.isEmpty()) {
            context.getLogger().warn("No suitable artifacts were found. Skipping");
            return;
        }

        try {
            String candidate = isNotBlank(sdkman.getCandidate()) ? sdkman.getCandidate().trim() : context.getModel().getProject().getName();
            String releaseNotesUrl = context.getModel().getRelease().getGitService().getResolvedReleaseNotesUrl(context.getModel());

            if (sdkman.isMajor()) {
                context.getLogger().info("Announcing major release of '{}' candidate", candidate);
                MajorReleaseSdkmanCommand.builder(context.getLogger())
                    .connectTimeout(sdkman.getConnectTimeout())
                    .readTimeout(sdkman.getReadTimeout())
                    .consumerKey(sdkman.getResolvedConsumerKey())
                    .consumerToken(sdkman.getResolvedConsumerToken())
                    .candidate(candidate)
                    .version(context.getModel().getProject().getVersion())
                    .platforms(platforms)
                    .releaseNotesUrl(releaseNotesUrl)
                    .dryrun(context.isDryrun())
                    .build()
                    .execute();
            } else {
                context.getLogger().info("Announcing minor release of '{}' candidate", candidate);
                MinorReleaseSdkmanCommand.builder(context.getLogger())
                    .connectTimeout(sdkman.getConnectTimeout())
                    .readTimeout(sdkman.getReadTimeout())
                    .consumerKey(sdkman.getResolvedConsumerKey())
                    .consumerToken(sdkman.getResolvedConsumerToken())
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
        return distribution.getType() == Distribution.DistributionType.JAVA_BINARY &&
            !distribution.getExtraProperties().containsKey("sdkmanSkip");
    }

    private String mapPlatform(String platform) {
        /*
           SDKMAN! supports the following platform mappings
           - LINUX_64
           - LINUX_32
           - LINUX_ARM32
           - LINUX_ARM64
           - MAC_OSX
           - MAC_ARM64
           - WINDOWS_64
           - UNIVERSAL
         */

        if (isBlank(platform)) {
            return "UNIVERSAL";
        }
        if (platform.contains("mac") || platform.contains("osx")) {
            return platform.contains("arm") ? "MAC_ARM64" : "MAC_OSX";
        } else if (platform.contains("win")) {
            return "WINDOWS_64";
        } else if (platform.contains("linux")) {
            if (platform.contains("x86_32")) return "LINUX_32";
            if (platform.contains("x86_64")) return "LINUX_64";
            // os plugin can't detect arm32 vs arm64 :'(
            if (platform.contains("arm")) return "LINUX_ARM32";
            return "LINUX_32";
        }

        return null;
    }

    private String artifactUrl(Distribution distribution, Artifact artifact) {
        Map<String, Object> newProps = context.props();
        newProps.put("artifactFileName", artifact.getEffectivePath(context, distribution).getFileName().toString());
        return applyTemplate(context.getModel().getRelease().getGitService().getDownloadUrlFormat(), newProps, "downloadUrl");
    }
}
