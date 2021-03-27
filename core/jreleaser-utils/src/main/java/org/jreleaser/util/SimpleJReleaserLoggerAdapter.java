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
package org.jreleaser.util;

import org.slf4j.helpers.MessageFormatter;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SimpleJReleaserLoggerAdapter extends AbstractJReleaserLogger {
    private final PrintWriter out;
    private final Level level;

    public SimpleJReleaserLoggerAdapter() {
        this(System.out, Level.WARN);
    }

    public SimpleJReleaserLoggerAdapter(Level level) {
        this(System.out, level);
    }

    public SimpleJReleaserLoggerAdapter(OutputStream out) {
        this(new PrintWriter(out, true), Level.WARN);
    }

    public SimpleJReleaserLoggerAdapter(OutputStream out, Level level) {
        this(new PrintWriter(out, true), level);
    }

    public SimpleJReleaserLoggerAdapter(PrintWriter out) {
        this(out, Level.WARN);
    }

    public SimpleJReleaserLoggerAdapter(PrintWriter out, Level level) {
        this.out = out;
        this.level = level;
    }

    @Override
    public void debug(String message) {
        if (isLevelEnabled(Level.DEBUG)) {
            out.println(Level.DEBUG + formatMessage(message));
        }
    }

    @Override
    public void info(String message) {
        if (isLevelEnabled(Level.INFO)) {
            out.println(Level.INFO + formatMessage(message));
        }
    }

    @Override
    public void warn(String message) {
        if (isLevelEnabled(Level.WARN)) {
            out.println(Level.WARN + formatMessage(message));
        }
    }

    @Override
    public void error(String message) {
        if (isLevelEnabled(Level.ERROR)) {
            out.println(Level.ERROR + formatMessage(message));
        }
    }

    @Override
    public void debug(String message, Object... args) {
        if (isLevelEnabled(Level.DEBUG)) {
            out.println(Level.DEBUG + formatMessage(MessageFormatter.arrayFormat(message, args).getMessage()));
        }
    }

    @Override
    public void info(String message, Object... args) {
        if (isLevelEnabled(Level.INFO)) {
            out.println(Level.INFO + formatMessage(MessageFormatter.arrayFormat(message, args).getMessage()));
        }
    }

    @Override
    public void warn(String message, Object... args) {
        if (isLevelEnabled(Level.WARN)) {
            out.println(Level.WARN + formatMessage(MessageFormatter.arrayFormat(message, args).getMessage()));
        }
    }

    @Override
    public void error(String message, Object... args) {
        if (isLevelEnabled(Level.ERROR)) {
            out.println(Level.ERROR + formatMessage(MessageFormatter.arrayFormat(message, args).getMessage()));
        }
    }

    @Override
    public void debug(String message, Throwable throwable) {
        if (isLevelEnabled(Level.DEBUG)) {
            out.println(Level.DEBUG + formatMessage(message));
            printThrowable(throwable);
        }
    }

    @Override
    public void info(String message, Throwable throwable) {
        if (isLevelEnabled(Level.INFO)) {
            out.println(Level.INFO + formatMessage(message));
            printThrowable(throwable);
        }
    }

    @Override
    public void warn(String message, Throwable throwable) {
        if (isLevelEnabled(Level.WARN)) {
            out.println(Level.WARN + formatMessage(message));
            printThrowable(throwable);
        }
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (isLevelEnabled(Level.ERROR)) {
            out.println(Level.ERROR + formatMessage(message));
            printThrowable(throwable);
        }
    }

    private void printThrowable(Throwable throwable) {
        if (null != throwable) {
            throwable.printStackTrace(out);
            out.flush();
        }
    }

    private boolean isLevelEnabled(Level requested) {
        return requested.ordinal() >= level.ordinal();
    }

    public enum Level {
        DEBUG,
        INFO,
        WARN,
        ERROR;

        @Override
        public String toString() {
            return "[" + name() + "] " + (name().length() == 4 ? " " : "");
        }
    }
}
