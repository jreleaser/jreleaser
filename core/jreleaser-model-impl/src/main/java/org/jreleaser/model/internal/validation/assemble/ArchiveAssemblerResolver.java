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
package org.jreleaser.model.internal.validation.assemble;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.ArchiveAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.jreleaser.model.Constants.KEY_PLATFORM;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public final class ArchiveAssemblerResolver {
    private ArchiveAssemblerResolver() {
        // noop
    }

    public static void resolveArchiveOutputs(JReleaserContext context, Errors errors) {
        List<ArchiveAssembler> activeArchives = context.getModel().getAssemble().getActiveArchives();
        if (!activeArchives.isEmpty()) context.getLogger().debug("assemble.archive");

        for (ArchiveAssembler archive : activeArchives) {
            if (archive.isExported()) resolveArchiveOutputs(context, archive, errors);
        }
    }

    public static void resolveArchiveOutputs(JReleaserContext context, ArchiveAssembler assembler, Errors errors) {
        if (!assembler.getMatrix().isEmpty()) {
            for (Map<String, String> matrixRow : assembler.getMatrix().resolve()) {
                if (matrixRow.containsKey(KEY_PLATFORM)) {
                    String srcPlatform = matrixRow.get(KEY_PLATFORM);
                    if (context.isPlatformSelected(srcPlatform, assembler.getPlatform())) {
                        resolveArchiveOutput(context, assembler, matrixRow, srcPlatform, errors);
                    }
                } else {
                    resolveArchiveOutput(context, assembler, matrixRow, assembler.isAttachPlatform() ? PlatformUtils.getCurrentFull() : "", errors);
                }
            }
        } else {
            resolveArchiveOutput(context, assembler, emptyMap(), assembler.isAttachPlatform() ? PlatformUtils.getCurrentFull() : "", errors);
        }
    }

    private static void resolveArchiveOutput(JReleaserContext context, ArchiveAssembler assembler, Map<String, String> matrix, String platform, Errors errors) {
        if (assembler.isAttachPlatform() &&
            !context.isPlatformSelected(PlatformUtils.getCurrentFull())) return;

        Path baseOutputDirectory = context.getAssembleDirectory()
            .resolve(assembler.getName())
            .resolve(assembler.getType());


        String archiveName = assembler.getResolvedArchiveName(context, matrix);

        for (org.jreleaser.model.Archive.Format format : assembler.getFormats()) {
            String skipKey = "skip" + capitalize(format.formatted());
            if (assembler.extraPropertyIsTrue(skipKey) || isTrue(matrix.get(skipKey))) continue;

            Path path = baseOutputDirectory
                .resolve(archiveName + "." + format.extension())
                .toAbsolutePath();

            if (!Files.exists(path)) {
                errors.assembly(RB.$("validation_missing_assembly",
                    assembler.getType(), assembler.getName(), assembler.getName()));
            } else {
                Artifact artifact = Artifact.of(path, platform);
                artifact.resolveActiveAndSelected(context);
                artifact.setExtraProperties(assembler.getExtraProperties());
                matrix.keySet().stream()
                    .filter(k -> k.startsWith("skip"))
                    .forEach(k -> artifact.addExtraProperty(k, matrix.get(k)));
                assembler.addOutput(artifact);
            }
        }
    }
}
