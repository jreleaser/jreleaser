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
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Checksums {
    public static void collectAndWriteChecksums(Logger logger, JReleaserModel model, Path checksumsDirectory) throws JReleaserException {
        List<String> checksums = new ArrayList<>();
        for (Distribution distribution : model.getDistributions().values()) {
            for (Artifact artifact : distribution.getArtifacts()) {
                readHash(logger, distribution.getName(), artifact, checksumsDirectory);
                checksums.add(artifact.getHash() + " " + distribution.getName() + "/" +
                    Paths.get(artifact.getPath()).getFileName());
            }
        }

        Path checksumsFilePath = checksumsDirectory.resolve("checksums.txt");
        try {
            Files.createDirectories(checksumsDirectory);
            Files.write(checksumsFilePath, String.join(System.lineSeparator(), checksums).getBytes());
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error writing checksums to " + checksumsFilePath.toAbsolutePath(), e);
        }
    }

    public static void readHash(Logger logger, String distributionName, Artifact artifact, Path checksumDirectory) throws JReleaserException {
        Path artifactPath = Paths.get(artifact.getPath());
        Path checksumPath = checksumDirectory.resolve(distributionName).resolve(artifactPath.getFileName() + ".sha256");

        if (!artifactPath.toFile().exists()) {
            throw new JReleaserException("Artifact does not exist. " + artifactPath.toAbsolutePath());
        }

        if (!checksumPath.toFile().exists()) {
            logger.info("Artifact checksum does not exist. " + checksumPath.toAbsolutePath());
            calculateHash(logger, artifactPath, checksumPath);
        } else if (artifactPath.toFile().lastModified() > checksumPath.toFile().lastModified()) {
            logger.info("Artifact {} is newer than {}", artifactPath.toAbsolutePath(), checksumPath.toAbsolutePath());
            calculateHash(logger, artifactPath, checksumPath);
        }

        try {
            logger.debug("Reading checksum for {} from {}", artifactPath.toAbsolutePath(), checksumPath.toAbsolutePath());
            artifact.setHash(new String(Files.readAllBytes(checksumPath)));
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error when reading hash from " + checksumPath.toAbsolutePath(), e);
        }
    }

    public static String calculateHash(Logger logger, Path input, Path output) throws JReleaserException {
        try {
            logger.info("Calculating checksum for " + input.toAbsolutePath());
            HashCode hashCode = com.google.common.io.Files.asByteSource(input.toFile()).hash(Hashing.sha256());
            output.toFile().getParentFile().mkdirs();
            com.google.common.io.Files.write(hashCode.toString().getBytes(), output.toFile());
            return hashCode.toString();
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error calculating checksum for " + input, e);
        }
    }
}
