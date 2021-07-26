/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.tools;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Sdkman;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.sdk.sdkman.MajorReleaseSdkmanCommand;
import org.jreleaser.sdk.sdkman.MinorReleaseSdkmanCommand;
import org.jreleaser.sdk.sdkman.SdkmanException;
import org.jreleaser.util.Constants;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public class SdkmanToolProcessor extends AbstractToolProcessor<Sdkman> {
    public SdkmanToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        // noop
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        // noop
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        org.jreleaser.model.Sdkman sdkman = distribution.getSdkman();

        Map<String, String> platforms = new LinkedHashMap<>();
        // collect artifacts by supported SDKMAN! platform
        for (Artifact artifact : distribution.getArtifacts()) {
            // only zips are supported
            if (!artifact.getPath().endsWith(".zip")) {
                context.getLogger().debug("Artifact {} is not suitable for Sdkman publication. Skipping.",
                    artifact.getEffectivePath(context, distribution).getFileName());
                continue;
            }

            if (isTrue(artifact.getExtraProperties().get("skipSdkman"))) {
                context.getLogger().debug("Artifact {} is explicitly skipped.",
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

        try {
            String candidate = isNotBlank(sdkman.getCandidate()) ? sdkman.getCandidate().trim() : context.getModel().getProject().getName();
            String releaseNotesUrl = applyTemplate(sdkman.getReleaseNotesUrl(), props);

            switch (sdkman.getCommand()) {
                case MAJOR:
                    context.getLogger().info("Announcing major release of '{}' candidate", candidate);
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
                    break;
                case MINOR:
                    context.getLogger().info("Announcing minor release of '{}' candidate", candidate);
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
                    break;
            }
        } catch (SdkmanException e) {
            throw new ToolProcessingException(e);
        }
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        props.put(Constants.KEY_SDKMAN_CANDIDATE, tool.getCandidate());
        props.put(Constants.KEY_SDKMAN_RELEASE_NOTES_URL, applyTemplate(tool.getReleaseNotesUrl(), props));
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
        return applyTemplate(context.getModel().getRelease().getGitService().getDownloadUrl(), newProps, "downloadUrl");
    }
}
