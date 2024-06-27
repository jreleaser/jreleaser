/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
package org.jreleaser.model.internal.validation.common;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.HostAware;
import org.jreleaser.model.internal.common.PortAware;
import org.jreleaser.model.internal.common.TimeoutAware;
import org.jreleaser.model.internal.servers.Server;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.AuthenticatableValidator.validateAuthenticatable;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.util.CollectionUtils.setOf;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.11.0
 */
public final class ServerValidator {
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String CONNECT_TIMEOUT = "connect.timeout";
    private static final String READ_TIMEOUT = "read.timeout";
    private static final String DOT = ".";

    private ServerValidator() {
        // noop
    }

    public static void validateServer(JReleaserContext context, Server<?> server, String prefix, String type, String name, Errors errors) {
        validateAuthenticatable(context, server, null, prefix, type, name, errors, true);
        validateHost(context, server, null, prefix, type, name, errors, true);
        validatePort(context, server, null, prefix, type, name, errors, true);
        validateTimeout(context, server, null, prefix, type, name, errors, true);
    }

    public static void validateHost(JReleaserContext context, HostAware subject, HostAware other, String prefix, String type, String name, Errors errors, boolean continueOnError) {
        subject.setHost(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + HOST,
                    prefix + DOT + type + DOT + HOST,
                    type + DOT + name + DOT + HOST,
                    type + DOT + HOST),
                prefix + DOT + type + DOT + name + DOT + HOST,
                subject.getHost(),
                null != other ? other.getHost() : null,
                errors,
                continueOnError));
    }

    public static void validatePort(JReleaserContext context, PortAware subject, PortAware other, String prefix, String type, String name, Errors errors, boolean continueOnError) {
        subject.setPort(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + PORT,
                    prefix + DOT + type + DOT + PORT,
                    type + DOT + name + DOT + PORT,
                    type + DOT + PORT),
                prefix + DOT + type + DOT + name + DOT + PORT,
                subject.getPort(),
                null != other ? other.getPort() : null,
                errors,
                continueOnError));
    }

    public static void validateTimeout(JReleaserContext context, TimeoutAware subject, TimeoutAware other, String prefix, String type, String name, Errors errors, boolean continueOnError) {
        subject.setConnectTimeout(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + CONNECT_TIMEOUT,
                    prefix + DOT + type + DOT + CONNECT_TIMEOUT,
                    type + DOT + name + DOT + CONNECT_TIMEOUT,
                    type + DOT + CONNECT_TIMEOUT),
                prefix + DOT + type + DOT + name + DOT + CONNECT_TIMEOUT,
                subject.getConnectTimeout(),
                null != other ? other.getConnectTimeout() : null,
                errors,
                continueOnError));

        subject.setReadTimeout(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + READ_TIMEOUT,
                    prefix + DOT + type + DOT + READ_TIMEOUT,
                    type + DOT + name + DOT + READ_TIMEOUT,
                    type + DOT + READ_TIMEOUT),
                prefix + DOT + type + DOT + name + DOT + READ_TIMEOUT,
                subject.getReadTimeout(),
                null != other ? other.getReadTimeout() : null,
                errors,
                continueOnError));

        if (null == subject.getConnectTimeout() || subject.getConnectTimeout() <= 0 || subject.getConnectTimeout() > 300) {
            subject.setConnectTimeout(20);
        }
        if (null == subject.getReadTimeout() || subject.getReadTimeout() <= 0 || subject.getReadTimeout() > 300) {
            subject.setReadTimeout(60);
        }
    }
}
