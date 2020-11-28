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
package org.kordamp.jreleaser.maven.plugin.internal;

import org.apache.maven.plugin.logging.Log;
import org.kordamp.jreleaser.util.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserLoggerAdapter implements Logger {
    private final Log delegate;

    public JReleaserLoggerAdapter(Log delegate) {
        this.delegate = delegate;
    }

    @Override
    public void debug(String message) {
        delegate.debug(message);
    }

    @Override
    public void info(String message) {
        delegate.info(message);
    }

    @Override
    public void warn(String message) {
        delegate.warn(message);
    }

    @Override
    public void error(String message) {
        delegate.error(message);
    }

    @Override
    public void debug(String message, Object... args) {
        delegate.debug(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Override
    public void info(String message, Object... args) {
        delegate.info(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Override
    public void warn(String message, Object... args) {
        delegate.warn(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Override
    public void error(String message, Object... args) {
        delegate.error(MessageFormatter.arrayFormat(message, args).getMessage());
    }

    @Override
    public void debug(String message, Throwable throwable) {
        delegate.debug(message, throwable);
    }

    @Override
    public void info(String message, Throwable throwable) {
        delegate.info(message, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        delegate.warn(message, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        delegate.error(message, throwable);
    }
}
