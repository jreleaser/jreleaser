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
package org.jreleaser.util;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public enum Algorithm {
    MD2,
    MD5,
    RMD160,
    SHA_1,
    SHA_256,
    SHA_384,
    SHA_512,
    SHA3_224,
    SHA3_256,
    SHA3_384,
    SHA3_512;

    public String formatted() {
        if (name().startsWith("SHA3")) {
            return name().toLowerCase(Locale.ENGLISH).replace("_", "-");
        }
        return name().toLowerCase(Locale.ENGLISH).replace("_", "");
    }

    @JsonCreator
    public static Algorithm of(String str) {
        if (isBlank(str)) return null;

        String value = str.toUpperCase(Locale.ENGLISH).trim()
            .replace("-", "_");

        switch (value) {
            case "SHA1":
                return SHA_1;
            case "SHA256":
                return SHA_256;
            case "SHA384":
                return SHA_384;
            case "SHA512":
                return SHA_512;
            default:
                // noop
        }

        return Algorithm.valueOf(value);
    }
}
