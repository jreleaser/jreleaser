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
package org.jreleaser.ant.tasks.internal;

import org.apache.tools.ant.Project;
import org.jreleaser.logging.AbstractJReleaserLogger;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserLoggerAdapter extends AbstractJReleaserLogger {
    private static final String DEBUG = "[DEBUG] ";
    private static final String INFO = "[INFO]  ";
    private static final String WARN = "[WARN]  ";
    private static final String ERROR = "[ERROR] ";

    private final Project delegate;

    public JReleaserLoggerAdapter(PrintWriter tracer, Project delegate) {
        super(tracer);
        this.delegate = delegate;
    }

    @Override
    public void plain(String message) {
        String msg = formatMessage(message);
        delegate.log(msg, Project.MSG_INFO);
        trace(msg);
    }

    @Override
    public void debug(String message) {
        String msg = DEBUG + formatMessage(message);
        delegate.log(msg, Project.MSG_DEBUG);
        trace(msg);
    }

    @Override
    public void info(String message) {
        String msg = INFO + formatMessage(message);
        delegate.log(msg, Project.MSG_INFO);
        trace(msg);
    }

    @Override
    public void warn(String message) {
        String msg = WARN + formatMessage(message);
        delegate.log(msg, Project.MSG_WARN);
        trace(msg);
    }

    @Override
    public void error(String message) {
        String msg = ERROR + formatMessage(message);
        delegate.log(msg, Project.MSG_ERR);
        trace(msg);
    }

    @Override
    public void plain(String message, Object... args) {
        plain(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Override
    public void debug(String message, Object... args) {
        debug(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Override
    public void info(String message, Object... args) {
        info(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Override
    public void warn(String message, Object... args) {
        warn(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Override
    public void error(String message, Object... args) {
        error(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Override
    public void plain(String message, Throwable throwable) {
        String msg = formatMessage(message);
        delegate.log(msg, throwable, Project.MSG_INFO);
        trace(msg, throwable);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        String msg = DEBUG + formatMessage(message);
        delegate.log(msg, throwable, Project.MSG_DEBUG);
        trace(msg, throwable);
    }

    @Override
    public void info(String message, Throwable throwable) {
        String msg = INFO + formatMessage(message);
        delegate.log(msg, throwable, Project.MSG_INFO);
        trace(msg, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        String msg = WARN + formatMessage(message);
        delegate.log(msg, throwable, Project.MSG_WARN);
        trace(msg, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        String msg = ERROR + formatMessage(message);
        delegate.log(msg, throwable, Project.MSG_ERR);
        trace(msg, throwable);
    }
}
