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

import org.jreleaser.util.Logger

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class JReleaserLoggerAdapter implements Logger {
    private final org.gradle.api.logging.Logger delegate

    JReleaserLoggerAdapter(org.gradle.api.logging.Logger delegate) {
        this.delegate = delegate
    }

    @Override
    void debug(String message) {
        delegate.debug(message)
    }

    @Override
    void info(String message) {
        delegate.info(message)
    }

    @Override
    void warn(String message) {
        delegate.warn(message)
    }

    @Override
    void error(String message) {
        delegate.error(message)
    }

    @Override
    void debug(String message, Object... args) {
        delegate.debug(message, args)
    }

    @Override
    void info(String message, Object... args) {
        delegate.info(message, args)
    }

    @Override
    void warn(String message, Object... args) {
        delegate.warn(message, args)
    }

    @Override
    void error(String message, Object... args) {
        delegate.error(message, args)
    }

    @Override
    void debug(String message, Throwable throwable) {
        delegate.debug(message, throwable)
    }

    @Override
    void info(String message, Throwable throwable) {
        delegate.info(message, throwable)
    }

    @Override
    void warn(String message, Throwable throwable) {
        delegate.warn(message, throwable)
    }

    @Override
    void error(String message, Throwable throwable) {
        delegate.error(message, throwable)
    }
}
