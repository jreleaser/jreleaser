/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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

    private static void resolveArchiveOutputs(JReleaserContext context, ArchiveAssembler assembler, Errors errors) {
        if (assembler.isAttachPlatform() &&
            !context.isPlatformSelected(PlatformUtils.getCurrentFull())) return;

        Path baseOutputDirectory = context.getAssembleDirectory()
            .resolve(assembler.getName())
            .resolve(assembler.getType());

        String archiveName = assembler.getResolvedArchiveName(context);

        for (org.jreleaser.model.Archive.Format format : assembler.getFormats()) {
            Path path = baseOutputDirectory
                .resolve(archiveName + "." + format.extension())
                .toAbsolutePath();

            if (!Files.exists(path)) {
                errors.assembly(RB.$("validation_missing_assembly",
                    assembler.getType(), assembler.getName(), assembler.getName()));
            } else {
                Artifact artifact = Artifact.of(path, assembler.isAttachPlatform() ? PlatformUtils.getCurrentFull() : "");
                artifact.resolveActiveAndSelected(context);
                artifact.setExtraProperties(assembler.getExtraProperties());
                assembler.addOutput(artifact);
            }
        }
    }
}
