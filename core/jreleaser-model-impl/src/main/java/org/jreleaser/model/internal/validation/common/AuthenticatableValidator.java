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
import org.jreleaser.model.internal.common.Authenticatable;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.util.Errors;

import java.util.Locale;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class AuthenticatableValidator {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String TOKEN = "token";
    private static final String DOT = ".";
    private static final String SERVERS = "servers";

    private AuthenticatableValidator() {
        // noop
    }

    public static void validateAuthenticatable(JReleaserContext context, Authenticatable subject, String prefix, String type, String name, Errors errors) {
        validateAuthenticatable(context, subject, null, prefix, type, name, errors, context.isDryrun());
    }

    public static void validateAuthenticatable(JReleaserContext context, Authenticatable subject, String prefix, String type, String name, Errors errors, boolean continueOnError) {
        validateAuthenticatable(context, subject, null, prefix, type, name, errors, continueOnError);
    }

    public static void validateAuthenticatable(JReleaserContext context, Authenticatable subject, Authenticatable other, String prefix, String type, String name, Errors errors) {
        validateAuthenticatable(context, subject, other, prefix, type, name, errors, context.isDryrun());
    }

    public static void validateAuthenticatable(JReleaserContext context, Authenticatable subject, Authenticatable other, String prefix, String type, String name, Errors errors, boolean continueOnError) {
        validateUsername(context, subject, other, prefix, type, name, errors, continueOnError);
        validatePassword(context, subject, other, prefix, type, name, errors, continueOnError);
    }

    public static void validateUsername(JReleaserContext context, Authenticatable subject, Authenticatable other, String prefix, String type, String name, Errors errors, boolean continueOnError) {
        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();
        String defaultUsername = null;
        String classType = subject.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        if (classType.contains(service.getServiceName().toLowerCase(Locale.ROOT))) {
            defaultUsername = service.getUsername();
        }
        String setUsername = subject.getUsername();
        if (isBlank(setUsername)) {
            setUsername = defaultUsername;
        }

        String oUsername = null;
        if (null != other) {
            oUsername = checkProperty(context,
                setOf(
                    SERVERS + DOT + type + DOT + name + DOT + USERNAME,
                    SERVERS + DOT + type + DOT + USERNAME),
                SERVERS + DOT + type + DOT + name + DOT + USERNAME,
                other.getUsername(),
                null,
                errors,
                true);
        }

        String sUsername = subject.getUsername();
        subject.setUsername(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + USERNAME,
                    prefix + DOT + type + DOT + USERNAME,
                    type + DOT + name + DOT + USERNAME,
                    type + DOT + USERNAME),
                prefix + DOT + type + DOT + name + DOT + USERNAME,
                isNotBlank(sUsername) ? sUsername : oUsername,
                setUsername,
                errors,
                continueOnError));
    }

    public static void validatePassword(JReleaserContext context, Authenticatable subject, Authenticatable other, String prefix, String type, String name, Errors errors, boolean continueOnError) {
        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();
        String defaultPassword = null;
        String classType = subject.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        if (classType.contains(service.getServiceName().toLowerCase(Locale.ROOT))) {
            defaultPassword = service.getToken();
        }
        String setPassword = subject.getPassword();
        if (isBlank(setPassword)) {
            setPassword = defaultPassword;
        }

        String oPassword = null;
        if (null != other) {
            oPassword = checkProperty(context,
                setOf(
                    SERVERS + DOT + type + DOT + name + DOT + PASSWORD,
                    SERVERS + DOT + type + DOT + name + DOT + TOKEN,
                    SERVERS + DOT + type + DOT + PASSWORD,
                    SERVERS + DOT + type + DOT + TOKEN),
                SERVERS + DOT + type + DOT + name + DOT + PASSWORD,
                other.getPassword(),
                null,
                errors,
                true);
        }

        String sPassword = subject.getPassword();
        subject.setPassword(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + PASSWORD,
                    prefix + DOT + type + DOT + name + DOT + TOKEN,
                    prefix + DOT + type + DOT + PASSWORD,
                    prefix + DOT + type + DOT + TOKEN,
                    type + DOT + name + DOT + PASSWORD,
                    type + DOT + name + DOT + TOKEN,
                    type + DOT + PASSWORD,
                    type + DOT + TOKEN),
                prefix + DOT + type + DOT + name + DOT + PASSWORD,
                isNotBlank(sPassword) ? sPassword : oPassword,
                setPassword,
                errors,
                continueOnError));
    }
}
