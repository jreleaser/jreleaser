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
package org.jreleaser.sdk.bluesky.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Simon Verhoeven
 * @author Tom Cools
 * @since 1.7.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSessionRequest {
    private String identifier;

    private String password;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static CreateSessionRequest of(String identifier, String password) {
        CreateSessionRequest r = new CreateSessionRequest();
        r.identifier = requireNonBlank(identifier, "'identifier' must not be blank");
        r.password = requireNonBlank(password, "'password' must not be blank");
        return r;
    }
}
