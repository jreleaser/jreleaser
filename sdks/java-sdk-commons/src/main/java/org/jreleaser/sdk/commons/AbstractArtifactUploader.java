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
package org.jreleaser.sdk.commons;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Artifactory;
import org.jreleaser.model.Checksum;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.ExtraProperties;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Signing;
import org.jreleaser.model.Uploader;
import org.jreleaser.model.uploader.spi.ArtifactUploader;
import org.jreleaser.model.util.Artifacts;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.Constants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.model.Checksum.INDIVIDUAL_CHECKSUM;
import static org.jreleaser.model.Checksum.KEY_SKIP_CHECKSUM;
import static org.jreleaser.model.Signing.KEY_SKIP_SIGNING;
import static org.jreleaser.util.Constants.KEY_PLATFORM_REPLACED;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

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
        Checksum checksum = context.getModel().getChecksum();
        boolean uploadChecksums = getUploader().isChecksums() && !(getUploader() instanceof Artifactory);

        if (getUploader().isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActive()) continue;
                Path path = artifact.getEffectivePath(context);
                if (isSkip(artifact, keys)) continue;
                if (Files.exists(path) && 0 != path.toFile().length()) {
                    artifacts.add(artifact);
                    if (uploadChecksums && isIndividual(context, artifact) &&
                        !artifact.extraPropertyIsTrue(KEY_SKIP_CHECKSUM)) {
                        for (Algorithm algorithm : checksum.getAlgorithms()) {
                            artifacts.add(Artifact.of(context.getChecksumsDirectory()
                                .resolve(path.getFileName() + "." + algorithm.formatted())));
                        }
                    }
                }
            }
        }

        if (getUploader().isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                if (isSkip(distribution, keys)) continue;
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActive()) continue;
                    Path path = artifact.getEffectivePath(context, distribution);
                    if (isSkip(artifact, keys)) continue;
                    if (Files.exists(path) && 0 != path.toFile().length()) {
                        String platform = artifact.getPlatform();
                        String platformReplaced = distribution.getPlatform().applyReplacements(platform);
                        if (isNotBlank(platformReplaced)) {
                            artifact.mutate(() -> artifact.getExtraProperties().put(KEY_PLATFORM_REPLACED, platformReplaced));
                        }
                        artifacts.add(artifact);
                        if (uploadChecksums && isIndividual(context, distribution, artifact)) {
                            for (Algorithm algorithm : checksum.getAlgorithms()) {
                                artifacts.add(Artifact.of(context.getChecksumsDirectory()
                                    .resolve(distribution.getName())
                                    .resolve(path.getFileName() + "." + algorithm.formatted())));
                            }
                        }
                    }
                }
            }
        }

        if (uploadChecksums) {
            for (Algorithm algorithm : checksum.getAlgorithms()) {
                Path checksums = context.getChecksumsDirectory()
                    .resolve(checksum.getResolvedName(context, algorithm));
                if (Files.exists(checksums)) {
                    artifacts.add(Artifact.of(checksums));
                }
            }
        }

        Signing signing = context.getModel().getSigning();
        if (getUploader().isSignatures() && signing.isEnabled()) {
            String extension = signing.getSignatureExtension();

            List<Artifact> signatures = new ArrayList<>();
            for (Artifact artifact : artifacts) {
                if (artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                Path signaturePath = context.getSignaturesDirectory()
                    .resolve(artifact.getEffectivePath(context).getFileName() + extension);
                if (Files.exists(signaturePath) && 0 != signaturePath.toFile().length()) {
                    signatures.add(Artifact.of(signaturePath, artifact.getExtraProperties()));
                }
            }
            if (!signatures.isEmpty() && signing.getMode() == Signing.Mode.COSIGN) {
                Path publicKeyFile = signing.getCosign().getResolvedPublicKeyFilePath(context);
                signatures.add(Artifact.of(publicKeyFile));
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

    private boolean isIndividual(JReleaserContext context, Artifact artifact) {
        if (artifact.getExtraProperties().containsKey(INDIVIDUAL_CHECKSUM)) {
            return isTrue(artifact.getExtraProperties().get(INDIVIDUAL_CHECKSUM));
        }
        return context.getModel().getChecksum().isIndividual();
    }

    private boolean isIndividual(JReleaserContext context, Distribution distribution, Artifact artifact) {
        if (artifact.getExtraProperties().containsKey(INDIVIDUAL_CHECKSUM)) {
            return isTrue(artifact.getExtraProperties().get(INDIVIDUAL_CHECKSUM));
        }
        if (distribution.getExtraProperties().containsKey(INDIVIDUAL_CHECKSUM)) {
            return isTrue(distribution.getExtraProperties().get(INDIVIDUAL_CHECKSUM));
        }
        return context.getModel().getChecksum().isIndividual();
    }
}
