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
package org.jreleaser.cli;

import org.jreleaser.cli.internal.ColorizedJReleaserLoggerAdapter;
import org.jreleaser.cli.internal.Colorizer;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.util.JReleaserLogger;
import picocli.CommandLine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractCommand extends BaseCommand implements Callable<Integer> {
    protected JReleaserLogger logger;

    @CommandLine.Option(names = {"-d", "--debug"})
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

    protected abstract Main parent();

    public Integer call() {
        Banner.display(parent().out);

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");

        try {
            execute();
        } catch (HaltExecutionException e) {
            return 1;
        } catch (JReleaserException e) {
            Colorizer colorizer = new Colorizer(parent().out);
            colorizer.println(e.getMessage());
            if (e.getCause() != null) {
                colorizer.println(e.getCause().getMessage());
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace(new Colorizer(parent().out));
            return 1;
        }

        return 0;
    }

    protected void initLogger() {
        ColorizedJReleaserLoggerAdapter.Level level = ColorizedJReleaserLoggerAdapter.Level.INFO;
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
        }

        logger = new ColorizedJReleaserLoggerAdapter(createTracer(), parent().out, level);
    }

    protected PrintWriter createTracer() {
        try {
            Files.createDirectories(getOutputDirectory());
            return new PrintWriter(new FileOutputStream(
                getOutputDirectory().resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new IllegalStateException($("ERROR_trace_file_init"), e);
        }
    }

    protected abstract Path getOutputDirectory();

    protected abstract void execute();

    protected List<String> collectEntries(String[] input) {
        return collectEntries(input, false);
    }

    protected List<String> collectEntries(String[] input, boolean lowerCase) {
        List<String> list = new ArrayList<>();
        if (input != null && input.length > 0) {
            for (String s : input) {
                if (isNotBlank(s)) {
                    s = s.trim();
                    list.add(lowerCase ? s.toLowerCase() : s);
                }
            }
        }
        return list;
    }
}
