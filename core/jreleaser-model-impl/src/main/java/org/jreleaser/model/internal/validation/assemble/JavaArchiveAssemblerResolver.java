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
import org.jreleaser.model.internal.assemble.JavaArchiveAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public final class JavaArchiveAssemblerResolver {
    private JavaArchiveAssemblerResolver() {
        // noop
    }

    public static void resolveJavaArchiveOutputs(JReleaserContext context, Errors errors) {
        List<JavaArchiveAssembler> activeArchives = context.getModel().getAssemble().getActiveJavaArchives();
        if (!activeArchives.isEmpty()) context.getLogger().debug("assemble.java-archive");

        for (JavaArchiveAssembler archive : activeArchives) {
            if (archive.isExported()) resolveJavaArchiveOutputs(context, archive, errors);
        }
    }

    public static void resolveJavaArchiveOutputs(JReleaserContext context, JavaArchiveAssembler assembler, Errors errors) {
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
                Artifact artifact = Artifact.of(path);
                artifact.resolveActiveAndSelected(context);
                artifact.setExtraProperties(assembler.getExtraProperties());
                assembler.addOutput(artifact);
            }
        }
    }
}
