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
package org.jreleaser.model.internal.util;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.util.PlatformUtils;

import java.util.Map;

import static org.jreleaser.model.api.packagers.SdkmanPackager.SKIP_SDKMAN;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public final class SdkmanHelper {
    private static final String UNIVERSAL = "UNIVERSAL";
    private static final String MAC_ARM_64 = "MAC_ARM64";
    private static final String MAC_OSX = "MAC_OSX";
    private static final String LINUX_32 = "LINUX_32";
    private static final String LINUX_64 = "LINUX_64";
    private static final String LINUX_ARM_32 = "LINUX_ARM32";
    private static final String LINUX_ARM_64 = "LINUX_ARM64";
    private static final String WINDOWS_64 = "WINDOWS_64";

    private SdkmanHelper() {
        // noop
    }

    public static void collectArtifacts(JReleaserContext context, Distribution distribution, Map<String, String> platforms) {
        for (Artifact artifact : distribution.getArtifacts()) {
            if (!artifact.isActiveAndSelected()) continue;
            // only zips are supported
            if (!artifact.getPath().endsWith(ZIP.extension())) {
                context.getLogger().debug(RB.$("sdkman.no.artifacts.match"),
                    artifact.getEffectivePath(context, distribution).getFileName());
                continue;
            }

            if (isTrue(artifact.getExtraProperties().get(SKIP_SDKMAN))) {
                context.getLogger().debug(RB.$("sdkman.artifact.explicit.skip"),
                    artifact.getEffectivePath(context, distribution).getFileName());
                continue;
            }

            if (artifact.isOptional(context) && !artifact.resolvedPathExists()) {
                context.getLogger().debug(RB.$("sdkman.artifact.optional"),
                    artifact.getEffectivePath(context, distribution).getFileName());
                continue;
            }

            String platform = mapPlatform(artifact.getPlatform());
            if (isBlank(platform)) {
                context.getLogger().warn(RB.$("sdkman.platform.unsupported"), artifact.getPlatform());
                continue;
            }

            String url = artifactUrl(context, distribution, artifact);
            if (platforms.containsKey(platform)) {
                context.getLogger().warn(RB.$("sdkman.platform.replacement"), platform, url, platforms.get(platform));
            }
            platforms.put(platform, url);
        }
    }

    private static String mapPlatform(String platform) {
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
            return UNIVERSAL;
        }
        if (PlatformUtils.isMac(platform) || platform.contains("mac")) {
            return PlatformUtils.isArm(platform) ? MAC_ARM_64 : MAC_OSX;
        } else if (PlatformUtils.isWindows(platform)) {
            return WINDOWS_64;
        } else if (PlatformUtils.isLinux(platform)) {
            if (PlatformUtils.isIntel32(platform)) return LINUX_32;
            if (PlatformUtils.isIntel64(platform)) return LINUX_64;
            if (PlatformUtils.isArm32(platform)) return LINUX_ARM_32;
            if (PlatformUtils.isArm64(platform)) return LINUX_ARM_64;
            return LINUX_32;
        }

        return null;
    }

    private static String artifactUrl(JReleaserContext context, Distribution distribution, Artifact artifact) {
        return Artifacts.resolveDownloadUrl(context, context.getModel().getAnnounce().getSdkman(), distribution, artifact);
    }
}
