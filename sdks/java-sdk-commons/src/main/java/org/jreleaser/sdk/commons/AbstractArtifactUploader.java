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
package org.jreleaser.sdk.commons;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.ExtraProperties;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Uploader;
import org.jreleaser.model.uploader.spi.ArtifactUploader;
import org.jreleaser.model.util.Artifacts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.model.Signing.KEY_SKIP_SIGNING;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class AbstractArtifactUploader<U extends Uploader> implements ArtifactUploader<U> {
    protected final JReleaserContext context;

    protected AbstractArtifactUploader(JReleaserContext context) {
        this.context = context;
    }

    protected List<Artifact> collectArtifacts() {
        List<Artifact> artifacts = new ArrayList<>();

        List<String> keys = getUploader().resolveSkipKeys();

        if (getUploader().isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActive()) continue;
                if (isSkip(artifact, keys)) continue;
                Path path = artifact.getEffectivePath(context);
                if (Files.exists(path) && 0 != path.toFile().length()) {
                    artifacts.add(artifact);
                }
            }
        }

        if (getUploader().isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                if (!context.isDistributionIncluded(distribution) || isSkip(distribution, keys)) continue;
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActive()) continue;
                    if (isSkip(artifact, keys)) continue;
                    Path path = artifact.getEffectivePath(context);
                    if (Files.exists(path) && 0 != path.toFile().length()) {
                        String platform = artifact.getPlatform();
                        String platformReplaced = distribution.getPlatform().applyReplacements(platform);
                        if (isNotBlank(platformReplaced)) {
                            artifact.getExtraProperties().put("platformReplaced", platformReplaced);
                        }
                        artifacts.add(artifact);
                    }
                }
            }
        }

        if (getUploader().isSignatures() && context.getModel().getSigning().isEnabled()) {
            String extension = context.getModel().getSigning().isArmored() ? ".asc" : ".sig";

            List<Artifact> signatures = new ArrayList<>();
            for (Artifact artifact : artifacts) {
                if (artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                Path signaturePath = context.getSignaturesDirectory()
                    .resolve(artifact.getEffectivePath(context).getFileName() + extension);
                if (Files.exists(signaturePath) && 0 != signaturePath.toFile().length()) {
                    signatures.add(Artifact.of(signaturePath));
                }
            }

            artifacts.addAll(signatures);
        }

        return artifacts;
    }

    private boolean isSkip(ExtraProperties props, List<String> keys) {
        for (String key : keys) {
            if (props.extraPropertyIsTrue(key)) {
                return true;
            }
        }
        return false;
    }
}
