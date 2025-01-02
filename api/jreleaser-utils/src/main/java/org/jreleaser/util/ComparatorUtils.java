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

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public final class ComparatorUtils {
    private ComparatorUtils() {
        // prevent instantiation
    }

    public static <T extends Comparable<T>> boolean greaterThan(T c1, T c2) {
        return c1.compareTo(c2) > 0;
    }

    public static <T extends Comparable<T>> boolean greaterThanOrEqualTo(T c1, T c2) {
        return c1.compareTo(c2) >= 0;
    }

    public static <T extends Comparable<T>> boolean lessThan(T c1, T c2) {
        return c1.compareTo(c2) < 0;
    }

    public static <T extends Comparable<T>> boolean lessThanOrEqualTo(T c1, T c2) {
        return c1.compareTo(c2) <= 0;
    }
}
