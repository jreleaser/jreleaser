/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.model.Assembler;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.assembler.spi.AssemblerProcessingException;
import org.jreleaser.model.assembler.spi.AssemblerProcessor;
import org.jreleaser.util.Constants;
import org.jreleaser.util.Version;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessInitException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.templates.TemplateUtils.resolveAndMergeTemplates;
import static org.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;
import static org.jreleaser.util.FileUtils.grantFullAccess;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
abstract class AbstractAssemblerProcessor<A extends Assembler> implements AssemblerProcessor<A> {
    protected final JReleaserContext context;
    protected A assembler;

    protected AbstractAssemblerProcessor(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public A getAssembler() {
        return assembler;
    }

    @Override
    public void setAssembler(A assembler) {
        this.assembler = assembler;
    }

    @Override
    public void assemble(Map<String, Object> props) throws AssemblerProcessingException {
        try {
            context.getLogger().debug("creating props for {}/{}", assembler.getType(), assembler.getName());
            Map<String, Object> newProps = fillProps(props);

            context.getLogger().debug("resolving templates for {}/{}", assembler.getType(), assembler.getName());
            Map<String, Reader> templates = resolveAndMergeTemplates(context.getLogger(),
                assembler.getType(),
                assembler.getType(),
                context.getModel().getProject().isSnapshot(),
                context.getBasedir().resolve(getAssembler().getTemplateDirectory()));

            for (Map.Entry<String, Reader> entry : templates.entrySet()) {
                context.getLogger().debug("evaluating template {} for {}/{}", entry.getKey(), assembler.getName(), assembler.getType());
                String content = applyTemplate(entry.getValue(), newProps);
                context.getLogger().debug("writing template {} for {}/{}", entry.getKey(), assembler.getName(), assembler.getType());
                writeFile(context.getModel().getProject(), content, newProps, entry.getKey());
            }

            Path assembleDirectory = (Path) props.get(Constants.KEY_ASSEMBLE_DIRECTORY);
            Files.createDirectories(assembleDirectory);

            doAssemble(newProps);
        } catch (IllegalArgumentException | IOException e) {
            throw new AssemblerProcessingException(e);
        }
    }

    protected abstract void doAssemble(Map<String, Object> props) throws AssemblerProcessingException;

    protected abstract void writeFile(Project project, String content, Map<String, Object> props, String fileName) throws AssemblerProcessingException;

    protected void writeFile(String content, Path outputFile) throws AssemblerProcessingException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);
            grantFullAccess(outputFile);
        } catch (IOException e) {
            throw new AssemblerProcessingException("Unexpected error when writing to " + outputFile.toAbsolutePath(), e);
        }
    }

    protected Map<String, Object> fillProps(Map<String, Object> props) throws AssemblerProcessingException {
        Map<String, Object> newProps = new LinkedHashMap<>(props);
        context.getLogger().debug("filling git properties into props");
        context.getModel().getRelease().getGitService().fillProps(newProps, context.getModel());
        context.getLogger().debug("filling assembler properties into props");
        fillAssemblerProperties(newProps);
        return newProps;
    }

    protected void fillAssemblerProperties(Map<String, Object> props) {
        props.put(Constants.KEY_DISTRIBUTION_NAME, assembler.getName());
        props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, assembler.getExecutable());
        props.putAll(assembler.getJava().getResolvedExtraProperties());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID, assembler.getJava().getGroupId());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, assembler.getJava().getArtifactId());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, assembler.getJava().getVersion());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS, assembler.getJava().getMainClass());
        if (isNotBlank(assembler.getJava().getVersion())) {
            Version jv = Version.of(assembler.getJava().getVersion());
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, jv.getMajor());
            if (jv.hasMinor()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, jv.getMinor());
            if (jv.hasPatch()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, jv.getPatch());
            if (jv.hasTag()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, jv.getTag());
            if (jv.hasBuild()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, jv.getBuild());
        } else {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, "");
        }
        props.putAll(assembler.getResolvedExtraProperties());
    }

    protected boolean executeCommand(Path directory, List<String> cmd) throws AssemblerProcessingException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            int exitValue = new ProcessExecutor(cmd)
                .directory(directory.toFile())
                .redirectOutput(out)
                .redirectError(err)
                .execute()
                .getExitValue();

            info(out);
            error(err);

            if (exitValue == 0) return true;
            throw new AssemblerProcessingException("Command execution error. exitValue = " + exitValue);
        } catch (ProcessInitException e) {
            throw new AssemblerProcessingException("Unexpected error", e.getCause());
        } catch (Exception e) {
            if (e instanceof AssemblerProcessingException) {
                throw (AssemblerProcessingException) e;
            }
            throw new AssemblerProcessingException("Unexpected error", e);
        }
    }

    protected boolean executeCommand(List<String> cmd) throws AssemblerProcessingException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            int exitValue = new ProcessExecutor(cmd)
                .redirectOutput(out)
                .redirectError(err)
                .execute()
                .getExitValue();

            info(out);
            error(err);

            if (exitValue == 0) return true;
            throw new AssemblerProcessingException("Command execution error. exitValue = " + exitValue);
        } catch (ProcessInitException e) {
            throw new AssemblerProcessingException("Unexpected error", e.getCause());
        } catch (Exception e) {
            if (e instanceof AssemblerProcessingException) {
                throw (AssemblerProcessingException) e;
            }
            throw new AssemblerProcessingException("Unexpected error", e);
        }
    }

    protected boolean executeCommandCapturing(List<String> cmd, OutputStream out) throws AssemblerProcessingException {
        try {
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            int exitValue = new ProcessExecutor(cmd)
                .redirectOutput(out)
                .redirectError(err)
                .execute()
                .getExitValue();

            error(err);

            if (exitValue == 0) return true;
            throw new AssemblerProcessingException("Command execution error. exitValue = " + exitValue);
        } catch (ProcessInitException e) {
            throw new AssemblerProcessingException("Unexpected error", e.getCause());
        } catch (Exception e) {
            if (e instanceof AssemblerProcessingException) {
                throw (AssemblerProcessingException) e;
            }
            throw new AssemblerProcessingException("Unexpected error", e);
        }
    }

    protected void info(ByteArrayOutputStream out) {
        log(out, context.getLogger()::info);
    }

    protected void error(ByteArrayOutputStream err) {
        log(err, context.getLogger()::error);
    }

    private void log(ByteArrayOutputStream stream, Consumer<? super String> consumer) {
        String str = stream.toString();
        if (isBlank(str)) return;

        Arrays.stream(str.split(System.lineSeparator()))
            .forEach(consumer);
    }
}
