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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.JavaArchiveAssembler;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.join;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_EXECUTABLE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_JAR;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_OPTIONS;
import static org.jreleaser.mustache.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public class JavaArchiveAssemblerProcessor extends AbstractAssemblerProcessor<org.jreleaser.model.api.assemble.JavaArchiveAssembler, JavaArchiveAssembler> {
    public JavaArchiveAssemblerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doAssemble(TemplateContext props) throws AssemblerProcessingException {
        Path assembleDirectory = props.get(KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
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

        // copy files
        context.getLogger().debug(RB.$("assembler.copy.files"), context.relativizeToBasedir(archiveDirectory));
        copyArtifacts(context, archiveDirectory, PlatformUtils.getCurrentFull(), false);
        copyFiles(context, archiveDirectory);
        copyFileSets(context, archiveDirectory);

        // copy jars
        Path jarsDirectory = archiveDirectory.resolve("lib");
        context.getLogger().debug(RB.$("assembler.copy.jars"), context.relativizeToBasedir(jarsDirectory));
        copyJars(context, assembler, jarsDirectory);

        // run archive x format
        for (Archive.Format format : assembler.getFormats()) {
            archive(workDirectory, assembleDirectory, archiveName, format);
        }
    }

    @Override
    protected void fillAssemblerProperties(TemplateContext props) {
        super.fillAssemblerProperties(props);

        if (isNotBlank(assembler.getMainJar().getPath())) {
            props.set(KEY_DISTRIBUTION_JAVA_MAIN_JAR, assembler.getMainJar().getEffectivePath(context, assembler)
                .getFileName());
        } else {
            props.set(KEY_DISTRIBUTION_JAVA_MAIN_JAR, "");
        }
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, assembler.getJava().getMainClass());
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_MODULE, assembler.getJava().getMainModule());
        List<String> javaOptions = assembler.getJava().getOptions();
        props.set(KEY_DISTRIBUTION_JAVA_OPTIONS, !javaOptions.isEmpty() ? passThrough(join(" ", javaOptions)) : "");
        props.set(KEY_DISTRIBUTION_EXECUTABLE, assembler.getExecutable().getName());
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

    @Override
    protected Path resolveOutputFile(TemplateContext props, Path targetDirectory, String fileName) throws AssemblerProcessingException {
        String executableName = assembler.getExecutable().getName();

        String windowsExtension = "." + assembler.getExecutable().getWindowsExtension();
        String unixExtension = assembler.getExecutable().getUnixExtension();
        unixExtension = isNotBlank(unixExtension) ? "." + unixExtension : "";
        return "bin/launcher.bat".equals(fileName) ?
            targetDirectory.resolve(BIN_DIRECTORY).resolve(executableName.concat(windowsExtension)) :
            "bin/launcher".equals(fileName) ?
                targetDirectory.resolve(BIN_DIRECTORY).resolve(executableName.concat(unixExtension)) :
                targetDirectory.resolve(fileName);
    }

    private void copyJars(JReleaserContext context, JavaArchiveAssembler assembler, Path jarsDirectory) throws AssemblerProcessingException {
        Set<Path> paths = new LinkedHashSet<>();

        if (isNotBlank(assembler.getMainJar().getPath())) {
            paths.add(assembler.getMainJar().getEffectivePath(context, assembler));
        }

        for (Glob glob : assembler.getJars()) {
            if (!glob.resolveActiveAndSelected(context)) continue;
            glob.getResolvedArtifacts(context).stream()
                .map(artifact -> artifact.getResolvedPath(context, assembler))
                .forEach(paths::add);
        }

        // copy all next
        try {
            Files.createDirectories(jarsDirectory);
            for (Path path : paths) {
                context.getLogger().debug(RB.$("assembler.copying"), path.getFileName());
                Files.copy(path, jarsDirectory.resolve(path.getFileName()), REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_copying_jars"), e);
        }
    }
}
