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
package org.jreleaser.model;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import static org.jreleaser.util.IoUtils.newPrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserVersion {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(JReleaserVersion.class.getName());
    private static final String JRELEASER_VERSION = BUNDLE.getString("jreleaser_version");
    private static final String BUILD_DATE = BUNDLE.getString("build_date");
    private static final String BUILD_TIME = BUNDLE.getString("build_time");
    private static final String BUILD_REVISION = BUNDLE.getString("build_revision");
    private static final String SEPARATOR = "------------------------------------------------------------%n";
    private static final String JRELEASER_FORMAT = "jreleaser %s%n";

    private JReleaserVersion() {
        // noop
    }

    public static String getPlainVersion() {
        return JRELEASER_VERSION;
    }

    public static void banner(PrintStream out) {
        banner(out, true);
    }

    public static void banner(PrintStream out, boolean full) {
        banner(newPrintWriter(out), full);
    }

    public static void banner(PrintWriter out) {
        banner(out, true);
    }

    public static void banner(PrintWriter out, boolean full) {
        if (full) {
            out.printf(SEPARATOR);
            out.printf(JRELEASER_FORMAT, JRELEASER_VERSION);

            String jvm = System.getProperty("java.version") + " (" +
                System.getProperty("java.vendor") + " " +
                System.getProperty("java.vm.version") + ")";

            out.printf(SEPARATOR);
            out.printf("Build time:   %s %s%n", BUILD_DATE, BUILD_TIME);
            out.printf("Revision:     %s%n", BUILD_REVISION);
            out.printf("JVM:          %s%n", jvm);
            out.printf(SEPARATOR);
        } else {
            out.printf(JRELEASER_FORMAT, JRELEASER_VERSION);
        }
    }
}
