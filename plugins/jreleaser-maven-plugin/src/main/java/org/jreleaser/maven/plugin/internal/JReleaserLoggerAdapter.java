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
package org.jreleaser.maven.plugin.internal;

import org.apache.maven.plugin.logging.Log;
import org.jreleaser.util.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserLoggerAdapter extends AbstractLogger {
    private final Log delegate;

    public JReleaserLoggerAdapter(Log delegate) {
        this.delegate = delegate;
    }

    @Override
    public void debug(String message) {
        delegate.debug(formatMessage(message));
    }

    @Override
    public void info(String message) {
        delegate.info(formatMessage(message));
    }

    @Override
    public void warn(String message) {
        delegate.warn(formatMessage(message));
    }

    @Override
    public void error(String message) {
        delegate.error(formatMessage(message));
    }

    @Override
    public void debug(String message, Object... args) {
        delegate.debug(formatMessage(MessageFormatter.arrayFormat(message, args).getMessage()));
    }

    @Override
    public void info(String message, Object... args) {
        delegate.info(formatMessage(MessageFormatter.arrayFormat(message, args).getMessage()));
    }

    @Override
    public void warn(String message, Object... args) {
        delegate.warn(formatMessage(MessageFormatter.arrayFormat(message, args).getMessage()));
    }

    @Override
    public void error(String message, Object... args) {
        delegate.error(formatMessage(MessageFormatter.arrayFormat(message, args).getMessage()));
    }

    @Override
    public void debug(String message, Throwable throwable) {
        delegate.debug(formatMessage(message), throwable);
    }

    @Override
    public void info(String message, Throwable throwable) {
        delegate.info(formatMessage(message), throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        delegate.warn(formatMessage(message), throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        delegate.error(formatMessage(message), throwable);
    }
}
