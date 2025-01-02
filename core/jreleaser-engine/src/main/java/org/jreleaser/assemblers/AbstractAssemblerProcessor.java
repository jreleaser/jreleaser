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
package org.jreleaser.assemblers;

import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.internal.catalog.swid.SwidTag;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.model.spi.assemble.AssemblerProcessor;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.command.CommandExecutor;
import org.jreleaser.templates.TemplateResource;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.templates.TemplateUtils.resolveAndMergeTemplates;
import static org.jreleaser.templates.TemplateUtils.resolveTemplates;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;
import static org.jreleaser.util.FileUtils.grantFullAccess;
import static org.jreleaser.util.PlatformUtils.isWindows;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.quote;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class AbstractAssemblerProcessor<A extends org.jreleaser.model.api.assemble.Assembler, S extends Assembler<A>> implements AssemblerProcessor<A, S> {
    public static final String BIN_DIRECTORY = "bin";
    public static final String LICENSE = "LICENSE";
    public static final String UNIVERSAL_DIRECTORY = "universal";
    public static final String INPUTS_DIRECTORY = "inputs";
    public static final String WORK_DIRECTORY = "work";
    public static final String JARS_DIRECTORY = "jars";
    public static final String ARCHIVE_DIRECTORY = "archive";

    protected final JReleaserContext context;
    protected S assembler;

    protected AbstractAssemblerProcessor(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public S getAssembler() {
        return assembler;
    }

    @Override
    public void setAssembler(S assembler) {
        this.assembler = assembler;
    }

    @Override
    public void assemble(TemplateContext props) throws AssemblerProcessingException {
        try {
            context.getLogger().debug(RB.$("packager.create.properties"), assembler.getType(), assembler.getName());
            TemplateContext newProps = fillProps(props);

            Path assembleDirectory = props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
            Files.createDirectories(assembleDirectory);

            doAssemble(newProps);
        } catch (IllegalArgumentException | IOException e) {
            throw new AssemblerProcessingException(e);
        }
    }

    protected abstract void doAssemble(TemplateContext props) throws AssemblerProcessingException;

    protected void writeFile(String content, Path outputFile) throws AssemblerProcessingException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content.getBytes(UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error_writing_file", outputFile.toAbsolutePath()), e);
        }
    }

    protected void writeFile(byte[] content, Path outputFile) throws AssemblerProcessingException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content, CREATE, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error_writing_file", outputFile.toAbsolutePath()), e);
        }
    }

    protected TemplateContext fillProps(TemplateContext props) {
        TemplateContext newProps = new TemplateContext(props);
        context.getLogger().debug(RB.$("packager.fill.git.properties"));
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();
        if (null != releaser) {
            releaser.fillProps(newProps, context.getModel());
        }
        context.getLogger().debug(RB.$("assembler.fill.assembler.properties"));
        fillAssemblerProperties(newProps);
        applyTemplates(props, props);
        return newProps;
    }

    protected void fillAssemblerProperties(TemplateContext props) {
        props.setAll(assembler.props());
    }

    protected Command.Result executeCommand(Path directory, Command command) throws AssemblerProcessingException {
        try {
            Command.Result result = new CommandExecutor(context.getLogger())
                .executeCommand(directory, command);
            if (result.getExitValue() != 0) {
                throw new CommandException(RB.$("ERROR_command_execution_exit_value", result.getExitValue()));
            }
            return result;
        } catch (CommandException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    protected Command.Result executeCommand(Command command) throws AssemblerProcessingException {
        try {
            Command.Result result = new CommandExecutor(context.getLogger())
                .executeCommand(command);
            if (result.getExitValue() != 0) {
                throw new CommandException(RB.$("ERROR_command_execution_exit_value", result.getExitValue()));
            }
            return result;
        } catch (CommandException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    protected void copyTemplates(JReleaserContext context, TemplateContext props, Path targetDirectory) throws AssemblerProcessingException {
        try {
            context.getLogger().debug(RB.$("packager.resolve.templates"), assembler.getType(), assembler.getName());
            Map<String, TemplateResource> templates = resolveAndMergeTemplates(context.getLogger(),
                assembler.getType(),
                assembler.getType(),
                context.getModel().getProject().isSnapshot(),
                context.getBasedir().resolve(getAssembler().getTemplateDirectory()));
            templates.putAll(resolveTemplates(context.getBasedir().resolve(getAssembler().getTemplateDirectory())));

            for (Map.Entry<String, TemplateResource> entry : templates.entrySet()) {
                String filename = entry.getKey();

                if (isSkipped(filename)) {
                    context.getLogger().debug(RB.$("packager.skipped.template"), filename, assembler.getType(), assembler.getName());
                    continue;
                }

                TemplateResource value = entry.getValue();

                if (value.isReader()) {
                    context.getLogger().debug(RB.$("packager.evaluate.template"), filename, assembler.getName(), assembler.getType());
                    String content = applyTemplate(value.getReader(), props, filename);
                    context.getLogger().debug(RB.$("packager.write.template"), filename, assembler.getName(), assembler.getType());
                    writeFile(content, props, targetDirectory, filename);
                } else {
                    context.getLogger().debug(RB.$("packager.write.template"), filename, assembler.getName(), assembler.getType());
                    writeFile(IOUtils.toByteArray(value.getInputStream()), props, targetDirectory, filename);
                }
            }
        } catch (IllegalArgumentException | IOException e) {
            throw new AssemblerProcessingException(e);
        }
    }

    protected void writeFile(byte[] content, TemplateContext props, Path targetDirectory, String fileName) throws AssemblerProcessingException {
        try {
            Files.createDirectories(targetDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_create_directories"), e);
        }

        Path outputFile = targetDirectory.resolve(fileName);
        writeFile(content, outputFile);
    }

    protected void writeFile(String content, TemplateContext props, Path targetDirectory, String fileName) throws AssemblerProcessingException {
        fileName = trimTplExtension(fileName);

        try {
            Files.createDirectories(targetDirectory);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_create_directories"), e);
        }

        Path outputFile = resolveOutputFile(props, targetDirectory, fileName);

        writeFile(content, outputFile);
    }

    protected Path resolveOutputFile(TemplateContext props, Path targetDirectory, String fileName) throws AssemblerProcessingException {
        return targetDirectory.resolve(fileName);
    }

    protected void copyArtifacts(JReleaserContext context, Path destination, String platformConstraint, boolean filterByPlatform) throws AssemblerProcessingException {
        copyArtifacts(context, null, destination, platformConstraint, filterByPlatform);
    }

    protected void copyArtifacts(JReleaserContext context, TemplateContext additionalContext, Path destination, String platformConstraint, boolean filterByPlatform) throws AssemblerProcessingException {
        try {
            Files.createDirectories(destination);

            for (Artifact artifact : assembler.getArtifacts()) {
                if (!artifact.resolveEnabled(context.getModel().getProject())) continue;
                Path incoming = artifact.getResolvedPath(context, additionalContext, assembler);
                if (artifact.isOptional(context) && !artifact.resolvedPathExists()) continue;
                String platform = artifact.getPlatform();
                if (filterByPlatform && isNotBlank(platformConstraint) && isNotBlank(platform) && !PlatformUtils.isCompatible(platformConstraint, platform)) {
                    context.getLogger().debug(RB.$("assembler.artifact.filter"), incoming.getFileName());
                    continue;
                }
                Path outgoing = incoming.getFileName();

                String transform = artifact.getTransform();
                if (isNotBlank(transform)) {
                    if (transform.startsWith("/")) transform = transform.substring(1);
                    outgoing = Paths.get(Artifacts.resolveForArtifact(transform, context, additionalContext, artifact, assembler));
                }
                outgoing = destination.resolve(outgoing);
                Files.createDirectories(outgoing.getParent());

                context.getLogger().debug(RB.$("assembler.copying"), incoming.getFileName());
                Files.copy(incoming, outgoing, REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_copying_files"), e);
        }
    }

    protected void copyFiles(JReleaserContext context, Path destination) throws AssemblerProcessingException {
        copyFiles(context, null, destination);
    }

    protected void copyFiles(JReleaserContext context, TemplateContext additionalContext, Path destination) throws AssemblerProcessingException {
        Set<Path> paths = new LinkedHashSet<>();

        // resolve all first
        for (Glob glob : assembler.getFiles()) {
            if (!glob.resolveActiveAndSelected(context)) continue;
            glob.getResolvedArtifacts(context, additionalContext).stream()
                .map(artifact -> artifact.getResolvedPath(context, additionalContext, assembler))
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
    }

    protected void copyFileSets(JReleaserContext context, Path destination) throws AssemblerProcessingException {
        copyFileSets(context, null, destination);
    }

    protected void copyFileSets(JReleaserContext context, TemplateContext additionalContext, Path destination) throws AssemblerProcessingException {
        try {
            for (FileSet fileSet : assembler.getFileSets()) {
                if (!fileSet.resolveActiveAndSelected(context)) continue;
                Path src = context.getBasedir().resolve(fileSet.getResolvedInput(context, additionalContext));
                Path dest = destination;

                String output = fileSet.getResolvedOutput(context, additionalContext);
                if (isNotBlank(output)) {
                    dest = destination.resolve(output);
                }

                Set<Path> paths = fileSet.getResolvedPaths(context, additionalContext);
                FileUtils.copyFiles(context.getLogger(), src, dest, paths);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_copying_files"), e);
        }
    }

    protected void generateSwidTag(JReleaserContext context, Path archiveDirectory) throws AssemblerProcessingException {
        SwidTag swidTag = assembler.getSwid();
        if (!swidTag.isEnabled()) return;

        context.getLogger().info(RB.$("assembler.swid.tag"), swidTag.getName());
        try {
            SwidTagGenerator.generateTag(context, archiveDirectory, swidTag);
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assemble_swid_tag", swidTag.getName()), e);
        }
    }

    protected String maybeQuote(String str) {
        return isWindows() ? quote(str) : str;
    }

    public boolean isSkipped(String filename) {
        // check explicit match
        if (assembler.getSkipTemplates().contains(filename)) return true;
        // check using string contains
        if (assembler.getSkipTemplates().stream()
            .anyMatch(filename::contains)) return true;
        // check using regex
        if (assembler.getSkipTemplates().stream()
            .anyMatch(filename::matches)) return true;

        // remove .tpl and check again
        String fname = trimTplExtension(filename);

        // check explicit match
        if (assembler.getSkipTemplates().contains(fname)) return true;
        // check using string contains
        if (assembler.getSkipTemplates().stream()
            .anyMatch(fname::contains)) return true;
        // check using regex
        return assembler.getSkipTemplates().stream()
            .anyMatch(fname::matches);
    }
}
