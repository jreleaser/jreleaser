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
package org.jreleaser.engine.checksum;

import org.apache.commons.codec.digest.DigestUtils;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.util.Artifacts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Checksum {
    public static void collectAndWriteChecksums(JReleaserContext context) throws JReleaserException {
        context.getLogger().info("Calculating checksums");
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("checksum");

        List<String> checksums = new ArrayList<>();

        for (Artifact artifact : Artifacts.resolveFiles(context)) {
            readHash(context, artifact);
            checksums.add(artifact.getHash() + " " + artifact.getFilePath().getFileName());
        }

        for (Distribution distribution : context.getModel().getDistributions().values()) {
            for (Artifact artifact : distribution.getArtifacts()) {
                readHash(context, distribution, artifact);
                checksums.add(artifact.getHash() + " " + distribution.getName() + "/" +
                    artifact.getFilePath().getFileName());
            }
        }

        if (checksums.isEmpty()) {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
            context.getLogger().info("No files configured for checksum. Skipping");
            return;
        }

        Path checksumsFilePath = context.getChecksumsDirectory().resolve("checksums.txt");
        String newContent = String.join(System.lineSeparator(), checksums) + System.lineSeparator();

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
            Files.createDirectories(context.getChecksumsDirectory());
            Files.write(checksumsFilePath, newContent.getBytes());
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error writing checksums to " + checksumsFilePath.toAbsolutePath(), e);
        }

        context.getLogger().restorePrefix();
        context.getLogger().decreaseIndent();
    }

    public static void readHash(JReleaserContext context, Distribution distribution, Artifact artifact) throws JReleaserException {
        Path artifactPath = artifact.getResolvedPath(context, distribution);
        Path checksumPath = context.getChecksumsDirectory().resolve(distribution.getName()).resolve(artifactPath.getFileName() + ".sha256");

        readHash(context, artifact, artifactPath, checksumPath);
    }

    public static void readHash(JReleaserContext context, Artifact artifact) throws JReleaserException {
        Path artifactPath = artifact.getResolvedPath(context);
        Path checksumPath = context.getChecksumsDirectory().resolve(artifactPath.getFileName() + ".sha256");

        readHash(context, artifact, artifactPath, checksumPath);
    }

    private static void readHash(JReleaserContext context, Artifact artifact, Path artifactPath, Path checksumPath) throws JReleaserException {
        if (!Files.exists(artifactPath)) {
            throw new JReleaserException("Artifact does not exist. " + context.getBasedir().relativize(artifactPath));
        }

        if (!checksumPath.toFile().exists()) {
            context.getLogger().debug("Artifact checksum does not exist: {}", context.getBasedir().relativize(checksumPath));
            calculateHash(context, artifactPath, checksumPath);
        } else if (artifactPath.toFile().lastModified() > checksumPath.toFile().lastModified()) {
            context.getLogger().debug("Artifact {} is newer than {}",
                context.getBasedir().relativize(artifactPath),
                context.getBasedir().relativize(checksumPath));
            calculateHash(context, artifactPath, checksumPath);
        }

        try {
            context.getLogger().debug("Reading checksum: {}",
                context.getBasedir().relativize(checksumPath));
            artifact.setHash(new String(Files.readAllBytes(checksumPath)));
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error when reading hash from " + context.getBasedir().relativize(checksumPath), e);
        }
    }

    public static String calculateHash(JReleaserContext context, Path input, Path output) throws JReleaserException {
        try {
            context.getLogger().info("{}", context.getBasedir().relativize(input));
            String hashcode = DigestUtils.sha256Hex(Files.readAllBytes(input));
            output.toFile().getParentFile().mkdirs();
            Files.write(output, hashcode.getBytes());
            return hashcode;
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error calculating checksum for " + input, e);
        }
    }
}
