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
package org.jreleaser.cli;

import org.jreleaser.cli.internal.ColorizedJReleaserLoggerAdapter;
import org.jreleaser.logging.JReleaserLogger;
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
import java.util.Set;
import java.util.concurrent.Callable;

import static org.jreleaser.model.JReleaserOutput.JRELEASER_QUIET;
import static org.jreleaser.util.IoUtils.newPrintWriter;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractLoggingCommand<C extends IO> extends AbstractCommand<C> implements Callable<Integer> {
    protected JReleaserLogger logger;

    @CommandLine.Option(names = {"-g", "--debug"})
    boolean debug;

    @CommandLine.Option(names = {"-i", "--info"})
    boolean info;

    @CommandLine.Option(names = {"-w", "--warn"})
    boolean warn;

    @CommandLine.Option(names = {"-q", "--quiet"})
    boolean quiet;

    @CommandLine.Option(names = {"-b", "--basedir"}, paramLabel = "<directory>")
    Path basedir;

    @CommandLine.Option(names = {"-od", "--output-directory"}, paramLabel = "<directory>")
    Path outputdir;

    protected ColorizedJReleaserLoggerAdapter.Level level = ColorizedJReleaserLoggerAdapter.Level.INFO;

    @Override
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

        Banner.display(parent().getErr());
    }

    @Override
    protected void collectCandidateDeprecatedArgs(Set<AbstractCommand.DeprecatedArg> args) {
        args.add(new DeprecatedArg("-od", "--output-directory", "1.5.0"));
    }

    protected void initLogger() {
        logger = new ColorizedJReleaserLoggerAdapter(createTracer(), parent().getOut(), level);
    }

    protected PrintWriter createTracer() {
        try {
            Files.createDirectories(getOutputDirectory());
            return newPrintWriter(new FileOutputStream(
                    getOutputDirectory().resolve("trace.log").toFile()));
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
        if (null != input && input.length > 0) {
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
