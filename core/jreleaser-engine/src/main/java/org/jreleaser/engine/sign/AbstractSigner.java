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
package org.jreleaser.engine.sign;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.sbom.SbomCataloger;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.signing.SigningTool;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessorHelper;
import org.jreleaser.sdk.signing.SigningUtils;
import org.jreleaser.util.Algorithm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.jreleaser.model.api.signing.Signing.KEY_SKIP_SIGNING;

/**
 * @author Andres Almiray
 * @since 1.22.0
 */
public abstract class AbstractSigner {
    protected final JReleaserContext context;
    protected final SigningTool tool;

    protected AbstractSigner(JReleaserContext context, SigningTool tool) {
        this.context = context;
        this.tool = tool;
    }

    protected List<SigningUtils.FilePair> collectArtifacts(Predicate<SigningUtils.FilePair> validator) {
        return collectArtifacts(false, validator);
    }

    protected List<SigningUtils.FilePair> collectArtifacts(boolean forceSign, Predicate<SigningUtils.FilePair> validator) {
        List<SigningUtils.FilePair> files = new ArrayList<>();

        Path signaturesDirectory = context.getSignaturesDirectory();

        if (tool.isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActiveAndSelected() || artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING) ||
                    artifact.isOptional(context) && !artifact.resolvedPathExists()) continue;
                Path input = artifact.getEffectivePath(context);
                Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(tool.getSignatureExtension()));
                SigningUtils.FilePair pair = new SigningUtils.FilePair(input, output);
                if (!forceSign) pair.setValid(validator.test(pair));
                files.add(pair);
            }
        }

        if (tool.isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                if (distribution.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActiveAndSelected() || artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                    Path input = artifact.getEffectivePath(context, distribution);
                    if (artifact.isOptional(context) && !artifact.resolvedPathExists()) continue;
                    Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(tool.getSignatureExtension()));
                    SigningUtils.FilePair pair = new SigningUtils.FilePair(input, output);
                    if (!forceSign) pair.setValid(validator.test(pair));
                    files.add(pair);
                }
            }
        }

        if (tool.isCatalogs()) {
            List<? extends SbomCataloger<?>> catalogers = context.getModel().getCatalog().getSbom().findAllActiveSbomCatalogers();
            for (SbomCataloger<?> cataloger : catalogers) {
                if (!cataloger.getPack().isEnabled()) continue;
                for (Artifact artifact : SbomCatalogerProcessorHelper.resolveArtifacts(context, cataloger)) {
                    Path input = artifact.getEffectivePath(context);
                    Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(tool.getSignatureExtension()));
                    SigningUtils.FilePair pair = new SigningUtils.FilePair(input, output);
                    if (!forceSign) pair.setValid(validator.test(pair));
                    files.add(pair);
                }
            }
        }

        if (tool.isChecksums()) {
            for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                Path checksums = context.getChecksumsDirectory()
                    .resolve(context.getModel().getChecksum().getResolvedName(context, algorithm));
                if (Files.exists(checksums)) {
                    Path output = signaturesDirectory.resolve(checksums.getFileName().toString().concat(tool.getSignatureExtension()));
                    SigningUtils.FilePair pair = new SigningUtils.FilePair(checksums, output);
                    if (!forceSign) pair.setValid(validator.test(pair));
                    files.add(pair);
                }
            }
        }

        return files;
    }
}
