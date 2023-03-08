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
package org.jreleaser.assemblers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Archive;
import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.ArchiveAssembler;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class ArchiveAssemblerProcessor extends AbstractAssemblerProcessor<org.jreleaser.model.api.assemble.ArchiveAssembler, ArchiveAssembler> {
    public ArchiveAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doAssemble(TemplateContext props) throws AssemblerProcessingException {
        Path assembleDirectory = props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        String archiveName = assembler.getResolvedArchiveName(context);

        Path workDirectory = assembleDirectory.resolve(WORK_DIRECTORY);
        Path archiveDirectory = workDirectory.resolve(archiveName);

        try {
            FileUtils.deleteFiles(workDirectory);
            Files.createDirectories(archiveDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_delete_archive", archiveName), e);
        }

        // copy templates
        copyTemplates(context, props, archiveDirectory);

        // copy fileSets
        context.getLogger().debug(RB.$("assembler.copy.files"), context.relativizeToBasedir(archiveDirectory));
        copyArtifacts(context, archiveDirectory, PlatformUtils.getCurrentFull(), assembler.isAttachPlatform());
        copyFiles(context, archiveDirectory);
        copyFileSets(context, archiveDirectory);

        // run archive x format
        for (Archive.Format format : assembler.getFormats()) {
            archive(workDirectory, assembleDirectory, archiveName, format);
        }
    }

    private void archive(Path workDirectory, Path assembleDirectory, String archiveName, Archive.Format format) throws AssemblerProcessingException {
        String finalArchiveName = archiveName + "." + format.extension();
        context.getLogger().info("- {}", finalArchiveName);

        try {
            Path archiveFile = assembleDirectory.resolve(finalArchiveName);
            FileUtils.packArchive(workDirectory, archiveFile, assembler.getOptions().toOptions());
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }
}
