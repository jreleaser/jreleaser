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
package org.jreleaser.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Docker;
import org.jreleaser.model.DockerConfiguration;
import org.jreleaser.model.DockerSpec;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Registry;
import org.jreleaser.model.packager.spi.PackagerProcessingException;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.command.Command;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_PREPARE_DIRECTORY;
import static org.jreleaser.util.Constants.KEY_DOCKER_BASE_IMAGE;
import static org.jreleaser.util.Constants.KEY_DOCKER_LABELS;
import static org.jreleaser.util.Constants.KEY_DOCKER_POST_COMMANDS;
import static org.jreleaser.util.Constants.KEY_DOCKER_PRE_COMMANDS;
import static org.jreleaser.util.Constants.KEY_DOCKER_SPEC_NAME;
import static org.jreleaser.util.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class DockerPackagerProcessor extends AbstractRepositoryPackagerProcessor<Docker> {
    private static final String ROOT = "ROOT";

    public DockerPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPrepareDistribution(Distribution distribution,
                                         Map<String, Object> props,
                                         String distributionName,
                                         Path prepareDirectory,
                                         String templateDirectory,
                                         String packagerName,
                                         boolean copyLicense) throws IOException, PackagerProcessingException {
        if (packager.getActiveSpecs().isEmpty()) {
            super.doPrepareDistribution(distribution, props, distributionName,
                prepareDirectory, templateDirectory, packagerName, true);

            if (!packager.isUseLocalArtifact()) {
                Files.move(prepareDirectory.resolve("Dockerfile-remote"),
                    prepareDirectory.resolve("Dockerfile"),
                    REPLACE_EXISTING);
            } else {
                Files.deleteIfExists(prepareDirectory.resolve("Dockerfile-remote"));
            }

            return;
        }

        // copy root files
        String rootTemplateDirectory = getPackager().getTemplateDirectory() + File.separator + ROOT;
        super.doPrepareDistribution(distribution, props, distributionName,
            prepareDirectory.resolve(ROOT),
            rootTemplateDirectory,
            packager.getType(),
            false);
        Files.deleteIfExists(prepareDirectory.resolve(ROOT).resolve("Dockerfile"));
        Files.deleteIfExists(prepareDirectory.resolve(ROOT).resolve("Dockerfile-remote"));

        for (DockerSpec spec : packager.getActiveSpecs()) {
            prepareSpec(distribution, props, distributionName, prepareDirectory, spec, ProcessingStep.PREPARE);
        }
    }

    private void prepareSpec(Distribution distribution,
                             Map<String, Object> props,
                             String distributionName,
                             Path prepareDirectory,
                             DockerSpec spec,
                             ProcessingStep processingStep) throws IOException, PackagerProcessingException {
        Map<String, Object> newProps = fillSpecProps(distribution, props, spec, processingStep);
        context.getLogger().debug(RB.$("distributions.action.preparing") + " {} spec", spec.getName());
        super.doPrepareDistribution(distribution, newProps, distributionName,
            prepareDirectory.resolve(spec.getName()),
            spec.getTemplateDirectory(),
            spec.getName() + "/" + packager.getType(),
            false);

        if (!spec.isUseLocalArtifact()) {
            Files.move(prepareDirectory.resolve(spec.getName()).resolve("Dockerfile-remote"),
                prepareDirectory.resolve(spec.getName()).resolve("Dockerfile"),
                REPLACE_EXISTING);
        } else {
            Files.deleteIfExists(prepareDirectory.resolve(spec.getName()).resolve("Dockerfile-remote"));
        }
    }

    private Map<String, Object> fillSpecProps(Distribution distribution, Map<String, Object> props, DockerSpec spec, ProcessingStep processingStep) throws PackagerProcessingException {
        List<Artifact> artifacts = Collections.singletonList(spec.getArtifact());
        Map<String, Object> newProps = fillProps(distribution, props, processingStep);
        newProps.put(KEY_DOCKER_SPEC_NAME, spec.getName());
        fillDockerProperties(newProps, distribution, spec);
        verifyAndAddArtifacts(newProps, distribution, artifacts);
        Path prepareDirectory = (Path) newProps.get(KEY_DISTRIBUTION_PREPARE_DIRECTORY);
        newProps.put(KEY_DISTRIBUTION_PREPARE_DIRECTORY, prepareDirectory.resolve(spec.getName()));
        Path packageDirectory = (Path) newProps.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        newProps.put(KEY_DISTRIBUTION_PACKAGE_DIRECTORY, packageDirectory.resolve(spec.getName()));
        return newProps;
    }

    @Override
    protected boolean verifyAndAddArtifacts(Map<String, Object> props,
                                            Distribution distribution) throws PackagerProcessingException {
        if (packager.getActiveSpecs().isEmpty()) {
            return super.verifyAndAddArtifacts(props, distribution);
        }
        return true;
    }

    @Override
    protected void doPackageDistribution(Distribution distribution,
                                         Map<String, Object> props,
                                         Path packageDirectory) throws PackagerProcessingException {
        if (packager.getActiveSpecs().isEmpty()) {
            List<Artifact> artifacts = packager.resolveCandidateArtifacts(context, distribution);
            packageDocker(distribution, props, packageDirectory, getPackager(), artifacts);
            return;
        }

        Path rootPrepareDirectory = getPrepareDirectory(props).resolve(ROOT);
        Path rootPackageDirectory = getPackageDirectory(props).resolve(ROOT);
        copyFiles(rootPrepareDirectory, rootPackageDirectory);

        for (DockerSpec spec : packager.getActiveSpecs()) {
            context.getLogger().debug(RB.$("distributions.action.packaging") + " {} spec", spec.getName());
            Map<String, Object> newProps = fillSpecProps(distribution, props, spec, ProcessingStep.PACKAGE);
            packageDocker(distribution, newProps, packageDirectory.resolve(spec.getName()),
                spec, Collections.singletonList(spec.getArtifact()));
        }
    }

    protected void packageDocker(Distribution distribution,
                                 Map<String, Object> props,
                                 Path packageDirectory,
                                 DockerConfiguration docker,
                                 List<Artifact> artifacts) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);

        try {
            // copy files
            Path workingDirectory = prepareAssembly(distribution, props, packageDirectory, artifacts);

            for (String imageName : docker.getImageNames()) {
                imageName = resolveTemplate(imageName, props);

                // command line
                Command cmd = createBuildCommand(props, docker);
                if (!cmd.hasArg("-q") && !cmd.hasArg("--quiet")) {
                    cmd.arg("-q");
                }
                cmd.arg("-f");
                cmd.arg(workingDirectory.resolve("Dockerfile").toAbsolutePath().toString());
                cmd.arg("-t");
                cmd.arg(imageName);
                cmd.arg(workingDirectory.toAbsolutePath().toString());
                context.getLogger().debug(String.join(" ", cmd.getArgs()));

                context.getLogger().info(" - {}", imageName);
                // execute
                executeCommand(cmd);
            }
        } catch (IOException e) {
            throw new PackagerProcessingException(e);
        }
    }

    private Path prepareAssembly(Distribution distribution,
                                 Map<String, Object> props,
                                 Path packageDirectory,
                                 List<Artifact> artifacts) throws IOException, PackagerProcessingException {
        copyPreparedFiles(distribution, props);
        Path assemblyDirectory = packageDirectory.resolve("assembly");

        Files.createDirectories(assemblyDirectory);

        for (Artifact artifact : artifacts) {
            Path artifactPath = artifact.getEffectivePath(context, distribution);
            if (distribution.getType() == Distribution.DistributionType.NATIVE_IMAGE) {
                if (artifactPath.toString().endsWith(".zip")) {
                    FileUtils.unpackArchive(artifactPath, assemblyDirectory);
                } else {
                    Files.copy(artifactPath, assemblyDirectory.resolve(artifactPath.getFileName()), REPLACE_EXISTING);
                }
            } else {
                Files.copy(artifactPath, assemblyDirectory.resolve(artifactPath.getFileName()), REPLACE_EXISTING);
            }
        }

        return packageDirectory;
    }

    private Command createBuildCommand(Map<String, Object> props, DockerConfiguration docker) {
        Command cmd = createCommand("build");
        for (int i = 0; i < docker.getBuildArgs().size(); i++) {
            String arg = docker.getBuildArgs().get(i);
            if (arg.contains("{{")) {
                cmd.arg(resolveTemplate(arg, props));
            } else {
                cmd.arg(arg);
            }
        }
        return cmd;
    }

    private Command createCommand(String name) {
        return new Command("docker" + (PlatformUtils.isWindows() ? ".exe" : ""))
            .arg("-l")
            .arg("error")
            .arg(name);
    }

    @Override
    public void publishDistribution(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        if (packager.getActiveSpecs().isEmpty()) {
            if (packager.getRegistries().isEmpty()) {
                context.getLogger().info(RB.$("docker.no.registries"));
                publishToRepository(distribution, props);
                return;
            }
            super.publishDistribution(distribution, props);
            publishToRepository(distribution, props);
            return;
        }

        for (DockerSpec spec : packager.getActiveSpecs()) {
            context.getLogger().debug(RB.$("distributions.action.publishing") + " {} spec", spec.getName());
            Map<String, Object> newProps = fillSpecProps(distribution, props, spec, ProcessingStep.PUBLISH);
            publishDocker(distribution, newProps, spec);
        }
        publishToRepository(distribution, props);
    }

    private void publishToRepository(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        super.doPublishDistribution(distribution, fillProps(distribution, props, ProcessingStep.PUBLISH));
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, Map<String, Object> props) throws PackagerProcessingException {
        publishDocker(distribution, props, getPackager());
    }

    protected void publishDocker(Distribution distribution,
                                 Map<String, Object> props,
                                 DockerConfiguration docker) throws PackagerProcessingException {
        for (Registry registry : docker.getRegistries()) {
            login(registry);
            for (String imageName : docker.getImageNames()) {
                publish(registry, imageName, props);
            }
            logout(registry);
        }
    }

    private void login(Registry registry) throws PackagerProcessingException {
        Command cmd = createCommand("login");
        if (isNotBlank(registry.getServer())) {
            cmd.arg(registry.getServer());
        }
        cmd.arg("-u");
        cmd.arg(registry.getResolvedUsername());
        cmd.arg("-p");
        cmd.arg(registry.getResolvedPassword());

        ByteArrayInputStream in = new ByteArrayInputStream((registry.getResolvedPassword() + System.lineSeparator()).getBytes());

        context.getLogger().debug(RB.$("docker.login"),
            registry.getServerName(),
            (isNotBlank(registry.getServer()) ? " (" + registry.getServer() + ")" : ""));
        if (!context.isDryrun()) executeCommandWithInput(cmd, in);
    }

    private void publish(Registry registry, String imageName, Map<String, Object> props) throws PackagerProcessingException {
        imageName = resolveTemplate(imageName, props);

        String tag = imageName;
        String serverName = registry.getServerName();
        String server = registry.getServer();
        String repositoryName = registry.getRepositoryName();

        // if serverName == DEFAULT
        //   tag: repositoryName/imageName
        // else
        //   tag: server/repositoryName/imageName

        if (Registry.DEFAULT_NAME.equals(serverName)) {
            if (!tag.startsWith(repositoryName)) {
                int pos = tag.indexOf("/");
                if (pos < 0) {
                    tag = repositoryName + "/" + tag;
                } else {
                    tag = repositoryName + tag.substring(pos);
                }
            }
        } else {
            if (!tag.startsWith(server)) {
                int pos = tag.indexOf("/");
                if (pos < 0) {
                    tag = server + "/" + repositoryName + "/" + tag;
                } else {
                    tag = server + "/" + repositoryName + tag.substring(pos);
                }
            }
        }

        if (!tag.equals(imageName)) {
            Command cmd = createCommand("tag")
                .arg(imageName)
                .arg(tag);

            context.getLogger().debug(RB.$("docker.tag"), imageName, tag);
            if (!context.isDryrun()) executeCommand(cmd);
        }

        Command cmd = createCommand("push")
            .arg("-q")
            .arg(tag);

        context.getLogger().info(" - {}", tag);
        context.getLogger().debug(RB.$("docker.push"),
            tag,
            registry.getServerName(),
            (isNotBlank(registry.getServer()) ? " (" + registry.getServer() + ")" : ""));
        if (!context.isDryrun()) executeCommand(cmd);
    }

    private void logout(Registry registry) throws PackagerProcessingException {
        Command cmd = createCommand("logout");
        if (isNotBlank(registry.getServer())) {
            cmd.arg(registry.getServerName());
        }

        context.getLogger().debug(RB.$("docker.logout"),
            registry.getServerName(),
            (isNotBlank(registry.getServer()) ? " (" + registry.getServer() + ")" : ""));
        if (!context.isDryrun()) executeCommand(cmd);
    }

    @Override
    protected void fillPackagerProperties(Map<String, Object> props, Distribution distribution, ProcessingStep processingStep) throws PackagerProcessingException {
        fillDockerProperties(props, distribution, getPackager());
    }

    protected void fillDockerProperties(Map<String, Object> props,
                                        Distribution distribution,
                                        DockerConfiguration docker) throws PackagerProcessingException {
        props.put(KEY_DOCKER_BASE_IMAGE,
            resolveTemplate(docker.getBaseImage(), props));

        List<String> labels = new ArrayList<>();
        docker.getLabels().forEach((label, value) -> labels.add(passThrough("\"" + label + "\"=\"" +
            resolveTemplate(value, props) + "\"")));
        props.put(KEY_DOCKER_LABELS, labels);
        props.put(KEY_DOCKER_PRE_COMMANDS, docker.getPreCommands().stream()
            .map(c -> passThrough(resolveTemplate(c, props)))
            .collect(toList()));
        props.put(KEY_DOCKER_POST_COMMANDS, docker.getPostCommands().stream()
            .map(c -> passThrough(resolveTemplate(c, props)))
            .collect(toList()));
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName)
        throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = "executable".equals(fileName) ?
            outputDirectory.resolve("assembly").resolve(distribution.getExecutable().getName()) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    @Override
    protected void prepareWorkingCopy(Map<String, Object> props, Path directory, Distribution distribution) throws PackagerProcessingException, IOException {
        Path packageDirectory = (Path) props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);

        List<DockerSpec> activeSpecs = packager.getActiveSpecs();

        if (activeSpecs.isEmpty()) {
            for (String imageName : packager.getImageNames()) {
                copyDockerfiles(packageDirectory, resolveTemplate(imageName, props), directory, packager, false);
            }
        } else {
            // copy files that do not belong to specs
            prepareWorkingCopy(packageDirectory.resolve(ROOT), directory);

            for (DockerSpec spec : activeSpecs) {
                Map<String, Object> newProps = fillSpecProps(distribution, props, spec, ProcessingStep.PUBLISH);
                for (String imageName : spec.getImageNames()) {
                    copyDockerfiles(packageDirectory.resolve(spec.getName()), resolveTemplate(imageName, newProps), directory, spec, true);
                }
            }
        }
    }

    private void copyDockerfiles(Path source, String imageName, Path directory, DockerConfiguration docker, boolean isSpec) throws IOException {
        Path destination = directory;

        String[] parts = imageName.split("/");
        parts = parts[parts.length - 1].split(":");
        if (isSpec) {
            destination = directory.resolve(parts[0]);
        }

        if (packager.getRepository().isVersionedSubfolders()) {
            destination = directory.resolve(parts[1]);
        }

        Path assembly = destination.resolve("assembly");
        FileUtils.deleteFiles(assembly);

        Files.createDirectories(destination);
        prepareWorkingCopy(source, destination,
            path -> !docker.isUseLocalArtifact() &&
                "assembly".equals(path.getFileName().toString()));
    }
}
