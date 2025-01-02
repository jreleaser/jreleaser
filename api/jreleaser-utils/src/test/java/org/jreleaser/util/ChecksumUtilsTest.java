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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChecksumUtilsTest {
    @ParameterizedTest
    @MethodSource("algorithm_factory")
    void testAlgorithmFactory(Algorithm expected, String input) {
        // given:
        Algorithm actual = Algorithm.of(input);

        // then:
        assertThat(actual, equalTo(expected));
    }

    @Test
    void testInvalidAlgorithm() {
        assertThrows(IOException.class, () ->
            ChecksumUtils.checksum(null, "JRELEASER\n".getBytes(UTF_8)));
        assertThrows(IOException.class, () ->
            ChecksumUtils.checksum(Algorithm.SHA_256, null));
        assertThrows(IOException.class, () ->
            ChecksumUtils.checksum(Algorithm.SHA_256, new byte[0]));
    }

    @ParameterizedTest
    @MethodSource("checksums")
    void testChecksums(Algorithm algorithm, String expected) throws IOException {
        // given:
        String actual = ChecksumUtils.checksum(algorithm, "JRELEASER\n".getBytes(UTF_8));

        // then:
        assertThat(actual, equalTo(expected));
    }

    private static Stream<Arguments> algorithm_factory() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(null, ""),
            Arguments.of(Algorithm.MD2, "md2"),
            Arguments.of(Algorithm.MD2, "MD2"),
            Arguments.of(Algorithm.MD5, "md5"),
            Arguments.of(Algorithm.MD5, "MD5"),
            Arguments.of(Algorithm.RMD160, "rmd160"),
            Arguments.of(Algorithm.RMD160, "RMD160"),
            Arguments.of(Algorithm.SHA_1, "sha1"),
            Arguments.of(Algorithm.SHA_1, "SHA1"),
            Arguments.of(Algorithm.SHA_256, "sha256"),
            Arguments.of(Algorithm.SHA_256, "SHA256"),
            Arguments.of(Algorithm.SHA_384, "sha384"),
            Arguments.of(Algorithm.SHA_384, "SHA384"),
            Arguments.of(Algorithm.SHA_512, "sha512"),
            Arguments.of(Algorithm.SHA_512, "SHA512"),
            Arguments.of(Algorithm.SHA3_224, "sha3-224"),
            Arguments.of(Algorithm.SHA3_224, "SHA3-224"),
            Arguments.of(Algorithm.SHA3_224, "SHA3_224"),
            Arguments.of(Algorithm.SHA3_256, "sha3-256"),
            Arguments.of(Algorithm.SHA3_256, "SHA3-256"),
            Arguments.of(Algorithm.SHA3_256, "SHA3_256"),
            Arguments.of(Algorithm.SHA3_384, "sha3-384"),
            Arguments.of(Algorithm.SHA3_384, "SHA3-384"),
            Arguments.of(Algorithm.SHA3_384, "SHA3_384"),
            Arguments.of(Algorithm.SHA3_512, "sha3-512"),
            Arguments.of(Algorithm.SHA3_512, "SHA3-512"),
            Arguments.of(Algorithm.SHA3_512, "SHA3_512")
        );
    }

    private static Stream<Arguments> checksums() {
        return Stream.of(
            Arguments.of(Algorithm.MD2, "a507d7d6fcbc4eb641764ab2ead64b89"),
            Arguments.of(Algorithm.MD5, "bcf296fd2c37d7eed841de01bde0c322"),
            Arguments.of(Algorithm.RMD160, "c419193a7fbb103de91b50c9ca4c9c4153842ade"),
            Arguments.of(Algorithm.SHA_1, "caa084c608363078d6e7185c8cff1aca897cba23"),
            Arguments.of(Algorithm.SHA_256, "d561fd74d2ebaff0b5c1e4ff4b0b918e09ba041e0eeccca1c12b801441b68fdb"),
            Arguments.of(Algorithm.SHA_384, "c94c6956268e4abb4dfd47386f247f0f0969f1f271e738a723881431dca9b7c3149219485685ae4d0de5fd8a262132cf"),
            Arguments.of(Algorithm.SHA_512, "d857ecd08c2c5d356b9dc19bcf420463581e77d0ed7b6d1302effda24ca7beef7f11178019d52a3fbda2ce7a1b6e9457aebfb1b0d73424c41c903c3177066134"),
            Arguments.of(Algorithm.SHA3_224, "583facb35998f20d00bbd4d2234ea0131811a74edf8f7f4d93d823b7"),
            Arguments.of(Algorithm.SHA3_256, "7a771b466755be08915f5be548e23cda121f2ba190576381fdbc7aef680224e4"),
            Arguments.of(Algorithm.SHA3_384, "0c0413287c6b6e4eb6730befb17d9d403eca7e31fa8b524070c6afc4b39b9fbea6702857e28990057ab2ca64fc986826"),
            Arguments.of(Algorithm.SHA3_512, "34a1210d365c5d678e84ba5f1587319c4d333b979b5ab33f27af6fc63074ae01fd4dfe5b6b8ce53051bc033cdf9de8f796b4db836fe9dd1448b6c18c55e395b7")
        );
    }
}
