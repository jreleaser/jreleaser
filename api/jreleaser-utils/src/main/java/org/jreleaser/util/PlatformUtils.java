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

import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class PlatformUtils {
    private static final OsDetector OS_DETECTOR = new OsDetector();
    private static final List<String> OS_NAMES = new ArrayList<>();
    private static final List<String> OS_ARCHS = new ArrayList<>();
    private static final String JRELEASER_PLATFORM_OVERRIDE = "JRELEASER_PLATFORM_OVERRIDE";
    private static String currentPlatform;

    static {
        OS_NAMES.addAll(Arrays.asList(
            "aix",
            "hpux",
            "os400",
            "linux",
            "linux_musl",
            "osx",
            "freebsd",
            "openbsd",
            "netbsd",
            "sunos",
            "windows",
            "zos"));
        OS_NAMES.sort(Comparator.naturalOrder());

        OS_ARCHS.addAll(Arrays.asList(
            "x86_64",
            "x86_32",
            "itanium_64",
            "itanium_32",
            "sparc_32",
            "sparc_64",
            "arm_32",
            "aarch_64",
            "mips_32",
            "mipsel_32",
            "mips_64",
            "mipsel_64",
            "ppc_32",
            "ppcle_32",
            "ppc_64",
            "ppcle_64",
            "s390_32",
            "s390_64",
            "riscv"));
        OS_ARCHS.sort(Comparator.naturalOrder());
    }

    private PlatformUtils() {
        //noop
    }

    public static List<String> getSupportedOsNames() {
        return Collections.unmodifiableList(OS_NAMES);
    }

    public static List<String> getSupportedOsArchs() {
        return Collections.unmodifiableList(OS_ARCHS);
    }

    public static boolean isSupported(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        switch (parts.length) {
            case 1:
                return OS_NAMES.contains(parts[0]);
            case 2:
                return OS_NAMES.contains(parts[0]) && OS_ARCHS.contains(parts[1]);
            default:
                return false;
        }
    }

    public static boolean isIntel(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        switch (parts.length) {
            case 1:
                return osNameOrClassifier.contains("x86_64") ||
                    osNameOrClassifier.contains("x86_32");
            case 2:
                return "x86_64".equals(parts[1]) ||
                    "x86_32".equals(parts[1]);
            default:
                return false;
        }
    }

    public static boolean isIntel32(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        switch (parts.length) {
            case 1:
                return osNameOrClassifier.contains("x86_32");
            case 2:
                return "x86_32".equals(parts[1]);
            default:
                return false;
        }
    }

    public static boolean isIntel64(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        switch (parts.length) {
            case 1:
                return osNameOrClassifier.contains("x86_64");
            case 2:
                return "x86_64".equals(parts[1]);
            default:
                return false;
        }
    }

    public static boolean isArm(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        switch (parts.length) {
            case 1:
                return osNameOrClassifier.contains("arm_32") ||
                    osNameOrClassifier.contains("aarch_64");
            case 2:
                return "arm_32".equals(parts[1]) ||
                    "aarch_64".equals(parts[1]);
            default:
                return false;
        }
    }

    public static boolean isArm32(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        switch (parts.length) {
            case 1:
                return osNameOrClassifier.contains("arm_32");
            case 2:
                return "arm_32".equals(parts[1]);
            default:
                return false;
        }
    }

    public static boolean isArm64(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        switch (parts.length) {
            case 1:
                return osNameOrClassifier.contains("aarch_64");
            case 2:
                return "aarch_64".equals(parts[1]);
            default:
                return false;
        }
    }

    public static boolean isWindows(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        return "windows".equalsIgnoreCase(parts[0]);
    }

    public static boolean isMac(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        return "osx".equalsIgnoreCase(parts[0]);
    }

    public static boolean isUnix(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        return OS_NAMES.contains(parts[0]) &&
            !"osx".equalsIgnoreCase(parts[0]) &&
            !"windows".equalsIgnoreCase(parts[0]);
    }

    public static boolean isLinux(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        return OS_NAMES.contains(parts[0]) &&
            parts[0].contains("linux");
    }

    public static boolean isAlpineLinux(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        return "linux_musl".equalsIgnoreCase(parts[0]);
    }

    public static String getCurrent() {
        String platform = getDetectedOs();

        if (isLinux(platform)) {
            String javaHome = System.getProperty("java.home");
            if (isBlank(javaHome)) {
                // Can only happen when running as native-image, ignore for now
                // TODO: native-image
                return platform;
            }

            Path release = Paths.get(javaHome).resolve("release");

            try (InputStream in = Files.newInputStream(release)) {
                Properties props = new Properties();
                props.load(in);
                if (props.containsKey("LIBC")) {
                    String libc = props.getProperty("LIBC");
                    if ("musl".equalsIgnoreCase(libc)) {
                        platform += "_musl";
                    }
                }
            } catch (IOException ignored) {
                // ignored
            }
        }

        return platform;
    }

    public static void resolveCurrentPlatform(JReleaserLogger logger) {
        String resolved = getCurrent() + "-" + getDetectedArch();
        String platform = System.getenv().getOrDefault(JRELEASER_PLATFORM_OVERRIDE,
            System.getProperty(JRELEASER_PLATFORM_OVERRIDE, resolved));

        if (!isSupported(platform)) {
            logger.warn(RB.$("ERROR_unsupported_platform_override", platform, resolved));
            platform = resolved;
        }

        currentPlatform = platform;
    }

    public static String getCurrentFull() {
        return currentPlatform;
    }

    public static boolean isWindows() {
        return "windows".equalsIgnoreCase(getDetectedOs());
    }

    public static boolean isMac() {
        return "osx".equalsIgnoreCase(getDetectedOs());
    }

    public static boolean isCompatible(String expected, String actual) {
        if (isBlank(actual)) return false;

        if (expected.contains("-")) {
            // expected is strict
            if (actual.contains("-")) {
                // actual is strict
                return expected.equalsIgnoreCase(actual);
            } else {
                return expected.toLowerCase(Locale.ENGLISH).contains(actual.toLowerCase(Locale.ENGLISH));
            }
        }

        String[] parts = actual.split("-");
        return expected.equalsIgnoreCase(parts[0]);
    }

    public static String getDetectedOs() {
        return OS_DETECTOR.get(OsDetector.DETECTED_NAME);
    }

    public static String getDetectedArch() {
        return OS_DETECTOR.get(OsDetector.DETECTED_ARCH);
    }

    public static String getDetectedVersion() {
        return OS_DETECTOR.get(OsDetector.DETECTED_VERSION);
    }
}
