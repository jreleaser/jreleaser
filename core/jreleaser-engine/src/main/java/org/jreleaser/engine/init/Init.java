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
package org.jreleaser.engine.init;

import org.apache.commons.io.IOUtils;
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.templates.TemplateResource;
import org.jreleaser.templates.TemplateUtils;
import org.jreleaser.templates.VersionDecoratingWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.bundle.RB.$;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public final class Init {
    private Init() {
        // noop
    }

    public static void execute(JReleaserLogger logger, String format, boolean overwrite, Path outputDirectory) {
        try {
            if (isBlank(format)) format = "yml";

            if (!getSupportedConfigFormats().contains(format)) {
                throw new IllegalArgumentException($("jreleaser.init.ERROR_invalid_format",
                    String.join("|", getSupportedConfigFormats())));
            }

            Path outputFile = outputDirectory.resolve("jreleaser." + format);

            TemplateResource template = TemplateUtils.resolveTemplate(logger, "init/jreleaser." + format + ".tpl");

            String content = IOUtils.toString(template.getReader());
            LocalDate now = LocalDate.now();
            content = content.replace("@year@", now.getYear() + "");

            logger.info($("jreleaser.init.TEXT_writing_file"), outputFile.toAbsolutePath());

            try (Writer fileWriter = Files.newBufferedWriter(outputFile, overwrite ? CREATE : CREATE_NEW, WRITE, TRUNCATE_EXISTING);
                 BufferedWriter decoratedWriter = new VersionDecoratingWriter(fileWriter)) {
                decoratedWriter.write(content);
            } catch (FileAlreadyExistsException e) {
                logger.error($("jreleaser.ERROR_file_exists"), outputFile.toAbsolutePath());
                return;
            }

            logger.info($("jreleaser.init.TEXT_success"), outputDirectory.toAbsolutePath());
        } catch (IllegalStateException | IOException e) {
            throw new JReleaserException($("ERROR_unexpected_error"), e);
        } finally {
            if (null != logger) logger.close();
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
