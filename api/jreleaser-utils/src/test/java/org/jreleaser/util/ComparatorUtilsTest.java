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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComparatorUtilsTest {
    @Test
    void testComparators() {
        // given:
        Integer one = 1;
        Integer two = 2;
        Integer three = 3;

        // then:
        assertAll(
            () -> assertTrue(ComparatorUtils.lessThan(one, two)),
            () -> assertTrue(ComparatorUtils.lessThanOrEqualTo(one, one)),
            () -> assertTrue(ComparatorUtils.lessThanOrEqualTo(one, two)),
            () -> assertTrue(ComparatorUtils.greaterThan(two, one)),
            () -> assertTrue(ComparatorUtils.greaterThanOrEqualTo(two, two)),
            () -> assertTrue(ComparatorUtils.greaterThanOrEqualTo(three, two)),
            () -> assertFalse(ComparatorUtils.lessThan(two, one)),
            () -> assertFalse(ComparatorUtils.lessThanOrEqualTo(two, one)),
            () -> assertFalse(ComparatorUtils.greaterThan(one, two)),
            () -> assertFalse(ComparatorUtils.greaterThanOrEqualTo(two, three))
        );
    }
}
