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
package org.jreleaser.model.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Archive;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public abstract class ArchiveResolver extends Validator {
    public static void resolveArchiveOutputs(JReleaserContext context, Errors errors) {
        context.getLogger().debug("assemble.archive");

        for (Archive archive : context.getModel().getAssemble().getActiveArchives()) {
            if (archive.isExported()) resolveArchiveOutputs(context, archive, errors);
        }
    }

    private static void resolveArchiveOutputs(JReleaserContext context, Archive archive, Errors errors) {
        if (archive.isAttachPlatform() &&
            !context.isPlatformSelected(PlatformUtils.getCurrentFull())) return;

        Path baseOutputDirectory = context.getAssembleDirectory()
            .resolve(archive.getName())
            .resolve(archive.getType());

        String archiveName = archive.getResolvedArchiveName(context);

        for (Archive.Format format : archive.getFormats()) {
            Path path = baseOutputDirectory
                .resolve(archiveName + "." + format.extension())
                .toAbsolutePath();

            if (!Files.exists(path)) {
                errors.assembly(RB.$("validation_missing_assembly",
                    archive.getType(), archive.getName(), archive.getName()));
            } else {
                Artifact artifact = Artifact.of(path, archive.isAttachPlatform() ? PlatformUtils.getCurrentFull() : "");
                artifact.setExtraProperties(archive.getExtraProperties());
                artifact.activate();
                archive.addOutput(artifact);
            }
        }
    }
}
