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
package org.jreleaser.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.JibConfiguration;
import org.jreleaser.model.internal.packagers.JibPackager;
import org.jreleaser.model.internal.packagers.JibSpec;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.tool.Jib;
import org.jreleaser.sdk.tool.ToolException;
import org.jreleaser.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.singletonList;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_PREPARE_DIRECTORY;
import static org.jreleaser.model.Constants.KEY_JIB_BASE_IMAGE;
import static org.jreleaser.model.Constants.KEY_JIB_CREATION_TIME;
import static org.jreleaser.model.Constants.KEY_JIB_ENVIRONMENT;
import static org.jreleaser.model.Constants.KEY_JIB_EXPOSED_PORTS;
import static org.jreleaser.model.Constants.KEY_JIB_FORMAT;
import static org.jreleaser.model.Constants.KEY_JIB_HAS_ENVIRONMENT;
import static org.jreleaser.model.Constants.KEY_JIB_HAS_EXPOSED_PORTS;
import static org.jreleaser.model.Constants.KEY_JIB_HAS_VOLUMES;
import static org.jreleaser.model.Constants.KEY_JIB_LABELS;
import static org.jreleaser.model.Constants.KEY_JIB_SPEC_NAME;
import static org.jreleaser.model.Constants.KEY_JIB_USER;
import static org.jreleaser.model.Constants.KEY_JIB_VOLUMES;
import static org.jreleaser.model.Constants.KEY_JIB_WORKING_DIRECTORY;
import static org.jreleaser.mustache.MustacheUtils.passThrough;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public class JibPackagerProcessor extends AbstractRepositoryPackagerProcessor<JibPackager> {
    private static final String ROOT = "ROOT";

    public JibPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution,
                                         TemplateContext props,
                                         String distributionName,
                                         Path prepareDirectory,
                                         String templateDirectory,
                                         String packagerName,
                                         boolean copyLicense) throws IOException, PackagerProcessingException {
        if (packager.getActiveSpecs().isEmpty()) {
            super.doPrepareDistribution(distribution, props, distributionName,
                prepareDirectory, templateDirectory, packagerName, true);
            return;
        }

        // copy root files
        String rootTemplateDirectory = getPackager().getTemplateDirectory() + File.separator + ROOT;
        super.doPrepareDistribution(distribution, props, distributionName,
            prepareDirectory.resolve(ROOT),
            rootTemplateDirectory,
            packager.getType(),
            false);
        Files.deleteIfExists(prepareDirectory.resolve(ROOT).resolve("build.yml"));

        for (JibSpec spec : packager.getActiveSpecs()) {
            prepareSpec(distribution, props, distributionName, prepareDirectory, spec);
        }
    }

    private void prepareSpec(Distribution distribution,
                             TemplateContext props,
                             String distributionName,
                             Path prepareDirectory,
                             JibSpec spec) throws IOException, PackagerProcessingException {
        TemplateContext newProps = fillSpecProps(distribution, props, spec);
        context.getLogger().debug(RB.$("distributions.action.preparing") + " {} spec", spec.getName());
        super.doPrepareDistribution(distribution, newProps, distributionName,
            prepareDirectory.resolve(spec.getName()),
            spec.getTemplateDirectory(),
            spec.getName() + "/" + packager.getType(),
            false);
    }

    private TemplateContext fillSpecProps(Distribution distribution, TemplateContext props, JibSpec spec) {
        List<Artifact> artifacts = singletonList(spec.getArtifact());
        TemplateContext newProps = fillProps(distribution, props);
        newProps.set(KEY_JIB_SPEC_NAME, spec.getName());
        fillJibProperties(newProps, spec);
        verifyAndAddArtifacts(newProps, distribution, artifacts);
        Path prepareDirectory = newProps.get(KEY_DISTRIBUTION_PREPARE_DIRECTORY);
        newProps.set(KEY_DISTRIBUTION_PREPARE_DIRECTORY, prepareDirectory.resolve(spec.getName()));
        Path packageDirectory = newProps.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        newProps.set(KEY_DISTRIBUTION_PACKAGE_DIRECTORY, packageDirectory.resolve(spec.getName()));
        return newProps;
    }

    @Override
    protected boolean verifyAndAddArtifacts(TemplateContext props, Distribution distribution) {
        if (packager.getActiveSpecs().isEmpty()) {
            return super.verifyAndAddArtifacts(props, distribution);
        }
        return true;
    }

    @Override
    protected void doPackageDistribution(Distribution distribution,
                                         TemplateContext props,
                                         Path packageDirectory) throws PackagerProcessingException {
        if (packager.getActiveSpecs().isEmpty()) {
            List<Artifact> artifacts = packager.resolveArtifacts(context, distribution);
            packageJib(distribution, props, packageDirectory, artifacts);
            return;
        }

        Path rootPrepareDirectory = getPrepareDirectory(props).resolve(ROOT);
        Path rootPackageDirectory = getPackageDirectory(props).resolve(ROOT);
        copyFiles(rootPrepareDirectory, rootPackageDirectory);

        for (JibSpec spec : packager.getActiveSpecs()) {
            context.getLogger().debug(RB.$("distributions.action.packaging") + " {} spec", spec.getName());
            TemplateContext newProps = fillSpecProps(distribution, props, spec);
            packageJib(distribution, newProps, packageDirectory.resolve(spec.getName()), singletonList(spec.getArtifact()));
        }
    }

    protected void packageJib(Distribution distribution,
                              TemplateContext props,
                              Path packageDirectory,
                              List<Artifact> artifacts) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);

        try {
            copyPreparedFiles(props);
            Path assemblyDirectory = packageDirectory.resolve("assembly");

            Files.createDirectories(assemblyDirectory);

            for (Artifact artifact : artifacts) {
                Path artifactPath = artifact.getEffectivePath(context, distribution);
                if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY) {
                    Files.copy(artifactPath, assemblyDirectory.resolve(artifactPath.getFileName()), REPLACE_EXISTING);
                } else {
                    FileUtils.unpackArchive(artifactPath, assemblyDirectory);
                }
            }
        } catch (IOException e) {
            throw new PackagerProcessingException(e);
        }
    }

    @Override
    public void publishDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        if (packager.getActiveSpecs().isEmpty()) {
            publishToRepository(distribution, props);
            super.publishDistribution(distribution, props);
            return;
        }

        publishToRepository(distribution, props);
        for (JibSpec spec : packager.getActiveSpecs()) {
            context.getLogger().debug(RB.$("distributions.action.publishing") + " {} spec", spec.getName());
            TemplateContext newProps = fillSpecProps(distribution, props, spec);
            publishJib(newProps, spec);
        }
    }

    private void publishToRepository(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        super.doPublishDistribution(distribution, fillProps(distribution, props));
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        publishJib(props, getPackager());
    }

    protected void publishJib(TemplateContext props, JibConfiguration jibc) throws PackagerProcessingException {
        if (context.isDryrun()) {
            for (JibConfiguration.Registry registry : jibc.getRegistries()) {
                for (String imageName : jibc.getImageNames()) {
                    imageName = registry.getServer() + "/" + resolveTemplate(imageName, props);
                    context.getLogger().info(" - {}", imageName);
                }
            }
            return;
        }

        Jib jib = new Jib(context.asImmutable(), packager.getVersion());
        try {
            if (!jib.setup()) {
                throw new PackagerProcessingException(RB.$("tool_unavailable", "jib"));
            }
        } catch (ToolException e) {
            throw new PackagerProcessingException(RB.$("tool_unavailable", "jib"));
        }

        Path packageDirectory = getPackageDirectory(props);

        for (JibConfiguration.Registry registry : jibc.getRegistries()) {
            List<String> args = new ArrayList<>();
            args.add("build");
            args.add("--console");
            args.add("plain");
            args.add("--verbosity");
            args.add("error");
            args.add("-c");
            args.add(packageDirectory.toAbsolutePath().toString());
            args.add("-b");
            args.add(packageDirectory.resolve("build.yml").toAbsolutePath().toString());

            if (isNotBlank(registry.getUsername())) {
                args.add("--username=" + registry.getUsername());
            }
            if (isNotBlank(registry.getFromUsername())) {
                args.add("--from-username=" + registry.getFromUsername());
            }
            if (isNotBlank(registry.getToUsername())) {
                args.add("--to-username=" + registry.getToUsername());
            }

            if (isNotBlank(registry.getPassword())) {
                args.add("--password=" + registry.getPassword());
            }
            if (isNotBlank(registry.getFromPassword())) {
                args.add("--from-password=" + registry.getFromPassword());
            }
            if (isNotBlank(registry.getToPassword())) {
                args.add("--to-password=" + registry.getToPassword());
            }

            for (String imageName : jibc.getImageNames()) {
                imageName = registry.getServer() + "/" + resolveTemplate(imageName, props);
                List<String> argsCopy = new ArrayList<>(args);
                argsCopy.add("-t");
                argsCopy.add(imageName);

                try {
                    context.getLogger().info(" - {}", imageName);
                    jib.invoke(context.getBasedir(), argsCopy);
                } catch (CommandException e) {
                    throw new PackagerProcessingException(RB.$("ERROR_unexpected_error"), e);
                }
            }
        }
    }

    @Override
    protected void fillPackagerProperties(TemplateContext props, Distribution distribution) {
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, distribution.getJava().getMainClass());
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_MODULE, distribution.getJava().getMainModule());
        fillJibProperties(props, getPackager());
    }

    protected void fillJibProperties(TemplateContext props, JibConfiguration jib) {
        props.set(KEY_JIB_BASE_IMAGE, jib.getBaseImage());
        props.set(KEY_JIB_CREATION_TIME, jib.getCreationTime());
        props.set(KEY_JIB_FORMAT, jib.getFormat().formatted());
        props.set(KEY_JIB_USER, jib.getUser());
        props.set(KEY_JIB_WORKING_DIRECTORY, jib.getWorkingDirectory());
        props.set(KEY_JIB_HAS_VOLUMES, !jib.getVolumes().isEmpty());
        props.set(KEY_JIB_VOLUMES, jib.getVolumes());
        props.set(KEY_JIB_HAS_EXPOSED_PORTS, !jib.getExposedPorts().isEmpty());
        props.set(KEY_JIB_EXPOSED_PORTS, jib.getExposedPorts());
        props.set(KEY_JIB_HAS_ENVIRONMENT, !jib.getEnvironment().isEmpty());

        Set<String> env = new TreeSet<>();
        jib.getEnvironment().forEach((key, value) -> env.add(passThrough("\"" + key + "\": \"" +
            resolveTemplate(value, props) + "\"")));
        props.set(KEY_JIB_ENVIRONMENT, env);

        Set<String> labels = new TreeSet<>();
        jib.getLabels().forEach((key, value) -> labels.add(passThrough("\"" + key + "\": \"" +
            resolveTemplate(value, props) + "\"")));
        props.set(KEY_JIB_LABELS, labels);
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             TemplateContext props,
                             Path outputDirectory,
                             String fileName) throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = "executable".equals(fileName) ?
            outputDirectory.resolve("assembly").resolve(distribution.getExecutable().getName()) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    @Override
    protected void prepareWorkingCopy(TemplateContext props, Path directory, Distribution distribution) throws IOException {
        Path packageDirectory = props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);

        List<JibSpec> activeSpecs = packager.getActiveSpecs();

        if (activeSpecs.isEmpty()) {
            for (String imageName : packager.getImageNames()) {
                copyJibfiles(packageDirectory, resolveTemplate(imageName, props), directory, false);
            }
        } else {
            // copy files that do not belong to specs
            prepareWorkingCopy(packageDirectory.resolve(ROOT), directory);

            for (JibSpec spec : activeSpecs) {
                TemplateContext newProps = fillSpecProps(distribution, props, spec);
                for (String imageName : spec.getImageNames()) {
                    copyJibfiles(packageDirectory.resolve(spec.getName()), resolveTemplate(imageName, newProps), directory, true);
                }
            }
        }
    }

    private void copyJibfiles(Path source, String imageName, Path directory, boolean isSpec) throws IOException {
        Path destination = directory;

        String[] parts = imageName.split("/");
        parts = parts[parts.length - 1].split(":");
        if (isSpec) {
            destination = directory.resolve(parts[0]);
        }

        if (packager.getPackagerRepository().isVersionedSubfolders()) {
            destination = directory.resolve(parts[1]);
        }

        Path assembly = destination.resolve("assembly");
        FileUtils.deleteFiles(assembly);

        Files.createDirectories(destination);
        prepareWorkingCopy(source, destination, path -> "assembly".equals(path.getFileName().toString()));
    }
}
