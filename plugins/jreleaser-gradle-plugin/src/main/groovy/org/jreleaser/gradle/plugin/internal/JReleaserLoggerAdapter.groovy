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
package org.jreleaser.gradle.plugin.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.jreleaser.util.Logger
import org.kordamp.gradle.util.AnsiConsole
import org.slf4j.helpers.MessageFormatter

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JReleaserLoggerAdapter implements Logger {
    private final PrintWriter out
    private final Level level
    private final AnsiConsole console

    JReleaserLoggerAdapter(Project project) {
        this(project, new PrintWriter(System.out, true), Level.INFO)
    }

    JReleaserLoggerAdapter(Project project, PrintWriter out) {
        this(project, out, Level.INFO)
    }

    JReleaserLoggerAdapter(Project project, PrintWriter out, Level level) {
        this.out = out
        this.level = level
        this.console = new AnsiConsole(project)
    }

    @Override
    void debug(String message) {
        if (isLevelEnabled(Level.DEBUG)) {
            log(Level.DEBUG, message)
        }
    }

    @Override
    void info(String message) {
        if (isLevelEnabled(Level.INFO)) {
            log(Level.INFO, message)
        }
    }

    @Override
    void warn(String message) {
        if (isLevelEnabled(Level.WARN)) {
            log(Level.WARN, message)
        }
    }

    @Override
    void error(String message) {
        if (isLevelEnabled(Level.ERROR)) {
            log(Level.ERROR, message)
        }
    }

    @Override
    void debug(String message, Object... args) {
        if (isLevelEnabled(Level.DEBUG)) {
            log(Level.DEBUG, MessageFormatter.arrayFormat(message, args).getMessage())
        }
    }

    @Override
    void info(String message, Object... args) {
        if (isLevelEnabled(Level.INFO)) {
            log(Level.INFO, MessageFormatter.arrayFormat(message, args).getMessage())
        }
    }

    @Override
    void warn(String message, Object... args) {
        if (isLevelEnabled(Level.WARN)) {
            log(Level.WARN, MessageFormatter.arrayFormat(message, args).getMessage())
        }
    }

    @Override
    void error(String message, Object... args) {
        if (isLevelEnabled(Level.ERROR)) {
            log(Level.ERROR, MessageFormatter.arrayFormat(message, args).getMessage())
        }
    }

    @Override
    void debug(String message, Throwable throwable) {
        if (isLevelEnabled(Level.DEBUG)) {
            log(Level.DEBUG, message)
            printThrowable(throwable)
        }
    }

    @Override
    void info(String message, Throwable throwable) {
        if (isLevelEnabled(Level.INFO)) {
            log(Level.INFO, message)
            printThrowable(throwable)
        }
    }

    @Override
    void warn(String message, Throwable throwable) {
        if (isLevelEnabled(Level.WARN)) {
            log(Level.WARN, message)
            printThrowable(throwable)
        }
    }

    @Override
    void error(String message, Throwable throwable) {
        if (isLevelEnabled(Level.ERROR)) {
            log(Level.ERROR, message)
            printThrowable(throwable)
        }
    }

    private void log(Level level, String message) {
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
            .append(message))
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
