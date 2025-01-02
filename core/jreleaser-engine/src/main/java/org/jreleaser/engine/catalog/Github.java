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
package org.jreleaser.engine.catalog;

import org.jreleaser.bundle.RB;
import org.jreleaser.engine.deploy.maven.ArtifactDeployers;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.GithubCataloger;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.deploy.maven.Maven;
import org.jreleaser.model.internal.deploy.maven.MavenDeployer;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.catalog.CatalogProcessingException;
import org.jreleaser.model.spi.deploy.maven.Deployable;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jreleaser.engine.catalog.CatalogerSupport.fireCatalogEvent;
import static org.jreleaser.model.api.catalog.GithubCataloger.KEY_SKIP_GITHUB;
import static org.jreleaser.model.internal.JReleaserSupport.supportedMavenDeployers;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.13.0
 */
public final class Github {
    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";

    private Github() {
        // noop
    }

    public static void catalog(JReleaserContext context) {
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("github");

        GithubCataloger github = context.getModel().getCatalog().getGithub();
        if (!github.isEnabled()) {
            context.getLogger().info(RB.$("catalogers.not.enabled"));
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
            return;
        }

        try {
            fireCatalogEvent(ExecutionEvent.before(JReleaserCommand.CATALOG.toStep()), context, github);
            attestation(context, github);
            fireCatalogEvent(ExecutionEvent.success(JReleaserCommand.CATALOG.toStep()), context, github);
        } catch (CatalogProcessingException e) {
            fireCatalogEvent(ExecutionEvent.failure(JReleaserCommand.CATALOG.toStep(), e), context, github);
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        } finally {
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
        }
    }

    private static void attestation(JReleaserContext context, GithubCataloger github) throws CatalogProcessingException {
        Set<PathMatcher> includes = new LinkedHashSet<>();
        Set<PathMatcher> excludes = new LinkedHashSet<>();

        FileSystem fileSystem = FileSystems.getDefault();
        for (String s : github.getIncludes()) {
            includes.add(fileSystem.getPathMatcher(normalize(s)));
        }
        for (String s : github.getExcludes()) {
            excludes.add(fileSystem.getPathMatcher(normalize(s)));
        }

        if (includes.isEmpty()) {
            includes.add(fileSystem.getPathMatcher(GLOB_PREFIX + "**/*"));
        }

        List<String> subjects = new ArrayList<>();
        String attestationName = github.getResolvedAttestationName(context);

        context.getLogger().info(attestationName);

        if (github.isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActiveAndSelected() || artifact.extraPropertyIsTrue(KEY_SKIP_GITHUB) ||
                    artifact.isOptional(context) && !artifact.resolvedPathExists()) continue;
                addSubject(context, subjects, artifact, includes, excludes);
            }
        }

        if (github.isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActiveAndSelected()) continue;
                    artifact.getEffectivePath(context, distribution);
                    if (artifact.isOptional(context) && !artifact.resolvedPathExists()) continue;
                    addSubject(context, subjects, artifact, includes, excludes);
                }
            }
        }

        if (github.isDeployables()) {
            for (Deployable deployable : collectDeployables(context)) {
                if (!deployable.isPom() && !deployable.isArtifact()) continue;
                Artifact artifact = Artifact.of(deployable.getLocalPath());
                addSubject(context, subjects, artifact, includes, excludes);
            }
        }

        if (subjects.isEmpty()) {
            context.getLogger().info(RB.$("catalog.no.artifacts"));
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
            return;
        }

        String newContent = String.join(System.lineSeparator(), subjects);
        Path attestationFile = context.getCatalogsDirectory()
            .resolve(github.getType())
            .resolve(attestationName);

        try {
            if (Files.exists(attestationFile)) {
                String oldContent = new String(Files.readAllBytes(attestationFile), UTF_8);
                if (newContent.equals(oldContent)) {
                    // no need to write down the same content
                    context.getLogger().info(RB.$("catalog.github.not.changed"));
                    context.getLogger().restorePrefix();
                    context.getLogger().decreaseIndent();
                    return;
                }
            }
        } catch (IOException ignored) {
            // OK
        }

        try {
            if (isNotBlank(newContent)) {
                Files.createDirectories(attestationFile.getParent());
                Files.write(attestationFile, newContent.getBytes(UTF_8));
            } else {
                Files.deleteIfExists(attestationFile);
            }
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_writing_file", attestationFile.toAbsolutePath()), e);
        }
    }

    private static String normalize(String pattern) {
        if (pattern.startsWith(GLOB_PREFIX) || pattern.startsWith(REGEX_PREFIX)) return pattern;
        return GLOB_PREFIX + pattern;
    }

    private static void addSubject(JReleaserContext context, List<String> subjects, Artifact artifact, Set<PathMatcher> includes, Set<PathMatcher> excludes) {
        Path path = artifact.getEffectivePath(context);
        String artifactFileName = path.toString();

        if (includes.stream().anyMatch(matcher -> matcher.matches(path)) &&
            excludes.stream().noneMatch(matcher -> matcher.matches(path))) {
            subjects.add(artifactFileName);
            context.getLogger().debug("- " + artifact.getEffectivePath(context).getFileName());
        }
    }

    private static Set<Deployable> collectDeployables(JReleaserContext context) {
        Set<String> stagingRepositories = new TreeSet<>();
        Set<Deployable> deployables = new TreeSet<>();
        Maven maven = context.getModel().getDeploy().getMaven();

        if (!context.getIncludedDeployerTypes().isEmpty()) {
            for (String deployerType : context.getIncludedDeployerTypes()) {
                if (!supportedMavenDeployers().contains(deployerType)) continue;

                Map<String, MavenDeployer<?>> deployers = maven.findMavenDeployersByType(deployerType);

                if (deployers.isEmpty()) return deployables;

                if (!context.getIncludedDeployerNames().isEmpty()) {
                    for (String deployerName : context.getIncludedDeployerNames()) {
                        if (!deployers.containsKey(deployerName)) continue;

                        MavenDeployer<?> deployer = deployers.get(deployerName);
                        if (!deployer.isEnabled()) continue;

                        handleDeployer(context, stagingRepositories, deployables, deployer);
                    }
                } else {
                    for (MavenDeployer<?> deployer : deployers.values()) {
                        handleDeployer(context, stagingRepositories, deployables, deployer);
                    }
                }
            }
        } else if (!context.getIncludedDeployerNames().isEmpty()) {
            for (String deployerName : context.getIncludedDeployerNames()) {
                maven.findAllActiveMavenDeployers().stream()
                    .filter(a -> deployerName.equals(a.getName()))
                    .forEach(deployer -> handleDeployer(context, stagingRepositories, deployables, deployer));
            }
        } else {
            for (MavenDeployer<?> deployer : maven.findAllActiveMavenDeployers()) {
                if (context.getExcludedDeployerTypes().contains(deployer.getType()) ||
                    context.getExcludedDeployerNames().contains(deployer.getName())) {
                    continue;
                }

                handleDeployer(context, stagingRepositories, deployables, deployer);
            }
        }

        return deployables;
    }

    private static void handleDeployer(JReleaserContext context, Set<String> stagingRepositories, Set<Deployable> deployables, MavenDeployer<?> deployer) {
        org.jreleaser.model.spi.deploy.maven.MavenDeployer<?, ?> artifactMavenDeployer = ArtifactDeployers.findMavenDeployer(context, deployer);
        for (String stagingRepository : deployer.getStagingRepositories()) {
            if (stagingRepositories.contains(stagingRepository)) continue;
            artifactMavenDeployer.collectDeployables(deployables, stagingRepository);
        }
    }
}
