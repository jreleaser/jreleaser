/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.engine.checksum;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.util.Artifacts;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.ChecksumUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Checksum {
    public static void collectAndWriteChecksums(JReleaserContext context) throws JReleaserException {
        context.getLogger().info("Calculating checksums");
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("checksum");

        Map<Algorithm, List<String>> checksums = new LinkedHashMap<>();

        for (Artifact artifact : Artifacts.resolveFiles(context)) {
            for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                readHash(context, algorithm, artifact);
                List<String> list = checksums.computeIfAbsent(algorithm, k -> new ArrayList<>());
                list.add(artifact.getHash(algorithm) + " " + artifact.getEffectivePath(context).getFileName());
            }
        }

        for (Distribution distribution : context.getModel().getActiveDistributions()) {
            for (Artifact artifact : distribution.getArtifacts()) {
                for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                    readHash(context, distribution, algorithm, artifact);
                    List<String> list = checksums.computeIfAbsent(algorithm, k -> new ArrayList<>());
                    list.add(artifact.getHash(algorithm) + " " + distribution.getName() + "/" +
                        artifact.getEffectivePath(context).getFileName());
                }
            }
        }

        if (checksums.isEmpty()) {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
            context.getLogger().info("No files configured for checksum. Skipping");
            return;
        }

        checksums.forEach((algorithm, list) -> {
            Path checksumsFilePath = context.getChecksumsDirectory()
                .resolve(context.getModel().getChecksum().getResolvedName(context, algorithm));

            String newContent = String.join(System.lineSeparator(), list) + System.lineSeparator();

            try {
                if (Files.exists(checksumsFilePath)) {
                    String oldContent = new String(Files.readAllBytes(checksumsFilePath));
                    if (newContent.equals(oldContent)) {
                        // no need to write down the same content
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
                    Files.createDirectories(context.getChecksumsDirectory());
                    Files.write(checksumsFilePath, newContent.getBytes());
                } else {
                    Files.deleteIfExists(checksumsFilePath);
                }
            } catch (IOException e) {
                throw new JReleaserException("Unexpected error writing checksums to " + checksumsFilePath.toAbsolutePath(), e);
            }
        });

        context.getLogger().restorePrefix();
        context.getLogger().decreaseIndent();
    }

    public static void readHash(JReleaserContext context, Distribution distribution, Algorithm algorithm, Artifact artifact) throws JReleaserException {
        Path artifactPath = artifact.getEffectivePath(context, distribution);
        Path checksumPath = context.getChecksumsDirectory().resolve(distribution.getName())
            .resolve(artifactPath.getFileName() + "." + algorithm.formatted());

        readHash(context, algorithm, artifact, artifactPath, checksumPath);
    }

    public static void readHash(JReleaserContext context, Algorithm algorithm, Artifact artifact) throws JReleaserException {
        Path artifactPath = artifact.getEffectivePath(context);
        Path checksumPath = context.getChecksumsDirectory()
            .resolve(artifactPath.getFileName() + "." + algorithm.formatted());

        readHash(context, algorithm, artifact, artifactPath, checksumPath);
    }

    private static void readHash(JReleaserContext context,
                                 Algorithm algorithm,
                                 Artifact artifact,
                                 Path artifactPath,
                                 Path checksumPath) throws JReleaserException {
        if (!Files.exists(artifactPath)) {
            throw new JReleaserException("Artifact does not exist. " + context.relativizeToBasedir(artifactPath));
        }

        if (!Files.exists(checksumPath)) {
            context.getLogger().debug("checksum does not exist: {}", context.relativizeToBasedir(checksumPath));
            calculateHash(context, artifactPath, checksumPath, algorithm);
        } else if (artifactPath.toFile().lastModified() > checksumPath.toFile().lastModified()) {
            context.getLogger().debug("{} is newer than {}",
                context.relativizeToBasedir(artifactPath),
                context.relativizeToBasedir(checksumPath));
            calculateHash(context, artifactPath, checksumPath, algorithm);
        }

        try {
            context.getLogger().debug("reading {}",
                context.relativizeToBasedir(checksumPath));
            artifact.setHash(algorithm, new String(Files.readAllBytes(checksumPath)));
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error when reading hash from " + context.relativizeToBasedir(checksumPath), e);
        }
    }

    public static String calculateHash(JReleaserContext context, Path input, Path output) throws JReleaserException {
        return calculateHash(context, input, output, Algorithm.SHA_256);
    }

    public static String calculateHash(JReleaserContext context, Path input, Path output, Algorithm algorithm) throws JReleaserException {
        try {
            context.getLogger().info("{}.{}", context.relativizeToBasedir(input), algorithm.formatted());
            String hashcode = ChecksumUtils.checksum(algorithm, Files.readAllBytes(input));
            output.toFile().getParentFile().mkdirs();
            Files.write(output, hashcode.getBytes());
            return hashcode;
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error calculating checksum for " + input, e);
        }
    }
}
