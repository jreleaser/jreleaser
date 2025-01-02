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
package org.jreleaser.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;

/**
 * @author Andres Almiray
 */
public abstract class Banner implements BuildService<Banner.Params> {
    private static final String ORG_JRELEASER_BANNER = "org.jreleaser.banner";

    private String productVersion;
    private String productId;
    private final List<String> projectNames = new ArrayList<>();

    interface Params extends BuildServiceParameters {
    }

    public void display(Project project) {
        if (checkIfVisited(project)) return;

        ResourceBundle bundle = ResourceBundle.getBundle(Banner.class.getName());
        productVersion = bundle.getString("product.version");
        productId = bundle.getString("product.id");
        String productName = bundle.getString("product.name");
        String banner = MessageFormat.format(bundle.getString("product.banner"), productName, productVersion);

        boolean printBanner = null == System.getProperty(ORG_JRELEASER_BANNER) || Boolean.getBoolean(ORG_JRELEASER_BANNER);

        File parent = new File(project.getGradle().getGradleUserHomeDir(), "caches");
        File markerFile = getMarkerFile(parent);
        if (!markerFile.exists()) {
            if (printBanner) System.err.println(banner);
            markerFile.getParentFile().mkdirs();
            writeQuietly(markerFile, "1");
        } else {
            try {
                int count = Integer.parseInt(readQuietly(markerFile));
                if (count < 3) {
                    if (printBanner) System.err.println(banner);
                }
                writeQuietly(markerFile, (count + 1) + "");
            } catch (NumberFormatException e) {
                if (printBanner) System.err.println(banner);
                writeQuietly(markerFile, "1");
            }
        }
    }

    private boolean checkIfVisited(Project project) {
        if (projectNames.contains(project.getRootProject().getName())) {
            return true;
        }
        projectNames.add(project.getRootProject().getName());
        return false;
    }

    private File getMarkerFile(File parent) {
        return new File(parent,
            "jreleaser" +
                File.separator +
                productId +
                File.separator +
                productVersion +
                File.separator +
                "marker.txt");
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

    private static Scanner newScanner(InputStream in) {
        return new Scanner(in, UTF_8.name());
    }

    private static PrintStream newPrintStream(OutputStream out) {
        return newPrintStream(out, true);
    }

    private static PrintStream newPrintStream(OutputStream out, boolean autoFlush) {
        try {
            return new PrintStream(out, autoFlush, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
