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
package org.jreleaser.ant.tasks.internal;

import org.apache.tools.ant.Project;
import org.jreleaser.util.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserLoggerAdapter extends AbstractLogger {
    private final Project delegate;

    public JReleaserLoggerAdapter(Project delegate) {
        this.delegate = delegate;
    }

    @Override
    public void debug(String message) {
        delegate.log(formatMessage(message), Project.MSG_DEBUG);
    }

    @Override
    public void info(String message) {
        delegate.log(formatMessage(message), Project.MSG_INFO);
    }

    @Override
    public void warn(String message) {
        delegate.log(formatMessage(message), Project.MSG_WARN);
    }

    @Override
    public void error(String message) {
        delegate.log(formatMessage(message), Project.MSG_ERR);
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
    public void debug(String message, Throwable throwable) {
        delegate.log(formatMessage(message), throwable, Project.MSG_DEBUG);
    }

    @Override
    public void info(String message, Throwable throwable) {
        delegate.log(formatMessage(message), throwable, Project.MSG_INFO);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        delegate.log(formatMessage(message), throwable, Project.MSG_WARN);
    }

    @Override
    public void error(String message, Throwable throwable) {
        delegate.log(formatMessage(message), throwable, Project.MSG_ERR);
    }
}
