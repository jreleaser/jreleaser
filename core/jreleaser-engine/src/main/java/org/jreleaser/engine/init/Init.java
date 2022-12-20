/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.engine.init;

import org.apache.commons.io.IOUtils;
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.templates.TemplateResource;
import org.jreleaser.templates.TemplateUtils;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public class Init {
    public static void execute(JReleaserLogger logger, String format, boolean overwrite, Path outputDirectory) {
        try {
            if (!getSupportedConfigFormats().contains(format)) {
                throw new JReleaserException("Unsupported file format. Must be one of [" +
                    String.join("|", getSupportedConfigFormats()) + "]");
            }

            Path outputFile = outputDirectory.resolve("jreleaser." + format);

            TemplateResource template = TemplateUtils.resolveTemplate(logger, "init/jreleaser." + format + ".tpl");

            logger.info("Writing file " + outputFile.toAbsolutePath());
            try (Writer writer = Files.newBufferedWriter(outputFile, overwrite ? CREATE : CREATE_NEW, WRITE, TRUNCATE_EXISTING)) {
                IOUtils.copy(template.getReader(), writer);
            } catch (FileAlreadyExistsException e) {
                logger.error("File {} already exists and overwrite was set to false.", outputFile.toAbsolutePath());
                return;
            }

            logger.info("JReleaser initialized at " + outputDirectory.toAbsolutePath());
        } catch (IllegalStateException | IOException e) {
            logger.trace(e);
            throw new JReleaserException("Unexpected error", e);
        } finally {
            if (logger != null) logger.close();
        }
    }

    private static Set<String> getSupportedConfigFormats() {
        Set<String> extensions = new LinkedHashSet<>();

        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class,
            JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            extensions.add(parser.getPreferredFileExtension());
        }

        return extensions;
    }
}
