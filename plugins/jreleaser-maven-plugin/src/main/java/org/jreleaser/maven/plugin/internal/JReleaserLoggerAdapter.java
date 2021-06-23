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
package org.jreleaser.maven.plugin.internal;

import org.apache.maven.plugin.logging.Log;
import org.jreleaser.util.AbstractJReleaserLogger;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserLoggerAdapter extends AbstractJReleaserLogger {
    private final Log delegate;

    public JReleaserLoggerAdapter(PrintWriter tracer, Log delegate) {
        super(tracer);
        this.delegate = delegate;
    }

    @Override
    public void debug(String message) {
        String msg = formatMessage(message);
        delegate.debug(msg);
        trace(Level.DEBUG + msg);
    }

    @Override
    public void info(String message) {
        String msg = formatMessage(message);
        delegate.info(msg);
        trace(Level.INFO + msg);
    }

    @Override
    public void warn(String message) {
        String msg = formatMessage(message);
        delegate.warn(msg);
        trace(Level.WARN + msg);
    }

    @Override
    public void error(String message) {
        String msg = formatMessage(message);
        delegate.error(msg);
        trace(Level.ERROR + msg);
    }

    @Override
    public void debug(String message, Object... args) {
        String msg = formatMessage(MessageFormatter.arrayFormat(message, args).getMessage());
        delegate.debug(msg);
        trace(Level.DEBUG + msg);
    }

    @Override
    public void info(String message, Object... args) {
        String msg = formatMessage(MessageFormatter.arrayFormat(message, args).getMessage());
        delegate.info(msg);
        trace(Level.INFO + msg);
    }

    @Override
    public void warn(String message, Object... args) {
        String msg = formatMessage(MessageFormatter.arrayFormat(message, args).getMessage());
        delegate.warn(msg);
        trace(Level.WARN + msg);
    }

    @Override
    public void error(String message, Object... args) {
        String msg = formatMessage(MessageFormatter.arrayFormat(message, args).getMessage());
        delegate.error(msg);
        trace(Level.ERROR + msg);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        String msg = formatMessage(message);
        delegate.debug(msg, throwable);
        trace(Level.DEBUG + msg, throwable);
    }

    @Override
    public void info(String message, Throwable throwable) {
        String msg = formatMessage(message);
        delegate.info(msg, throwable);
        trace(Level.INFO + msg, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        String msg = formatMessage(message);
        delegate.warn(msg, throwable);
        trace(Level.WARN + msg, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        String msg = formatMessage(message);
        delegate.error(msg, throwable);
        trace(Level.ERROR + msg, throwable);
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
