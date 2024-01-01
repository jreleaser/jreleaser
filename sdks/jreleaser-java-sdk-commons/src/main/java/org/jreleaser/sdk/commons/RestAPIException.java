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
package org.jreleaser.sdk.commons;

import feign.Request;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class RestAPIException extends RuntimeException {
    private static final long serialVersionUID = -3807652106740042028L;

    private final Request request;
    private final int status;
    private final String reason;
    private final String body;
    private final Map<String, Collection<String>> headers;

    public RestAPIException(int status, String reason) {
        this(null, status, reason, Collections.emptyMap());
    }

    public RestAPIException(int status, String reason, String body) {
        this(null, status, reason, body, Collections.emptyMap());
    }

    public RestAPIException(Request request, int status, String reason, Map<String, Collection<String>> headers) {
        this(request, status, reason, "", headers);
    }

    public RestAPIException(Request request, int status, String reason, String body, Map<String, Collection<String>> headers) {
        super(status + ": " + reason + (isNotBlank(body) ? System.lineSeparator() + body : ""));
        this.request = request;
        this.status = status;
        this.reason = reason;
        this.body = body;
        this.headers = headers;
    }

    public Request getRequest() {
        return request;
    }

    public int getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getBody() {
        return body;
    }

    public Map<String, Collection<String>> getHeaders() {
        return headers;
    }

    public boolean isNotFound() {
        return 404 == status;
    }

    public boolean isForbidden() {
        return 403 == status;
    }

    public boolean isUnprocessableEntity() {
        return 422 == status;
    }
}
