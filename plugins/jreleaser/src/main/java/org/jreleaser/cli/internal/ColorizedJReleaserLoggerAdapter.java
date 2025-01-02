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
package org.jreleaser.cli.internal;

import org.jreleaser.logging.AbstractJReleaserLogger;
import org.slf4j.helpers.MessageFormatter;
import picocli.CommandLine;

import java.io.PrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ColorizedJReleaserLoggerAdapter extends AbstractJReleaserLogger {
    private final PrintWriter out;
    private final Level level;

    public ColorizedJReleaserLoggerAdapter(PrintWriter tracer, PrintWriter out) {
        this(tracer, out, Level.WARN);
    }

    public ColorizedJReleaserLoggerAdapter(PrintWriter tracer, PrintWriter out, Level level) {
        super(tracer);
        this.out = out;
        this.level = level;
    }

    @Override
    public void plain(String message) {
        String msg = formatMessage(message);
        out.println(msg);
        trace(msg);
    }

    @Override
    public void debug(String message) {
        String msg = formatMessage(message);
        if (isLevelEnabled(Level.DEBUG)) {
            out.println(Level.DEBUG + msg);
        }
        trace(Level.DEBUG.asString() + msg);
    }

    @Override
    public void info(String message) {
        String msg = formatMessage(message);
        if (isLevelEnabled(Level.INFO)) {
            out.println(Level.INFO + msg);
        }
        trace(Level.INFO.asString() + msg);
    }

    @Override
    public void warn(String message) {
        String msg = formatMessage(message);
        if (isLevelEnabled(Level.WARN)) {
            out.println(Level.WARN + msg);
        }
        trace(Level.WARN.asString() + msg);
    }

    @Override
    public void error(String message) {
        String msg = formatMessage(message);
        if (isLevelEnabled(Level.ERROR)) {
            out.println(Level.ERROR + msg);
        }
        trace(Level.ERROR.asString() + msg);
    }

    @Override
    public void plain(String message, Object... args) {
        plain(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Override
    public void debug(String message, Object... args) {
        String msg = formatMessage(MessageFormatter.arrayFormat(message, args).getMessage());
        if (isLevelEnabled(Level.DEBUG)) {
            out.println(Level.DEBUG + msg);
        }
        trace(Level.DEBUG.asString() + msg);
    }

    @Override
    public void info(String message, Object... args) {
        String msg = formatMessage(MessageFormatter.arrayFormat(message, args).getMessage());
        if (isLevelEnabled(Level.INFO)) {
            out.println(Level.INFO + msg);
        }
        trace(Level.INFO.asString() + msg);
    }

    @Override
    public void warn(String message, Object... args) {
        String msg = formatMessage(MessageFormatter.arrayFormat(message, args).getMessage());
        if (isLevelEnabled(Level.WARN)) {
            out.println(Level.WARN + msg);
        }
        trace(Level.WARN.asString() + msg);
    }

    @Override
    public void error(String message, Object... args) {
        String msg = formatMessage(MessageFormatter.arrayFormat(message, args).getMessage());
        if (isLevelEnabled(Level.ERROR)) {
            out.println(Level.ERROR + msg);
        }
        trace(Level.ERROR.asString() + msg);
    }

    @Override
    public void plain(String message, Throwable throwable) {
        String msg = formatMessage(message);
        out.println(msg);
        if (null != throwable) {
            throwable.printStackTrace(out);
        }
        trace(msg, throwable);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        String msg = formatMessage(message);
        if (isLevelEnabled(Level.DEBUG)) {
            out.println(Level.DEBUG + msg);
            printThrowable(throwable);
        }
        trace(Level.DEBUG.asString() + msg, throwable);
    }

    @Override
    public void info(String message, Throwable throwable) {
        String msg = formatMessage(message);
        if (isLevelEnabled(Level.INFO)) {
            out.println(Level.INFO + msg);
            printThrowable(throwable);
        }
        trace(Level.INFO.asString() + msg, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        String msg = formatMessage(message);
        if (isLevelEnabled(Level.WARN)) {
            out.println(Level.WARN + msg);
            printThrowable(throwable);
        }
        trace(Level.WARN.asString() + msg, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        String msg = formatMessage(message);
        if (isLevelEnabled(Level.ERROR)) {
            out.println(Level.ERROR + msg);
            printThrowable(throwable);
        }
        trace(Level.ERROR.asString() + msg, throwable);
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
            return "[" + colorize(name()) + "] " + (name().length() == 4 ? " " : "");
        }

        public String asString() {
            return "[" + name() + "] " + (name().length() == 4 ? " " : "");
        }

        private String colorize(String input) {
            return CommandLine.Help.Ansi.AUTO.string("@|" + color + " " + input + "|@");
        }
    }
}
