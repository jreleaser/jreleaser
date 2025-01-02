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
package org.jreleaser.model.internal.validation.common;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Http;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.util.CollectionUtils.listOf;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class HttpValidator {
    private static final String PASSWORD = ".password";
    private static final String USERNAME = ".username";

    private HttpValidator() {
        // noop
    }

    public static void validateHttp(JReleaserContext context, Http http, String prefix, String name, Errors errors) {
        String baseKey1 = prefix + ".http." + name;
        String baseKey2 = prefix + ".http";
        String baseKey3 = "http." + name;
        String baseKey4 = "http";

        switch (http.resolveAuthorization()) {
            case BEARER:
                http.setPassword(
                    checkProperty(context,
                        listOf(
                            baseKey1 + PASSWORD,
                            baseKey2 + PASSWORD,
                            baseKey3 + PASSWORD,
                            baseKey4 + PASSWORD),
                        baseKey1 + PASSWORD,
                        http.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case BASIC:
                http.setUsername(
                    checkProperty(context,
                        listOf(
                            baseKey1 + USERNAME,
                            baseKey2 + USERNAME,
                            baseKey3 + USERNAME,
                            baseKey4 + USERNAME),
                        baseKey1 + USERNAME,
                        http.getUsername(),
                        errors,
                        context.isDryrun()));

                http.setPassword(
                    checkProperty(context,
                        listOf(
                            baseKey1 + PASSWORD,
                            baseKey2 + PASSWORD,
                            baseKey3 + PASSWORD,
                            baseKey4 + PASSWORD),
                        baseKey1 + PASSWORD,
                        http.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case NONE:
                break;
        }
    }
}
