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

import java.util.Collection;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class ObjectUtils {
    private static final String MESSAGE = "message";

    private ObjectUtils() {
        // noop
    }

    /**
     * Checks that the specified condition is met. This method is designed
     * primarily for doing parameter validation in methods and constructors,
     * as demonstrated below:
     * <blockquote><pre>
     * public Foo(int[] array) {
     *     GriffonClassUtils.requireState(array.length &gt; 0);
     * }
     * </pre></blockquote>
     *
     * @param condition the condition to check
     * @throws IllegalStateException if {@code condition} evaluates to false
     */
    public static void requireState(boolean condition) {
        if (!condition) {
            throw new IllegalStateException();
        }
    }

    /**
     * Checks that the specified condition is met and throws a customized
     * {@link IllegalStateException} if it is. This method is designed primarily
     * for doing parameter validation in methods and constructors with multiple
     * parameters, as demonstrated below:
     * <blockquote><pre>
     * public Foo(int[] array) {
     *     GriffonClassUtils.requireState(array.length &gt; 0, "array must not be empty");
     * }
     * </pre></blockquote>
     *
     * @param condition the condition to check
     * @param message   detail message to be used in the event that a {@code
     *                  IllegalStateException} is thrown
     * @throws IllegalStateException if {@code condition} evaluates to false
     */
    public static void requireState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static byte[] requireNonEmpty(byte[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static byte[] requireNonEmpty(byte[] array, String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static short[] requireNonEmpty(short[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static short[] requireNonEmpty(short[] array, String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static int[] requireNonEmpty(int[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static int[] requireNonEmpty(int[] array, String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static long[] requireNonEmpty(long[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static long[] requireNonEmpty(long[] array, String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static float[] requireNonEmpty(float[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static float[] requireNonEmpty(float[] array, String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static double[] requireNonEmpty(double[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static double[] requireNonEmpty(double[] array, String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static char[] requireNonEmpty(char[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static char[] requireNonEmpty(char[] array, String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static boolean[] requireNonEmpty(boolean[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static boolean[] requireNonEmpty(boolean[] array, String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param array the array to check
     * @throws NullPointerException  if {@code array} is null
     * @throws IllegalStateException if {@code array} is empty
     */
    public static <E> E[] requireNonEmpty(E[] array) {
        requireNonNull(array);
        requireState(array.length != 0);
        return array;
    }

    /**
     * Checks that the specified array is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param array   the array to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code array} is empty
     */
    public static <E> E[] requireNonEmpty(E[] array, String message) {
        requireNonNull(array);
        requireState(array.length != 0, requireNonBlank(message, MESSAGE));
        return array;
    }

    /**
     * Checks that the specified collection is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param collection the collection to check
     * @throws NullPointerException  if {@code collection} is null
     * @throws IllegalStateException if {@code collection} is empty
     */
    public static Collection<?> requireNonEmpty(Collection<?> collection) {
        requireNonNull(collection);
        requireState(!collection.isEmpty());
        return collection;
    }

    /**
     * Checks that the specified collection is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param collection the collection to check
     * @param message    detail message to be used in the event that a {@code
     *                   IllegalStateException} is thrown
     * @throws NullPointerException     if {@code collection} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code collection} is empty
     */
    public static Collection<?> requireNonEmpty(Collection<?> collection, String message) {
        requireNonNull(collection);
        requireState(!collection.isEmpty(), requireNonBlank(message, MESSAGE));
        return collection;
    }

    /**
     * Checks that the specified map is not empty, throwing a
     * {@link IllegalStateException} if it is.
     *
     * @param map the map to check
     * @throws NullPointerException  if {@code map} is null
     * @throws IllegalStateException if {@code map} is empty
     */
    public static Map<?, ?> requireNonEmpty(Map<?, ?> map) {
        requireNonNull(map);
        requireState(!map.isEmpty());
        return map;
    }

    /**
     * Checks that the specified map is not empty, throwing a customized
     * {@link IllegalStateException} if it is.
     *
     * @param map     the map to check
     * @param message detail message to be used in the event that a {@code
     *                IllegalStateException} is thrown
     * @throws NullPointerException     if {@code map} is null
     * @throws IllegalArgumentException if {@code message} is {@code blank}
     * @throws IllegalStateException    if {@code map} is empty
     */
    public static Map<?, ?> requireNonEmpty(Map<?, ?> map, String message) {
        requireNonNull(map);
        requireState(!map.isEmpty(), requireNonBlank(message, MESSAGE));
        return map;
    }
}