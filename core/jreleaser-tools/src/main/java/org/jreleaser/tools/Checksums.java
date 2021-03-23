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
package org.jreleaser.tools;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.util.Logger.DEBUG_TAB;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Checksums {
    public static void collectAndWriteChecksums(JReleaserContext context) throws JReleaserException {
        context.getLogger().info("Calculating checksums");

        List<String> checksums = new ArrayList<>();

        for (Artifact artifact : context.getModel().getFiles()) {
            readHash(context, artifact);
            checksums.add(artifact.getHash() + " " + Paths.get(artifact.getPath()).getFileName());
        }

        for (Distribution distribution : context.getModel().getDistributions().values()) {
            for (Artifact artifact : distribution.getArtifacts()) {
                readHash(context, distribution.getName(), artifact);
                checksums.add(artifact.getHash() + " " + distribution.getName() + "/" +
                    Paths.get(artifact.getPath()).getFileName());
            }
        }

        Path checksumsFilePath = context.getChecksumsDirectory().resolve("checksums.txt");
        try {
            Files.createDirectories(context.getChecksumsDirectory());
            Files.write(checksumsFilePath, (String.join(System.lineSeparator(), checksums) + System.lineSeparator()).getBytes());
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error writing checksums to " + checksumsFilePath.toAbsolutePath(), e);
        }
    }

    public static void readHash(JReleaserContext context, String distributionName, Artifact artifact) throws JReleaserException {
        Path artifactPath = context.getBasedir().resolve(Paths.get(artifact.getPath()));
        Path checksumPath = context.getChecksumsDirectory().resolve(distributionName).resolve(artifactPath.getFileName() + ".sha256");

        readHash(context, artifact, artifactPath, checksumPath);
    }

    public static void readHash(JReleaserContext context, Artifact artifact) throws JReleaserException {
        Path artifactPath = context.getBasedir().resolve(Paths.get(artifact.getPath()));
        Path checksumPath = context.getChecksumsDirectory().resolve(artifactPath.getFileName() + ".sha256");

        readHash(context, artifact, artifactPath, checksumPath);
    }

    private static void readHash(JReleaserContext context, Artifact artifact, Path artifactPath, Path checksumPath) throws JReleaserException {
        if (!artifactPath.toFile().exists()) {
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
            context.getLogger().debug("Reading checksum:{}{}{}{}{}{}",
                System.lineSeparator(),
                DEBUG_TAB, context.getBasedir().relativize(artifactPath), System.lineSeparator(),
                DEBUG_TAB, context.getBasedir().relativize(checksumPath));
            artifact.setHash(new String(Files.readAllBytes(checksumPath)));
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error when reading hash from " + context.getBasedir().relativize(checksumPath), e);
        }
    }

    public static String calculateHash(JReleaserContext context, Path input, Path output) throws JReleaserException {
        try {
            context.getLogger().debug("Calculating checksum for {}", context.getBasedir().relativize(input));
            HashCode hashCode = com.google.common.io.Files.asByteSource(input.toFile()).hash(Hashing.sha256());
            output.toFile().getParentFile().mkdirs();
            com.google.common.io.Files.write(hashCode.toString().getBytes(), output.toFile());
            return hashCode.toString();
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error calculating checksum for " + input, e);
        }
    }
}
