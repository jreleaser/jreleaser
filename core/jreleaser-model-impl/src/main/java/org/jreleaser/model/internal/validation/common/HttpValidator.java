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
import org.jreleaser.model.internal.servers.HttpServer;
import org.jreleaser.util.Errors;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.model.internal.validation.common.AuthenticatableValidator.validatePassword;
import static org.jreleaser.model.internal.validation.common.AuthenticatableValidator.validateUsername;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class HttpValidator {
    private static final String HTTP = "http";

    private HttpValidator() {
        // noop
    }

    public static void validateHttp(JReleaserContext context, Http http, HttpServer server, String prefix, String name, Errors errors) {
        validateHttp(context, http, server, prefix, HTTP, name, errors);
    }

    public static void validateHttp(JReleaserContext context, Http http, HttpServer server, String prefix, String type, String name, Errors errors) {
        org.jreleaser.model.Http.Authorization pauth = null != server ? server.getAuthorization() : null;
        org.jreleaser.model.Http.Authorization sauth = http.getAuthorization();

        if (null == sauth) http.setAuthorization(pauth);

        switch (http.resolveAuthorization()) {
            case BEARER:
                validatePassword(context, http, server, prefix, type, name, errors, context.isDryrun());
                break;
            case BASIC:
                validateUsername(context, http, server, prefix, type, name, errors, context.isDryrun());
                validatePassword(context, http, server, prefix, type, name, errors, context.isDryrun());
                break;
            case NONE:
                break;
        }

        if (null != server) {
            Map<String, String> headers = new LinkedHashMap<>(server.getHeaders());
            headers.putAll(http.getHeaders());
            http.getHeaders().putAll(headers);
        }
    }
}
