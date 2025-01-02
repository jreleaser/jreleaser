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
package org.jreleaser.sdk.commons.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.nio.charset.Charset;
import java.util.Base64;

import static feign.Util.ISO_8859_1;
import static feign.Util.checkNotNull;

/**
 * @author Andres Almiray
 * @since 1.12.0
 */
public class TokenAuthRequestInterceptor implements RequestInterceptor {
    private static final String BASIC = "Basic";
    private final String headerValue;

    public TokenAuthRequestInterceptor(String username, String password) {
        this(BASIC, username, password, ISO_8859_1);
    }

    public TokenAuthRequestInterceptor(String token, String username, String password) {
        this(token, username, password, ISO_8859_1);
    }

    public TokenAuthRequestInterceptor(String username, String password, Charset charset) {
        this(BASIC, username, password, charset);
    }

    public TokenAuthRequestInterceptor(String token, String username, String password, Charset charset) {
        checkNotNull(token, "token");
        checkNotNull(username, "username");
        checkNotNull(password, "password");
        this.headerValue = token + " " + base64Encode((username + ":" + password).getBytes(charset));
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", headerValue);
    }
}

