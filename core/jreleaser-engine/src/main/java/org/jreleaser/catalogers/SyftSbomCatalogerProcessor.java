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
package org.jreleaser.catalogers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.spi.catalog.CatalogProcessingException;
import org.jreleaser.model.spi.catalog.sbom.AbstractSbomCatalogerProcessor;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.tool.Syft;
import org.jreleaser.sdk.tool.ToolException;
import org.jreleaser.util.FileUtils;
import org.jreleaser.version.SemanticVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.util.ComparatorUtils.greaterThanOrEqualTo;
import static org.jreleaser.util.StringUtils.getFilename;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public class SyftSbomCatalogerProcessor extends AbstractSbomCatalogerProcessor<SyftSbomCataloger, org.jreleaser.model.internal.catalog.sbom.SyftSbomCataloger> {
    private org.jreleaser.model.internal.catalog.sbom.SyftSbomCataloger cataloger;

    public SyftSbomCatalogerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    public org.jreleaser.model.internal.catalog.sbom.SyftSbomCataloger getCataloger() {
        return cataloger;
    }

    @Override
    public void setCataloger(org.jreleaser.model.internal.catalog.sbom.SyftSbomCataloger cataloger) {
        this.cataloger = cataloger;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger.TYPE;
    }

    @Override
    public Result catalog() throws CatalogProcessingException {
        Set<Artifact> artifacts = collectArtifactsSelf(context);

        if (artifacts.isEmpty()) {
            context.getLogger().info(RB.$("catalog.no.artifacts"));
            return Result.NO_ARTIFACTS;
        }

        Syft syft = setupSyft(context);

        boolean executed = false;
        for (Artifact artifact : artifacts) {
            if (generateSbom(syft, artifact)) executed = true;
        }

        Path archivePath = cataloger.resolveArchivePath(context);
        if (executed || !Files.exists(archivePath)) {
            pack(artifacts, archivePath);
        }

        return executed ? Result.EXECUTED : Result.UPTODATE;
    }

    private void pack(Set<Artifact> candidates, Path archivePath) throws CatalogProcessingException {
        if (!cataloger.getPack().isEnabled()) return;

        context.getLogger().info(RB.$("catalog.sbom.pack", archivePath.getFileName()));

        String archiveFileName = getFilename(archivePath.getFileName().toString());

        try {
            Path tmp = Files.createTempDirectory(getType());
            Path workingDirectory = tmp.resolve(archiveFileName);
            Files.createDirectories(workingDirectory);
            for (Artifact artifact : doResolveArtifacts(candidates)) {
                Files.copy(artifact.getEffectivePath(), workingDirectory.resolve(artifact.getEffectivePath().getFileName()),
                    REPLACE_EXISTING, COPY_ATTRIBUTES);
            }
            FileUtils.zip(workingDirectory, archivePath);
        } catch (IOException e) {
            throw new CatalogProcessingException(RB.$("ERROR_catalog_unexpected_error_packing", archivePath.getFileName()), e);
        }
    }

    private Set<Artifact> doResolveArtifacts(Set<Artifact> candidates) {
        Set<Artifact> artifacts = new LinkedHashSet<>();

        Path catalogDirectory = resolveCatalogDirectory();

        for (org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger.Format format : cataloger.getFormats()) {
            for (Artifact artifact : candidates) {
                String artifactFile = artifact.getEffectivePath().getFileName().toString();
                Path targetPath = catalogDirectory.resolve(artifactFile + format.extension());
                artifacts.add(Artifact.of(targetPath));
            }
        }

        return artifacts;
    }

    private Syft setupSyft(JReleaserContext context) throws CatalogProcessingException {
        Syft syft = new Syft(context.asImmutable(), cataloger.getVersion());
        try {
            if (!syft.setup()) {
                throw new CatalogProcessingException(RB.$("tool_unavailable", "syft"));
            }
        } catch (ToolException e) {
            throw new CatalogProcessingException(e.getMessage(), e);
        }

        return syft;
    }

    private boolean generateSbom(Syft syft, Artifact artifact) throws CatalogProcessingException {
        Path catalogDirectory = resolveCatalogDirectory();

        try {
            Files.createDirectories(catalogDirectory);
        } catch (IOException e) {
            throw new CatalogProcessingException(RB.$("ERROR_assembler_create_directories"), e);
        }

        Path artifactPath = artifact.getEffectivePath();
        String artifactFile = artifactPath.getFileName().toString();

        SemanticVersion syftVersion = SemanticVersion.of(syft.getVersion());
        SemanticVersion syft99 = SemanticVersion.of("0.99.0");
        boolean deprecations = greaterThanOrEqualTo(syftVersion, syft99);

        boolean executed = false;
        for (SyftSbomCataloger.Format format : cataloger.getFormats()) {
            Path targetPath = catalogDirectory.resolve(artifactFile + format.extension());

            List<String> args = new ArrayList<>();
            if (deprecations) {
                args.add("--source-name");
                args.add(artifactFile);
                args.add("--output");
                args.add(format + "=" + targetPath.toAbsolutePath());
            } else {
                args.add("--output");
                args.add(format.toString());
                args.add("--name");
                args.add(artifactFile);
                args.add("--file");
                args.add(targetPath.toAbsolutePath().toString());
            }
            args.add(artifactFile);

            if (!Files.exists(targetPath)) {
                context.getLogger().debug(RB.$("catalog.sbom.not.exist"), context.relativizeToBasedir(targetPath));
                generateSbom(syft, artifactPath.getParent(), targetPath.getFileName(), args);
                executed = true;
            } else if (artifactPath.toFile().lastModified() > targetPath.toFile().lastModified()) {
                context.getLogger().debug(RB.$("checksum.file.newer"),
                    context.relativizeToBasedir(artifactPath),
                    context.relativizeToBasedir(targetPath));
                generateSbom(syft, artifactPath.getParent(), targetPath.getFileName(), args);
                executed = true;
            }
        }

        return executed;
    }

    private Path resolveCatalogDirectory() {
        return context.getCatalogsDirectory()
            .resolve("sbom").resolve(cataloger.getType());
    }

    private void generateSbom(Syft syft, Path executionDirectory, Path filename, List<String> args) throws CatalogProcessingException {
        context.getLogger().info(" - {}", filename);

        try {
            syft.invoke(executionDirectory, args);
        } catch (CommandException e) {
            throw new CatalogProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }
}
