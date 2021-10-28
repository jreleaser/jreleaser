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
package org.jreleaser.assemblers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Archive;
import org.jreleaser.model.FileSet;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.assembler.spi.AssemblerProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class ArchiveAssemblerProcessor extends AbstractAssemblerProcessor<Archive> {
    public ArchiveAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doAssemble(Map<String, Object> props) throws AssemblerProcessingException {
        Path assembleDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        String archiveName = assembler.getResolvedArchiveName(context);

        Path workDirectory = assembleDirectory.resolve("work");
        Path archiveDirectory = workDirectory.resolve(archiveName);

        try {
            FileUtils.deleteFiles(workDirectory);
            Files.createDirectories(archiveDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_delete_archive", archiveName), e);
        }
        // copy fileSets
        context.getLogger().debug(RB.$("assembler.copy.files"), context.relativizeToBasedir(archiveDirectory));
        copyFiles(context, archiveDirectory);

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
            switch (format) {
                case ZIP:
                    FileUtils.zip(workDirectory, archiveFile);
                    break;
                case TAR:
                    FileUtils.tar(workDirectory, archiveFile);
                    break;
                case TGZ:
                case TAR_GZ:
                    FileUtils.tgz(workDirectory, archiveFile);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private void copyFiles(JReleaserContext context, Path destination) throws AssemblerProcessingException {
        try {
            for (FileSet fileSet : assembler.getFileSets()) {
                Path src = context.getBasedir().resolve(fileSet.getInput());
                Path dest = destination;

                if (isNotBlank(fileSet.getOutput())) {
                    dest = destination.resolve(fileSet.getOutput());
                }

                if (!FileUtils.copyFilesRecursive(context.getLogger(), src, dest, filter(fileSet))) {
                    throw new IOException(RB.$("ERROR_repository_copy_files",
                        context.relativizeToBasedir(src)));
                }
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_copying_files"), e);
        }
    }

    private Predicate<Path> filter(final FileSet fileSet) {
        return new Predicate<Path>() {
            private final Set<Pattern> includes = fileSet.getIncludes().stream()
                .map(StringUtils::toSafePattern)
                .collect(Collectors.toSet());

            private final Set<Pattern> excludes = fileSet.getExcludes().stream()
                .map(StringUtils::toSafePattern)
                .collect(Collectors.toSet());

            @Override
            public boolean test(Path path) {
                // filter logic is inverted, `true` means it will be skipped.
                if (!includes.isEmpty()) {
                    for (Pattern pattern : includes) {
                        if (!pattern.matcher(path.getFileName().toString()).matches()) return true;
                    }
                }
                if (!excludes.isEmpty()) {
                    for (Pattern pattern : excludes) {
                        if (pattern.matcher(path.getFileName().toString()).matches()) return true;
                    }
                }

                return false;
            }
        };
    }
}
