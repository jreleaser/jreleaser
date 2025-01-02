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
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.JpackageAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.util.Errors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.jreleaser.util.FileUtils.listFilesAndProcess;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public final class JpackageAssemblerResolver {
    private JpackageAssemblerResolver() {
        // noop
    }

    public static void resolveJpackageOutputs(JReleaserContext context, Errors errors) {
        List<JpackageAssembler> activeJpackages = context.getModel().getAssemble().getActiveJpackages();
        if (!activeJpackages.isEmpty()) context.getLogger().debug("assemble.jpackage");

        for (JpackageAssembler jpackage : activeJpackages) {
            if (jpackage.isExported()) resolveJpackageOutputs(context, jpackage, errors);
        }
    }

    private static void resolveJpackageOutputs(JReleaserContext context, JpackageAssembler assembler, Errors errors) {
        Path baseOutputDirectory = context.getAssembleDirectory()
            .resolve(assembler.getName())
            .resolve(assembler.getType());

        Artifact jdk = assembler.getResolvedPlatformPackager().getJdk();
        if (!context.isPlatformSelected(jdk)) return;

        JpackageAssembler.PlatformPackager packager = assembler.getResolvedPlatformPackager();
        if (!packager.getJdk().isActiveAndSelected()) return;
        String platform = jdk.getPlatform();

        for (String type : packager.getTypes()) {
            try {
                Optional<Path> file = listFilesAndProcess(baseOutputDirectory, files ->
                    files.filter(path -> path.getFileName().toString().endsWith(type))
                        .findFirst().orElse(null));

                if (!file.isPresent()) {
                    errors.assembly(RB.$("validation_missing_assembly",
                        assembler.getType(), assembler.getName(), assembler.getName()));
                } else {
                    Artifact artifact = Artifact.of(file.get(), platform);
                    artifact.resolveActiveAndSelected(context);
                    artifact.setExtraProperties(assembler.getExtraProperties());
                    assembler.addOutput(artifact);
                }
            } catch (IOException e) {
                throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
            }
        }
    }
}
