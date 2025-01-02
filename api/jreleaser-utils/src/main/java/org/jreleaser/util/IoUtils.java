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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public final class IoUtils {
    private IoUtils() {
        // noop
    }

    public static Scanner newScanner(InputStream in) {
        return new Scanner(in, UTF_8.name());
    }

    public static PrintWriter newPrintWriter(OutputStream out) {
        return newPrintWriter(out, true);
    }

    public static PrintWriter newPrintWriter(OutputStream out, boolean autoFlush) {
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, UTF_8)), autoFlush);
    }

    public static PrintStream newPrintStream(OutputStream out) {
        return newPrintStream(out, true);
    }

    public static PrintStream newPrintStream(OutputStream out, boolean autoFlush) {
        try {
            return new PrintStream(out, autoFlush, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String toString(ByteArrayOutputStream out) {
        try {
            return out.toString(UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void withInputStream(InputStream input, Consumer<? super Character> consumer) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(input, UTF_8)) {
            int ch;
            while ((ch = reader.read()) != -1) {
                consumer.accept((char) ch);
            }
        }
    }

    public static void withLines(InputStream input, Consumer<? super String> consumer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8))) {
            reader.lines().forEach(consumer);
        }
    }

    public static Reader newInputStreamReader(InputStream in) {
        return newInputStreamReader(in, UTF_8);
    }

    public static Reader newInputStreamReader(InputStream in, Charset cs) {
        if (null != in) {
            if (cs == null) {
                cs = UTF_8;
            }

            return new InputStreamReader(in, cs);
        }

        return new StringReader("");
    }
}
