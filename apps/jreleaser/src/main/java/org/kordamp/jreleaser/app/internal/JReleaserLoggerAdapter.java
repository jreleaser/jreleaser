/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.app.internal;

import org.kordamp.jreleaser.util.Logger;
import org.slf4j.helpers.MessageFormatter;
import picocli.CommandLine;

import java.io.PrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserLoggerAdapter implements Logger {
    private final PrintWriter out;
    private final Level level;

    public JReleaserLoggerAdapter(PrintWriter out) {
        this(out, Level.WARN);
    }

    public JReleaserLoggerAdapter(PrintWriter out, Level level) {
        this.out = out;
        this.level = level;
    }

    @Override
    public void debug(String message) {
        if (isLevelEnabled(Level.DEBUG)) {
            out.println(Level.DEBUG + message);
        }
    }

    @Override
    public void info(String message) {
        if (isLevelEnabled(Level.INFO)) {
            out.println(Level.INFO + message);
        }
    }

    @Override
    public void warn(String message) {
        if (isLevelEnabled(Level.WARN)) {
            out.println(Level.WARN + message);
        }
    }

    @Override
    public void error(String message) {
        if (isLevelEnabled(Level.ERROR)) {
            out.println(Level.ERROR + message);
        }
    }

    @Override
    public void debug(String message, Object... args) {
        if (isLevelEnabled(Level.DEBUG)) {
            out.println(Level.DEBUG + MessageFormatter.arrayFormat(message, args).getMessage());
        }
    }

    @Override
    public void info(String message, Object... args) {
        if (isLevelEnabled(Level.INFO)) {
            out.println(Level.INFO + MessageFormatter.arrayFormat(message, args).getMessage());
        }
    }

    @Override
    public void warn(String message, Object... args) {
        if (isLevelEnabled(Level.WARN)) {
            out.println(Level.WARN + MessageFormatter.arrayFormat(message, args).getMessage());
        }
    }

    @Override
    public void error(String message, Object... args) {
        if (isLevelEnabled(Level.ERROR)) {
            out.println(Level.ERROR + MessageFormatter.arrayFormat(message, args).getMessage());
        }
    }

    @Override
    public void debug(String message, Throwable throwable) {
        if (isLevelEnabled(Level.DEBUG)) {
            out.println(Level.DEBUG + message);
            printThrowable(throwable);
        }
    }

    @Override
    public void info(String message, Throwable throwable) {
        if (isLevelEnabled(Level.INFO)) {
            out.println(Level.INFO + message);
            printThrowable(throwable);
        }
    }

    @Override
    public void warn(String message, Throwable throwable) {
        if (isLevelEnabled(Level.WARN)) {
            out.println(Level.WARN + message);
            printThrowable(throwable);
        }
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (isLevelEnabled(Level.ERROR)) {
            out.println(Level.ERROR + message);
            printThrowable(throwable);
        }
    }

    private void printThrowable(Throwable throwable) {
        if (null != throwable) {
            throwable.printStackTrace(new Colorizer(out));
        }
    }

    private boolean isLevelEnabled(Level requested) {
        return requested.ordinal() >= level.ordinal();
    }

    public enum Level {
        DEBUG("cyan"),
        INFO("blue"),
        WARN("yellow"),
        ERROR("red");

        private final String color;

        Level(String color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return "[" + colorize(name()) + "] ";
        }

        private String colorize(String input) {
            return CommandLine.Help.Ansi.AUTO.string("@|" + color + " " + input + "|@");
        }
    }
}
