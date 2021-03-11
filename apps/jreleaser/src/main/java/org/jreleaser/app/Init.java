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
package org.jreleaser.app;

import org.jreleaser.model.JReleaserException;
import org.jreleaser.templates.TemplateUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "init",
    description = "Creates a jrelaser config file")
public class Init extends AbstractCommand {
    @CommandLine.Option(names = {"--overwrite"},
        description = "Overwrite existing files")
    boolean overwrite;

    @CommandLine.ParentCommand
    Main parent;

    @Override
    protected Main parent() {
        return parent;
    }

    protected void execute() {
        try {
            Path outputDirectory = null != basedir ? basedir : Paths.get(".").normalize();
            Path outputFile = outputDirectory.resolve(".jreleaser.yml");

            Reader template = TemplateUtils.resolveTemplate(logger, Init.class,
                "META-INF/jreleaser/templates/jreleaser.yml.tpl");

            logger.info("Writing file " + outputFile.toAbsolutePath());
            try (Writer writer = Files.newBufferedWriter(outputFile, (overwrite ? CREATE : CREATE_NEW), WRITE, TRUNCATE_EXISTING);
                 Scanner scanner = new Scanner(template)) {
                while (scanner.hasNextLine()) {
                    writer.write(scanner.nextLine() + System.lineSeparator());
                }
            } catch (FileAlreadyExistsException e) {
                logger.error("File {} already exists and overwrite was set to false.", outputFile.toAbsolutePath());
                return;
            }

            if (!quiet) {
                parent.out.println("JReleaser initialized at " + outputDirectory.toAbsolutePath());
            }
        } catch (IllegalStateException | IOException e) {
            throw new JReleaserException("Unexpected error", e);
        }
    }
}
