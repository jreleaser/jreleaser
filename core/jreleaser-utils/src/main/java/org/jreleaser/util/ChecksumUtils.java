/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ChecksumUtils {
    private ChecksumUtils() {
        // prevent instantiation
    }

    public static String checksum(Algorithm algorithm, byte[] data) throws IOException {
        switch (algorithm) {
            case MD2:
                return DigestUtils.md2Hex(data);
            case MD5:
                return DigestUtils.md5Hex(data);
            case SHA_1:
                return DigestUtils.sha1Hex(data);
            case SHA_256:
                return DigestUtils.sha256Hex(data);
            case SHA_384:
                return DigestUtils.sha384Hex(data);
            case SHA_512:
                return DigestUtils.sha512Hex(data);
            case SHA3_224:
                return DigestUtils.sha3_224Hex(data);
            case SHA3_256:
                return DigestUtils.sha3_256Hex(data);
            case SHA3_384:
                return DigestUtils.sha3_384Hex(data);
            case SHA3_512:
                return DigestUtils.sha3_512Hex(data);
            default:
                throw new IOException("Unsupported algorithm " + algorithm.name());
        }
    }
}
