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

import org.jreleaser.app.internal.Colorizer;
import org.jreleaser.app.internal.ColorizedJReleaserLoggerAdapter;
import org.jreleaser.util.Logger;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command
abstract class AbstractCommand implements Callable<Integer> {
    protected Logger logger;

    @CommandLine.Option(names = {"-d", "--debug"},
        description = "Set log level to debug.")
    boolean debug;

    @CommandLine.Option(names = {"-i", "--info"},
        description = "Set log level to info.")
    boolean info;

    @CommandLine.Option(names = {"-w", "--warn"},
        description = "Set log level to warn.")
    boolean warn;

    @CommandLine.Option(names = {"-q", "--quiet"},
        description = "Log errors only.")
    boolean quiet;

    @CommandLine.Option(names = {"--basedir"},
        description = "Base directory")
    Path basedir;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    protected abstract Main parent();

    public Integer call() {
        Banner.display();

        ColorizedJReleaserLoggerAdapter.Level level = ColorizedJReleaserLoggerAdapter.Level.WARN;
        if (debug) {
            level = ColorizedJReleaserLoggerAdapter.Level.DEBUG;
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        } else if (info) {
            level = ColorizedJReleaserLoggerAdapter.Level.INFO;
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        } else if (warn) {
            level = ColorizedJReleaserLoggerAdapter.Level.WARN;
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        } else if (quiet) {
            level = ColorizedJReleaserLoggerAdapter.Level.ERROR;
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        }

        logger = new ColorizedJReleaserLoggerAdapter(parent().out, level);

        try {
            execute();
        } catch (HaltExecutionException e) {
            return 1;
        } catch (Exception e) {
            e.printStackTrace(new Colorizer(parent().out));
            return 1;
        }

        return 0;
    }

    protected abstract void execute();
}
