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
package org.jreleaser.gradle.plugin.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.jreleaser.util.AbstractJReleaserLogger
import org.kordamp.gradle.util.AnsiConsole
import org.slf4j.helpers.MessageFormatter

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JReleaserLoggerAdapter extends AbstractJReleaserLogger {
    private final PrintWriter out
    private final Level level
    private final AnsiConsole console

    JReleaserLoggerAdapter(Project project, PrintWriter tracer) {
        this(project, tracer, new PrintWriter(System.out, true), Level.INFO)
    }

    JReleaserLoggerAdapter(Project project, PrintWriter tracer, PrintWriter out) {
        this(project, tracer, out, Level.INFO)
    }

    JReleaserLoggerAdapter(Project project, PrintWriter tracer, PrintWriter out, Level level) {
        super(tracer)
        this.out = out
        this.level = level
        this.console = new AnsiConsole(project)
    }

    @Override
    void debug(String message) {
        if (isLevelEnabled(Level.DEBUG)) {
            log(Level.DEBUG, message)
        }
        trace(Level.DEBUG, message)
    }

    @Override
    void info(String message) {
        if (isLevelEnabled(Level.INFO)) {
            log(Level.INFO, message)
        }
        trace(Level.INFO, message)
    }

    @Override
    void warn(String message) {
        if (isLevelEnabled(Level.WARN)) {
            log(Level.WARN, message)
        }
        trace(Level.WARN, message)
    }

    @Override
    void error(String message) {
        if (isLevelEnabled(Level.ERROR)) {
            log(Level.ERROR, message)
        }
        trace(Level.ERROR, message)
    }

    @Override
    void debug(String message, Object... args) {
        String msg = MessageFormatter.arrayFormat(message, args).getMessage()
        if (isLevelEnabled(Level.DEBUG)) {
            log(Level.DEBUG, msg)
        }
        trace(Level.DEBUG, msg)
    }

    @Override
    void info(String message, Object... args) {
        String msg = MessageFormatter.arrayFormat(message, args).getMessage()
        if (isLevelEnabled(Level.INFO)) {
            log(Level.INFO, msg)
        }
        trace(Level.INFO, msg)
    }

    @Override
    void warn(String message, Object... args) {
        String msg = MessageFormatter.arrayFormat(message, args).getMessage()
        if (isLevelEnabled(Level.WARN)) {
            log(Level.WARN, msg)
        }
        trace(Level.WARN, msg)
    }

    @Override
    void error(String message, Object... args) {
        String msg = MessageFormatter.arrayFormat(message, args).getMessage()
        if (isLevelEnabled(Level.ERROR)) {
            log(Level.ERROR, msg)
        }
        trace(Level.ERROR, msg)
    }

    @Override
    void debug(String message, Throwable throwable) {
        if (isLevelEnabled(Level.DEBUG)) {
            log(Level.DEBUG, message, throwable)
        }
        trace(Level.DEBUG, message, throwable)
    }

    @Override
    void info(String message, Throwable throwable) {
        if (isLevelEnabled(Level.INFO)) {
            log(Level.INFO, message, throwable)
        }
        trace(Level.INFO, message, throwable)
    }

    @Override
    void warn(String message, Throwable throwable) {
        if (isLevelEnabled(Level.WARN)) {
            log(Level.WARN, message, throwable)
        }
        trace(Level.WARN, message, throwable)
    }

    @Override
    void error(String message, Throwable throwable) {
        if (isLevelEnabled(Level.ERROR)) {
            log(Level.ERROR, message, throwable)
        }
        trace(Level.ERROR, message, throwable)
    }

    private void log(Level level, String message) {
        log(level, message, null)
    }

    private void log(Level level, String message, Throwable throwable) {
        StringBuilder b = new StringBuilder('[')
        switch (level.color()) {
            case 'cyan':
                b.append(console.cyan(level.name()))
                break
            case 'blue':
                b.append(console.blue(level.name()))
                break
            case 'yellow':
                b.append(console.yellow(level.name()))
                break
            case 'red':
                b.append(console.red(level.name()))
                break
        }

        out.println(b.append('] ')
            .append(level.name().length() == 4 ? ' ' : '')
            .append(formatMessage(message)))
        if (throwable) printThrowable(throwable)
    }

    private void trace(Level level, String message) {
        trace(level, message, null)
    }

    private void trace(Level level, String message, Throwable throwable) {
        StringBuilder b = new StringBuilder('[')
            .append(level.name())
            .append('] ')
            .append(level.name().length() == 4 ? ' ' : '')
            .append(formatMessage(message))
        if (throwable) {
            trace(b.toString(), throwable)
        } else {
            trace(b.toString())
        }
    }

    private void printThrowable(Throwable throwable) {
        if (null != throwable) {
            throwable.printStackTrace(new Colorizer(out))
        }
    }

    private boolean isLevelEnabled(Level requested) {
        return requested.ordinal() >= level.ordinal()
    }

    private class Colorizer extends PrintWriter {
        Colorizer(PrintWriter delegate) {
            super(delegate, true)
        }

        @Override
        void print(String s) {
            super.print(console.red(s))
        }
    }

    enum Level {
        DEBUG('cyan'),
        INFO('blue'),
        WARN('yellow'),
        ERROR('red')

        private final String color

        Level(String color) {
            this.color = color
        }

        String color() {
            this.color
        }
    }
}
