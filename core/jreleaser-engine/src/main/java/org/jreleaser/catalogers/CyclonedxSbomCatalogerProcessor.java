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
import org.jreleaser.model.api.catalog.sbom.CyclonedxSbomCataloger;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.spi.catalog.CatalogProcessingException;
import org.jreleaser.model.spi.catalog.sbom.AbstractSbomCatalogerProcessor;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.tool.Cyclonedx;
import org.jreleaser.sdk.tool.ToolException;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.util.StringUtils.getFilename;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public class CyclonedxSbomCatalogerProcessor extends AbstractSbomCatalogerProcessor<CyclonedxSbomCataloger, org.jreleaser.model.internal.catalog.sbom.CyclonedxSbomCataloger> {
    private org.jreleaser.model.internal.catalog.sbom.CyclonedxSbomCataloger cataloger;

    public CyclonedxSbomCatalogerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    public org.jreleaser.model.internal.catalog.sbom.CyclonedxSbomCataloger getCataloger() {
        return cataloger;
    }

    @Override
    public void setCataloger(org.jreleaser.model.internal.catalog.sbom.CyclonedxSbomCataloger cataloger) {
        this.cataloger = cataloger;
    }

    @Override
    public String getType() {
        return CyclonedxSbomCataloger.TYPE;
    }

    @Override
    public Result catalog() throws CatalogProcessingException {
        Set<Artifact> artifacts = collectArtifactsSelf(context);

        if (artifacts.isEmpty()) {
            context.getLogger().info(RB.$("catalog.no.artifacts"));
            return Result.NO_ARTIFACTS;
        }

        Cyclonedx cyclonedx = setupCyclonedx(context);

        boolean executed = false;
        for (Artifact artifact : artifacts) {
            if (generateSbom(cyclonedx, artifact)) executed = true;
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

        for (CyclonedxSbomCataloger.Format format : cataloger.getFormats()) {
            for (Artifact artifact : candidates) {
                String artifactFile = artifact.getEffectivePath().getFileName().toString();
                Path targetPath = catalogDirectory.resolve(artifactFile + format.extension());
                artifacts.add(Artifact.of(targetPath));
            }
        }

        return artifacts;
    }

    private Cyclonedx setupCyclonedx(JReleaserContext context) throws CatalogProcessingException {
        Cyclonedx cyclonedx = new Cyclonedx(context.asImmutable(), cataloger.getVersion());
        try {
            if (!cyclonedx.setup()) {
                throw new CatalogProcessingException(RB.$("tool_unavailable", "cyclonedx"));
            }
        } catch (ToolException e) {
            throw new CatalogProcessingException(e.getMessage(), e);
        }

        return cyclonedx;
    }

    private boolean generateSbom(Cyclonedx cyclonedx, Artifact artifact) throws CatalogProcessingException {
        Path catalogDirectory = resolveCatalogDirectory();
        Path artifactPath = artifact.getEffectivePath();
        String artifactFile = artifactPath.getFileName().toString();

        try {
            Files.createDirectories(catalogDirectory);
        } catch (IOException e) {
            throw new CatalogProcessingException(RB.$("ERROR_assembler_create_directories"), e);
        }

        boolean executed = false;
        for (CyclonedxSbomCataloger.Format format : cataloger.getFormats()) {
            Path targetPath = catalogDirectory.resolve(artifactFile + format.extension());

            List<String> args = new ArrayList<>();
            args.add("add");
            args.add("files");
            args.add("--no-input");
            args.add("--output-format");
            args.add(format.toString());
            args.add("--output-file");
            args.add(targetPath.toAbsolutePath().toString());
            args.add("--include");
            args.add(artifactFile);

            if (!Files.exists(targetPath)) {
                context.getLogger().debug(RB.$("catalog.sbom.not.exist"), context.relativizeToBasedir(targetPath));
                generateSbom(cyclonedx, artifactPath.getParent(), targetPath.getFileName(), args);
                executed = true;
            } else if (artifactPath.toFile().lastModified() > targetPath.toFile().lastModified()) {
                context.getLogger().debug(RB.$("checksum.file.newer"),
                    context.relativizeToBasedir(artifactPath),
                    context.relativizeToBasedir(targetPath));
                generateSbom(cyclonedx, artifactPath.getParent(), targetPath.getFileName(), args);
                executed = true;
            }
        }

        return executed;
    }

    private Path resolveCatalogDirectory() {
        return context.getCatalogsDirectory()
            .resolve("sbom").resolve(cataloger.getType());
    }

    private void generateSbom(Cyclonedx cyclonedx, Path executionDirectory, Path filename, List<String> args) throws CatalogProcessingException {
        context.getLogger().info(" - {}", filename);

        try {
            cyclonedx.invoke(executionDirectory, args);
        } catch (CommandException e) {
            throw new CatalogProcessingException(RB.$("ERROR_unexpected_error"), e);
        }
    }
}
