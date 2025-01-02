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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jreleaser.bundle.RB;
import org.jreleaser.engine.deploy.maven.ArtifactDeployers;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.SlsaCataloger;
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jreleaser.engine.catalog.CatalogerSupport.fireCatalogEvent;
import static org.jreleaser.engine.checksum.Checksum.readHash;
import static org.jreleaser.model.api.catalog.SlsaCataloger.KEY_SKIP_SLSA;
import static org.jreleaser.model.internal.JReleaserSupport.supportedMavenDeployers;
import static org.jreleaser.util.Algorithm.SHA_256;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.7.0
 */
public final class Slsa {
    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";

    private Slsa() {
        // noop
    }

    public static void catalog(JReleaserContext context) {
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("slsa");

        SlsaCataloger slsa = context.getModel().getCatalog().getSlsa();
        if (!slsa.isEnabled()) {
            context.getLogger().info(RB.$("catalogers.not.enabled"));
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
            return;
        }

        try {
            fireCatalogEvent(ExecutionEvent.before(JReleaserCommand.CATALOG.toStep()), context, slsa);
            attestation(context, slsa);
            fireCatalogEvent(ExecutionEvent.success(JReleaserCommand.CATALOG.toStep()), context, slsa);
        } catch (CatalogProcessingException e) {
            fireCatalogEvent(ExecutionEvent.failure(JReleaserCommand.CATALOG.toStep(), e), context, slsa);
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        } finally {
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
        }
    }

    private static void attestation(JReleaserContext context, SlsaCataloger slsa) throws CatalogProcessingException {
        Set<PathMatcher> includes = new LinkedHashSet<>();
        Set<PathMatcher> excludes = new LinkedHashSet<>();

        FileSystem fileSystem = FileSystems.getDefault();
        for (String s : slsa.getIncludes()) {
            includes.add(fileSystem.getPathMatcher(normalize(s)));
        }
        for (String s : slsa.getExcludes()) {
            excludes.add(fileSystem.getPathMatcher(normalize(s)));
        }

        if (includes.isEmpty()) {
            includes.add(fileSystem.getPathMatcher(GLOB_PREFIX + "**/*"));
        }

        Attestation attestation = new Attestation(slsa.getResolvedAttestationName(context));

        context.getLogger().info(attestation.getName());

        if (slsa.isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActiveAndSelected() || artifact.extraPropertyIsTrue(KEY_SKIP_SLSA) ||
                    artifact.isOptional(context) && !artifact.resolvedPathExists() &&
                        !isIncluded(context, artifact, includes, excludes)) continue;
                readHash(context, SHA_256, artifact);
                addSubject(context, attestation, artifact);
            }
        }

        if (slsa.isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActiveAndSelected()) continue;
                    artifact.getEffectivePath(context, distribution);
                    if (artifact.isOptional(context) && !artifact.resolvedPathExists() &&
                        !isIncluded(context, artifact, includes, excludes)) continue;
                    readHash(context, distribution, SHA_256, artifact);
                    addSubject(context, attestation, artifact);
                }
            }
        }

        if (slsa.isDeployables()) {
            for (Deployable deployable : collectDeployables(context)) {
                if (!deployable.isPom() && !deployable.isArtifact()) continue;
                Artifact artifact = Artifact.of(deployable.getLocalPath());
                if (!isIncluded(context, artifact, includes, excludes)) continue;
                readHash(context, SHA_256, artifact);
                addSubject(context, attestation, artifact);
            }
        }

        if (attestation.getSubjects().isEmpty()) {
            context.getLogger().info(RB.$("catalog.no.artifacts"));
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
            return;
        }

        String newContent = null;
        try {
            Attestations attestations = new Attestations();
            attestations.getAttestations().add(attestation);

            ObjectMapper objectMapper = new ObjectMapper();
            newContent = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(attestations) + System.lineSeparator();
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        }

        Path attestationFile = context.getCatalogsDirectory()
            .resolve(slsa.getType())
            .resolve(attestation.getName());

        try {
            if (Files.exists(attestationFile)) {
                String oldContent = new String(Files.readAllBytes(attestationFile), UTF_8);
                if (newContent.equals(oldContent)) {
                    // no need to write down the same content
                    context.getLogger().info(RB.$("catalog.slsa.not.changed"));
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

    private static boolean isIncluded(JReleaserContext context, Artifact artifact, Set<PathMatcher> includes, Set<PathMatcher> excludes) {
        Path path = artifact.getEffectivePath(context);

        return includes.stream().anyMatch(matcher -> matcher.matches(path)) &&
            excludes.stream().noneMatch(matcher -> matcher.matches(path));
    }

    private static void addSubject(JReleaserContext context, Attestation attestation, Artifact artifact) {
        String artifactFileName = artifact.getEffectivePath(context).getFileName().toString();
        attestation.addSubject(artifactFileName, artifact.getHash(SHA_256));
        context.getLogger().debug("- " + artifactFileName);
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

    private static class Attestations {
        private final Set<Attestation> attestations = new LinkedHashSet<>();

        public Integer getVersion() {
            return 1;
        }

        public Set<Attestation> getAttestations() {
            return attestations;
        }
    }

    private static class Attestation {
        private final String name;
        private final Set<Subject> subjects = new TreeSet<>();

        private Attestation(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Set<Subject> getSubjects() {
            return subjects;
        }

        public void addSubject(String name, String sha256) {
            subjects.add(new Subject(name, sha256));
        }
    }

    private static class Subject implements Comparable<Subject> {
        private final String name;
        private final Digest digest;

        private Subject(String name, String sha256) {
            this.name = name;
            this.digest = new Digest(sha256);
        }

        public String getName() {
            return name;
        }

        public Digest getDigest() {
            return digest;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Subject subject = (Subject) o;
            return name.equals(subject.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public int compareTo(Subject o) {
            if (null == o) return -1;
            return this.name.compareTo(o.name);
        }
    }

    private static class Digest {
        private final String sha256;

        private Digest(String sha256) {
            this.sha256 = sha256;
        }

        public String getSha256() {
            return sha256;
        }
    }
}
