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
package org.kordamp.jreleaser.app;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.kordamp.jreleaser.model.Artifact;
import org.kordamp.jreleaser.model.Distribution;
import org.kordamp.jreleaser.model.JReleaserModel;
import org.kordamp.jreleaser.tools.DistributionProcessor;
import org.kordamp.jreleaser.tools.ToolProcessingException;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "prepare",
    description = "Prepares all distributions")
public class Prepare extends AbstractProcessorCommand {
    @Override
    protected void consumeProcessor(DistributionProcessor processor) throws ToolProcessingException {
        if (processor.prepareDistribution()) {
            parent.out.println("Prepared " + processor.getDistributionName() +
                " distribution with tool " + processor.getToolName());
        }
    }

    @Override
    protected Path computeChecksums(JReleaserModel jreleaserModel, Path outputDirectory) {
        Path checksumDirectory = super.computeChecksums(jreleaserModel, outputDirectory);

        try {
            java.nio.file.Files.createDirectories(checksumDirectory);
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error creating checksum directory.", e);
        }

        jreleaserModel.getDistributions().values()
            .stream().flatMap((Function<Distribution, Stream<Artifact>>) distribution -> distribution.getArtifacts()
            .stream()).forEach(artifact -> {
            Path inputFile = Paths.get(artifact.getPath()).toAbsolutePath();
            String fileName = inputFile.getFileName().toString();
            Path checksumFilePath = checksumDirectory.resolve(fileName + ".sha256");
            try {
                HashCode hashCode = Files.asByteSource(inputFile.toFile()).hash(Hashing.sha256());
                Files.write(hashCode.toString().getBytes(), checksumFilePath.toFile());
            } catch (IOException e) {
                throw new JReleaserException("Unexpected error creating checksum for " + inputFile, e);
            }
        });

        return checksumDirectory;
    }
}
