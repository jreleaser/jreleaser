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
package org.jreleaser.ant.tasks;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Scanner;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static org.jreleaser.util.IoUtils.newPrintStream;
import static org.jreleaser.util.IoUtils.newScanner;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
final class Banner {
    private static final Banner INSTANCE = new Banner();
    private final ResourceBundle bundle = ResourceBundle.getBundle(Banner.class.getName());
    private final String productVersion = bundle.getString("product.version");
    private final String productId = bundle.getString("product.id");
    private final String productName = bundle.getString("product.name");
    private final String message = MessageFormat.format(bundle.getString("product.banner"), productName, productVersion);

    private Banner() {
        // noop
    }

    public static void display(PrintWriter writer) {
        try {
            File parent = new File(System.getProperty("user.home"), "/.ant/caches");
            File markerFile = getMarkerFile(parent, INSTANCE);
            if (!markerFile.exists()) {
                writer.println(INSTANCE.message);
                markerFile.getParentFile().mkdirs();
                PrintStream out = newPrintStream(newOutputStream(markerFile.toPath()));
                out.println("1");
                out.close();
                writeQuietly(markerFile, "1");
            } else {
                try {
                    int count = Integer.parseInt(readQuietly(markerFile));
                    if (count < 3) {
                        writer.println(INSTANCE.message);
                    }
                    writeQuietly(markerFile, (count + 1) + "");
                } catch (NumberFormatException e) {
                    writeQuietly(markerFile, "1");
                    writer.println(INSTANCE.message);
                }
            }
        } catch (IOException ignored) {
            // noop
        }
    }

    private static void writeQuietly(File file, String text) {
        try {
            PrintStream out = newPrintStream(newOutputStream(file.toPath()));
            out.println(text);
            out.close();
        } catch (IOException ignored) {
            // ignored
        }
    }

    private static String readQuietly(File file) {
        try (Scanner in = newScanner(newInputStream(file.toPath()))) {
            return in.next();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static File getMarkerFile(File parent, Banner b) {
        return new File(parent,
            "jreleaser" +
                File.separator +
                b.productId +
                File.separator +
                b.productVersion +
                File.separator +
                "marker.txt");
    }
}
