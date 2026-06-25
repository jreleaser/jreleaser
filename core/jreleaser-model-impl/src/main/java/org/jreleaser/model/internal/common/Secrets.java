/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.model.internal.common;

import org.jreleaser.model.Constants;

import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.25.0
 */
public final class Secrets {
    private static final String SECRET_KEYWORDS = "password,secret,credential,token,apikey,login,passphrase,consumerkey,publickey,accesskey,webhook";

    public static String sanitizeSecret(String value) {
        if (isBlank(value)) return Constants.UNSET;

        if (getBoolean("JRELEASER_SECRETS_HINT")) {
            char c1 = value.charAt(0);
            char c2 = value.length() > 1 ? value.charAt(value.length() - 1) : '\0';
            return c1 + Constants.HIDE + c2;
        }

        return Constants.HIDE;
    }

    public static boolean isSecret(String key) {
        String lower = key.toLowerCase(Locale.ENGLISH);

        for (String keyword : SECRET_KEYWORDS.split(",")) {
            if (lower.contains(keyword.trim().toLowerCase(Locale.ENGLISH))) return true;
        }

        return false;
    }

    private static boolean getBoolean(String key) {
        try {
            return Boolean.parseBoolean(System.getenv(key));
        } catch (IllegalArgumentException | NullPointerException e) {
            // noop
        }
        return false;
    }
}
