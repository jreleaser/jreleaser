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
package org.jreleaser.cli;

import org.jreleaser.cli.internal.ColorizedJReleaserLoggerAdapter;
import org.jreleaser.util.JReleaserLogger;
import org.jreleaser.util.StringUtils;
import picocli.CommandLine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import static org.jreleaser.util.JReleaserOutput.JRELEASER_QUIET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractLoggingCommand extends AbstractCommand implements Callable<Integer> {
    protected JReleaserLogger logger;

    @CommandLine.Option(names = {"-g", "--debug"})
    boolean debug;

    @CommandLine.Option(names = {"-i", "--info"})
    boolean info;

    @CommandLine.Option(names = {"-w", "--warn"})
    boolean warn;

    @CommandLine.Option(names = {"-q", "--quiet"})
    boolean quiet;

    @CommandLine.Option(names = {"-b", "--basedir"})
    Path basedir;

    @CommandLine.Option(names = {"-od", "--output-directory"})
    Path outputdir;

    protected ColorizedJReleaserLoggerAdapter.Level level = ColorizedJReleaserLoggerAdapter.Level.INFO;

    protected void setup() {
        if (debug) {
            level = ColorizedJReleaserLoggerAdapter.Level.DEBUG;
            System.setProperty("org.slf4j.simpleLogger.org.jreleaser", "debug");
        } else if (info) {
            level = ColorizedJReleaserLoggerAdapter.Level.INFO;
            System.setProperty("org.slf4j.simpleLogger.org.jreleaser", "info");
        } else if (warn) {
            level = ColorizedJReleaserLoggerAdapter.Level.WARN;
            System.setProperty("org.slf4j.simpleLogger.org.jreleaser", "warn");
        } else if (quiet) {
            level = ColorizedJReleaserLoggerAdapter.Level.ERROR;
            System.setProperty("org.slf4j.simpleLogger.org.jreleaser", "error");
            System.setProperty(JRELEASER_QUIET, "true");
        }

        Banner.display(parent().out);
    }

    protected void initLogger() {
        logger = new ColorizedJReleaserLoggerAdapter(createTracer(), parent().out, level);
    }

    protected PrintWriter createTracer() {
        try {
            Files.createDirectories(getOutputDirectory());
            return new PrintWriter(new FileOutputStream(
                getOutputDirectory().resolve("trace.log").toFile()),
                true);
        } catch (IOException e) {
            throw new IllegalStateException($("ERROR_trace_file_init"), e);
        }
    }

    protected abstract Path getOutputDirectory();

    protected List<String> collectEntries(String[] input) {
        return collectEntries(input, false);
    }

    protected List<String> collectEntries(String[] input, boolean lowerCase) {
        List<String> list = new ArrayList<>();
        if (input != null && input.length > 0) {
            for (String s : input) {
                if (isNotBlank(s)) {
                    if (!s.contains("-") && lowerCase) {
                        s = StringUtils.getHyphenatedName(s);
                    }
                    list.add(lowerCase ? s.toLowerCase(Locale.ENGLISH) : s);
                }
            }
        }
        return list;
    }
}
