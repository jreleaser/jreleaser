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
package org.jreleaser.assemblers;

import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Archive;
import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.JavaArchiveAssembler;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.templates.TemplateResource;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.templates.TemplateUtils.resolveAndMergeTemplates;
import static org.jreleaser.templates.TemplateUtils.resolveTemplates;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
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
    protected void doAssemble(Map<String, Object> props) throws AssemblerProcessingException {
        Path assembleDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        String archiveName = assembler.getResolvedArchiveName(context);

        Path inputsDirectory = assembleDirectory.resolve("inputs");
        Path workDirectory = assembleDirectory.resolve("work");
        Path archiveDirectory = workDirectory.resolve(archiveName);

        try {
            FileUtils.deleteFiles(inputsDirectory);
            FileUtils.deleteFiles(workDirectory);
            Files.createDirectories(archiveDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_delete_archive", archiveName), e);
        }

        try {
            context.getLogger().debug(RB.$("packager.resolve.templates"), assembler.getType(), assembler.getName());
            Map<String, TemplateResource> templates = resolveAndMergeTemplates(context.getLogger(),
                assembler.getType(),
                assembler.getType(),
                context.getModel().getProject().isSnapshot(),
                context.getBasedir().resolve(getAssembler().getTemplateDirectory()));
            templates.putAll(resolveTemplates(context.getBasedir().resolve(getAssembler().getTemplateDirectory())));

            for (Map.Entry<String, TemplateResource> entry : templates.entrySet()) {
                String key = entry.getKey();
                TemplateResource value = entry.getValue();

                if (value.isReader()) {
                    context.getLogger().debug(RB.$("packager.evaluate.template"), key, assembler.getName(), assembler.getType());
                    String content = applyTemplate(value.getReader(), props, key);
                    context.getLogger().debug(RB.$("packager.write.template"), key, assembler.getName(), assembler.getType());
                    writeFile(context.getModel().getProject(), content, props, key);
                } else {
                    context.getLogger().debug(RB.$("packager.write.template"), key, assembler.getName(), assembler.getType());
                    writeFile(context.getModel().getProject(), IOUtils.toByteArray(value.getInputStream()), props, key);
                }
            }
        } catch (IllegalArgumentException | IOException e) {
            throw new AssemblerProcessingException(e);
        }

        // copy files
        context.getLogger().debug(RB.$("assembler.copy.files"), context.relativizeToBasedir(archiveDirectory));
        copyFiles(context, archiveDirectory);
        copyFileSets(context, archiveDirectory);

        // copy jars
        Path jarsDirectory = archiveDirectory.resolve("lib");
        context.getLogger().debug(RB.$("assembler.copy.jars"), context.relativizeToBasedir(jarsDirectory));
        copyJars(context, assembler, jarsDirectory);

        // copy launcher
        Path binDirectory = archiveDirectory.resolve("bin");
        try {
            Files.createDirectories(binDirectory);

            String executableName = assembler.getExecutable().getName();

            Path launcher = inputsDirectory.resolve("bin").resolve(executableName.concat(".bat"));
            Files.copy(launcher,
                binDirectory.resolve(executableName.concat("." + assembler.getExecutable().getWindowsExtension())));

            launcher = inputsDirectory.resolve("bin").resolve(executableName);
            Files.copy(launcher, binDirectory.resolve(executableName));

            FileUtils.grantExecutableAccess(launcher);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_copy_launcher",
                context.relativizeToBasedir(binDirectory)), e);
        }

        // run archive x format
        for (Archive.Format format : assembler.getFormats()) {
            archive(workDirectory, assembleDirectory, archiveName, format);
        }
    }

    @Override
    protected void fillAssemblerProperties(Map<String, Object> props) {
        super.fillAssemblerProperties(props);

        if (isNotBlank(assembler.getMainJar().getPath())) {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_MAIN_JAR, assembler.getMainJar().getEffectivePath(context, assembler)
                .getFileName());
        } else {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_MAIN_JAR, "");
        }
        props.put(Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS, assembler.getJava().getMainClass());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE, assembler.getJava().getMainModule());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_OPTIONS, !assembler.getJava().getOptions().isEmpty() ? assembler.getJava().getOptions() : "");
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
                    break;
                case TXZ:
                case TAR_XZ:
                    FileUtils.xz(workDirectory, archiveFile);
                    break;
                case TBZ2:
                case TAR_BZ2:
                    FileUtils.bz2(workDirectory, archiveFile);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private Set<Path> copyFiles(JReleaserContext context, Path destination) throws AssemblerProcessingException {
        Set<Path> paths = new LinkedHashSet<>();

        // resolve all first
        for (Glob glob : assembler.getFiles()) {
            glob.getResolvedArtifacts(context).stream()
                .map(artifact -> artifact.getResolvedPath(context, assembler))
                .forEach(paths::add);
        }

        // copy all next
        try {
            Files.createDirectories(destination);
            for (Path path : paths) {
                context.getLogger().debug(RB.$("assembler.copying"), path.getFileName());
                Files.copy(path, destination.resolve(path.getFileName()), REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_copying_files"), e);
        }

        return paths;
    }

    private void writeFile(Project project, String content, Map<String, Object> props, String fileName)
        throws AssemblerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path inputsDirectory = outputDirectory.resolve("inputs");
        try {
            Files.createDirectories(inputsDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_create_directories"), e);
        }

        String executableName = assembler.getExecutable().getName();

        Path outputFile = "launcher.bat".equals(fileName) ?
            inputsDirectory.resolve("bin").resolve(executableName.concat(".bat")) :
            "launcher".equals(fileName) ?
                inputsDirectory.resolve("bin").resolve(executableName) :
                inputsDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    private void writeFile(Project project, byte[] content, Map<String, Object> props, String fileName) throws AssemblerProcessingException {
        Path outputDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Path inputsDirectory = outputDirectory.resolve("inputs");
        try {
            Files.createDirectories(inputsDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_create_directories"), e);
        }

        Path outputFile = inputsDirectory.resolve(fileName);
        writeFile(content, outputFile);
    }

    private static Set<Path> copyJars(JReleaserContext context, JavaArchiveAssembler assembler, Path jarsDirectory) throws AssemblerProcessingException {
        Set<Path> paths = new LinkedHashSet<>();

        if (isNotBlank(assembler.getMainJar().getPath())) {
            paths.add(assembler.getMainJar().getEffectivePath(context, assembler));
        }

        for (Glob glob : assembler.getJars()) {
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

        return paths;
    }
}
