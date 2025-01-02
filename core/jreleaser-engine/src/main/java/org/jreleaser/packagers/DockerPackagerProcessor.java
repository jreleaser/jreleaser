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
import org.jreleaser.model.internal.packagers.DockerConfiguration;
import org.jreleaser.model.internal.packagers.DockerPackager;
import org.jreleaser.model.internal.packagers.DockerSpec;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_PREPARE_DIRECTORY;
import static org.jreleaser.model.Constants.KEY_DOCKER_BASE_IMAGE;
import static org.jreleaser.model.Constants.KEY_DOCKER_LABELS;
import static org.jreleaser.model.Constants.KEY_DOCKER_POST_COMMANDS;
import static org.jreleaser.model.Constants.KEY_DOCKER_PRE_COMMANDS;
import static org.jreleaser.model.Constants.KEY_DOCKER_SPEC_NAME;
import static org.jreleaser.mustache.MustacheUtils.passThrough;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class DockerPackagerProcessor extends AbstractRepositoryPackagerProcessor<DockerPackager> {
    private static final String ROOT = "ROOT";

    public DockerPackagerProcessor(JReleaserContext context) {
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
            prepareSpec(distribution, props, distributionName, prepareDirectory, spec);
        }
    }

    private void prepareSpec(Distribution distribution,
                             TemplateContext props,
                             String distributionName,
                             Path prepareDirectory,
                             DockerSpec spec) throws IOException, PackagerProcessingException {
        TemplateContext newProps = fillSpecProps(distribution, props, spec);
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

    private TemplateContext fillSpecProps(Distribution distribution, TemplateContext props, DockerSpec spec) {
        List<Artifact> artifacts = singletonList(spec.getArtifact());
        TemplateContext newProps = fillProps(distribution, props);
        newProps.set(KEY_DOCKER_SPEC_NAME, spec.getName());
        fillDockerProperties(newProps, spec);
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
            packageDocker(distribution, props, packageDirectory, getPackager(), artifacts);
            return;
        }

        Path rootPrepareDirectory = getPrepareDirectory(props).resolve(ROOT);
        Path rootPackageDirectory = getPackageDirectory(props).resolve(ROOT);
        copyFiles(rootPrepareDirectory, rootPackageDirectory);

        for (DockerSpec spec : packager.getActiveSpecs()) {
            context.getLogger().debug(RB.$("distributions.action.packaging") + " {} spec", spec.getName());
            TemplateContext newProps = fillSpecProps(distribution, props, spec);
            packageDocker(distribution, newProps, packageDirectory.resolve(spec.getName()),
                spec, singletonList(spec.getArtifact()));
        }
    }

    protected void packageDocker(Distribution distribution,
                                 TemplateContext props,
                                 Path packageDirectory,
                                 DockerConfiguration docker,
                                 List<Artifact> artifacts) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);

        try {
            // copy files
            Path workingDirectory = prepareAssembly(distribution, props, packageDirectory, artifacts);

            Map<String, List<String>> tagNames = resolveTagNames(docker, props);
            List<String> tags = tagNames.values().stream()
                .flatMap(List::stream)
                .collect(toList());

            tags.forEach(tag -> context.getLogger().info(" - {}", tag));

            if (docker.getBuildx().isEnabled()) {
                // create builder if needed
                createBuildxBuilder(props, docker);
                configureAndExecuteBuildCommand(buildxBuildCommand(props, docker), workingDirectory, tags);
            } else {
                configureAndExecuteBuildCommand(buildCommand(props, docker), workingDirectory, tags);
            }
        } catch (IOException e) {
            throw new PackagerProcessingException(e);
        }
    }

    private void configureAndExecuteBuildCommand(Command cmd, Path workingDirectory, List<String> tags) throws PackagerProcessingException {
        if (!cmd.hasArg("-q") && !cmd.hasArg("--quiet")) {
            cmd.arg("--quiet");
        }
        cmd.arg("--file");
        cmd.arg(workingDirectory.resolve("Dockerfile").toAbsolutePath().toString());
        for (String tag : tags) {
            cmd.arg("--tag");
            cmd.arg(tag);
        }
        cmd.arg(workingDirectory.toAbsolutePath().toString());
        context.getLogger().debug(String.join(" ", cmd.getArgs()));

        // execute
        executeCommand(cmd);
    }

    private void createBuildxBuilder(TemplateContext props, DockerConfiguration docker) throws PackagerProcessingException {
        if (!docker.getBuildx().isCreateBuilder()) return;

        Command cmd = new Command("docker" + (PlatformUtils.isWindows() ? ".exe" : ""))
            .arg("buildx")
            .arg("ls");
        Command.Result result = executeCommand(cmd);
        if (result.getOut().contains("jreleaser")) return;

        cmd = buildxCreateCommand(props, docker);
        context.getLogger().debug(String.join(" ", cmd.getArgs()));
        executeCommand(cmd);
    }

    private Path prepareAssembly(Distribution distribution,
                                 TemplateContext props,
                                 Path packageDirectory,
                                 List<Artifact> artifacts) throws IOException, PackagerProcessingException {
        copyPreparedFiles(props);
        Path assemblyDirectory = packageDirectory.resolve("assembly");

        Files.createDirectories(assemblyDirectory);

        for (Artifact artifact : artifacts) {
            Path artifactPath = artifact.getEffectivePath(context, distribution);
            if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.BINARY) {
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

    private Command buildCommand(TemplateContext props, DockerConfiguration docker) {
        Command cmd = createCommand("build");
        for (int i = 0; i < docker.getBuildArgs().size(); i++) {
            String arg = docker.getBuildArgs().get(i);
            if (arg.contains("{{")) {
                cmd.arg(resolveTemplate(arg, props).trim());
            } else {
                cmd.arg(arg.trim());
            }
        }
        return cmd;
    }

    private Command buildxBuildCommand(TemplateContext props, DockerConfiguration docker) {
        Command cmd = createCommand("buildx");
        cmd.arg("build");

        List<String> platforms = new ArrayList<>();
        for (int i = 0; i < docker.getBuildx().getPlatforms().size(); i++) {
            String arg = docker.getBuildx().getPlatforms().get(i);
            if (arg.contains("{{")) {
                platforms.add(resolveTemplate(arg, props).trim());
            } else {
                platforms.add(arg.trim());
            }
        }
        cmd.arg("--platform")
            .arg(String.join(",", platforms));

        for (int i = 0; i < docker.getBuildArgs().size(); i++) {
            String arg = docker.getBuildArgs().get(i);
            if (arg.contains("{{")) {
                cmd.arg(resolveTemplate(arg, props).trim());
            } else {
                cmd.arg(arg.trim());
            }
        }
        return cmd;
    }

    private Command buildxCreateCommand(TemplateContext props, DockerConfiguration docker) {
        Command cmd = createCommand("buildx");
        cmd.arg("create");
        for (int i = 0; i < docker.getBuildx().getCreateBuilderFlags().size(); i++) {
            String arg = docker.getBuildx().getCreateBuilderFlags().get(i);
            if (arg.contains("{{")) {
                cmd.arg(resolveTemplate(arg, props).trim());
            } else {
                cmd.arg(arg.trim());
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
    public void publishDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        if (packager.getActiveSpecs().isEmpty()) {
            publishToRepository(distribution, props);
            super.publishDistribution(distribution, props);
            cleanupBuilder(props, getPackager());
            return;
        }

        publishToRepository(distribution, props);
        for (DockerSpec spec : packager.getActiveSpecs()) {
            context.getLogger().debug(RB.$("distributions.action.publishing") + " {} spec", spec.getName());
            TemplateContext newProps = fillSpecProps(distribution, props, spec);
            publishDocker(newProps, spec);
        }
        cleanupBuilder(props, packager.getActiveSpecs());
    }

    private void publishToRepository(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        super.doPublishDistribution(distribution, fillProps(distribution, props));
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        publishDocker(props, getPackager());
    }

    protected void publishDocker(TemplateContext props, DockerConfiguration docker) throws PackagerProcessingException {
        Map<String, List<String>> tagNames = resolveTagNames(docker, props);

        if (context.isDryrun()) {
            for (Map.Entry<String, List<String>> e : tagNames.entrySet()) {
                Set<String> uniqueImageNames = e.getValue().stream()
                    .map(tag -> tag.split(":")[0])
                    .collect(toSet());
                for (String imageName : uniqueImageNames) {
                    context.getLogger().info(" - {}", imageName);
                }
            }
            return;
        }

        for (DockerConfiguration.Registry registry : docker.getRegistries()) {
            login(registry);
        }

        if (docker.getBuildx().isEnabled()) {
            Path workingDirectory = props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
            List<String> tags = tagNames.values().stream()
                .flatMap(List::stream)
                .collect(toList());

            // command line
            Command cmd = buildxBuildCommand(props, docker);
            if (!cmd.hasArg("-q") && !cmd.hasArg("--quiet")) {
                cmd.arg("--quiet");
            }
            cmd.arg("--file");
            cmd.arg(workingDirectory.resolve("Dockerfile").toAbsolutePath().toString());
            for (String tag : tags) {
                cmd.arg("--tag");
                cmd.arg(tag);
            }
            cmd.arg("--push");
            cmd.arg(workingDirectory.toAbsolutePath().toString());
            context.getLogger().debug(String.join(" ", cmd.getArgs()));

            // execute
            executeCommand(cmd);
        } else {
            for (Map.Entry<String, List<String>> e : tagNames.entrySet()) {
                Set<String> uniqueImageNames = e.getValue().stream()
                    .map(tag -> tag.split(":")[0])
                    .collect(toSet());
                for (String imageName : uniqueImageNames) {
                    push(e.getKey(), imageName);
                }
            }
        }

        for (DockerConfiguration.Registry registry : docker.getRegistries()) {
            logout(registry);
        }
    }

    private void cleanupBuilder(TemplateContext props, DockerConfiguration docker) throws PackagerProcessingException {
        if (docker.getBuildx().isEnabled() && docker.getBuildx().isCreateBuilder()) {
            int i = docker.getBuildx().getCreateBuilderFlags().indexOf("--name");
            String builderName = docker.getBuildx().getCreateBuilderFlags().get(i + 1);
            Command cmd = createCommand("buildx")
                .arg("rm")
                .arg(resolveTemplate(builderName, props).trim());

            executeCommand(cmd);
        }
    }

    private void cleanupBuilder(TemplateContext props, List<DockerSpec> specs) throws PackagerProcessingException {
        Set<String> builderNames = new LinkedHashSet<>();
        for (DockerSpec spec : specs) {
            if (spec.getBuildx().isEnabled() && spec.getBuildx().isCreateBuilder()) {
                int i = spec.getBuildx().getCreateBuilderFlags().indexOf("--name");
                builderNames.add(spec.getBuildx().getCreateBuilderFlags().get(i + 1));
            }
        }

        for (String builderName : builderNames) {
            Command cmd = createCommand("buildx")
                .arg("rm")
                .arg(resolveTemplate(builderName, props).trim());

            executeCommand(cmd);
        }
    }

    private void login(DockerConfiguration.Registry registry) throws PackagerProcessingException {
        if (registry.isExternalLogin()) return;

        Command cmd = createCommand("login");
        if (isNotBlank(registry.getServer())) {
            cmd.arg(registry.getServer());
        }
        cmd.arg("-u");
        cmd.arg(registry.getUsername());
        cmd.arg("-p");
        cmd.arg(registry.getPassword());

        ByteArrayInputStream in = new ByteArrayInputStream((registry.getPassword() + System.lineSeparator()).getBytes(UTF_8));

        context.getLogger().debug(RB.$("docker.login"),
            registry.getServerName(),
            isNotBlank(registry.getServer()) ? " (" + registry.getServer() + ")" : "");
        if (!context.isDryrun()) executeCommand(cmd, in);
    }

    private Map<String, List<String>> resolveTagNames(DockerConfiguration docker, TemplateContext props) {
        Map<String, List<String>> tags = new LinkedHashMap<>();

        for (DockerConfiguration.Registry registry : docker.getRegistries()) {
            for (String imageName : docker.getImageNames()) {
                imageName = resolveTemplate(imageName, props).toLowerCase(Locale.ENGLISH);

                String tag = imageName;
                String serverName = registry.getServerName();
                String server = registry.getServer();
                String repositoryName = registry.getRepositoryName();

                // if serverName == DEFAULT
                //   tag: docker.io/repositoryName/imageName
                // else
                //   tag: server/repositoryName/imageName

                if (DockerConfiguration.Registry.DEFAULT_NAME.equals(serverName)) {
                    if (!tag.startsWith(repositoryName)) {
                        int pos = tag.indexOf("/");
                        if (pos < 0) {
                            tag = server + "/" + repositoryName + "/" + tag;
                        } else {
                            tag = server + "/" + repositoryName + tag.substring(pos);
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

                tags.computeIfAbsent(server, k -> new ArrayList<>()).add(tag);
            }
        }

        return tags;
    }

    private void push(String server, String imageName) throws PackagerProcessingException {
        Command cmd = createCommand("push")
            .arg("--quiet")
            .arg("--all-tags")
            .arg(imageName);

        context.getLogger().info(" - {}", imageName);
        context.getLogger().debug(RB.$("docker.push", imageName, server));
        context.getLogger().debug(String.join(" ", cmd.getArgs()));
        if (!context.isDryrun()) executeCommand(cmd);
    }

    private void logout(DockerConfiguration.Registry registry) throws PackagerProcessingException {
        if (registry.isExternalLogin()) return;

        Command cmd = createCommand("logout");
        if (isNotBlank(registry.getServer())) {
            cmd.arg(registry.getServerName());
        }

        context.getLogger().debug(RB.$("docker.logout"),
            registry.getServerName(),
            isNotBlank(registry.getServer()) ? " (" + registry.getServer() + ")" : "");
        if (!context.isDryrun()) executeCommand(cmd);
    }

    @Override
    protected void fillPackagerProperties(TemplateContext props, Distribution distribution) {
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, distribution.getJava().getMainClass());
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_MODULE, distribution.getJava().getMainModule());
        fillDockerProperties(props, getPackager());
    }

    protected void fillDockerProperties(TemplateContext props, DockerConfiguration docker) {
        props.set(KEY_DOCKER_BASE_IMAGE,
            resolveTemplate(docker.getBaseImage(), props));

        List<String> labels = new ArrayList<>();
        docker.getLabels().forEach((label, value) -> labels.add(passThrough("\"" + label + "\"=\"" +
            resolveTemplate(value, props) + "\"")));
        props.set(KEY_DOCKER_LABELS, labels);
        props.set(KEY_DOCKER_PRE_COMMANDS, docker.getPreCommands().stream()
            .map(c -> passThrough(resolveTemplate(c, props)))
            .collect(toList()));
        props.set(KEY_DOCKER_POST_COMMANDS, docker.getPostCommands().stream()
            .map(c -> passThrough(resolveTemplate(c, props)))
            .collect(toList()));
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

        List<DockerSpec> activeSpecs = packager.getActiveSpecs();

        if (activeSpecs.isEmpty()) {
            for (String imageName : packager.getImageNames()) {
                copyDockerfiles(packageDirectory, resolveTemplate(imageName, props), directory, packager, false);
            }
        } else {
            // copy files that do not belong to specs
            prepareWorkingCopy(packageDirectory.resolve(ROOT), directory);

            for (DockerSpec spec : activeSpecs) {
                TemplateContext newProps = fillSpecProps(distribution, props, spec);
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

        if (packager.getPackagerRepository().isVersionedSubfolders()) {
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
