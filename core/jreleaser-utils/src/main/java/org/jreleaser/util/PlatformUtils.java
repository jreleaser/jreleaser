/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import kr.motd.maven.os.Detector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class PlatformUtils {
    private static final OsDetector OS_DETECTOR = new OsDetector();
    private static final List<String> OS_NAMES = new ArrayList<>();
    private static final List<String> OS_ARCHS = new ArrayList<>();

    static {
        OS_NAMES.addAll(Arrays.asList(
            "aix",
            "hpux",
            "os400",
            "linux",
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

    public static boolean isWindows(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        switch (parts.length) {
            case 1:
            case 2:
                return "windows".equalsIgnoreCase(osNameOrClassifier);
            default:
                return false;
        }
    }

    public static boolean isMac(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        switch (parts.length) {
            case 1:
            case 2:
                return "osx".equalsIgnoreCase(osNameOrClassifier);
            default:
                return false;
        }
    }

    public static boolean isUnix(String osNameOrClassifier) {
        if (isBlank(osNameOrClassifier)) return false;
        String[] parts = osNameOrClassifier.split("-");

        switch (parts.length) {
            case 1:
            case 2:
                return OS_NAMES.contains(parts[0]) &&
                    !"osx".equalsIgnoreCase(osNameOrClassifier) &&
                    !"windows".equalsIgnoreCase(osNameOrClassifier);
            default:
                return false;
        }
    }

    public static String getCurrent() {
        return OS_DETECTOR.get(Detector.DETECTED_NAME);
    }

    public static String getCurrentFull() {
        return getCurrent() + "-" + OS_DETECTOR.get(Detector.DETECTED_ARCH);
    }

    public static boolean isWindows() {
        return "windows".equals(OS_DETECTOR.get(Detector.DETECTED_NAME));
    }

    public static boolean isMac() {
        return "osx".equals(OS_DETECTOR.get(Detector.DETECTED_NAME));
    }

    public static String getValue(String key) {
        return OS_DETECTOR.get(key);
    }

    public static Set<String> keySet() {
        return OS_DETECTOR.getProperties().keySet();
    }

    public static Collection<String> values() {
        return OS_DETECTOR.getProperties().values();
    }

    public static Set<Map.Entry<String, String>> entrySet() {
        return OS_DETECTOR.getProperties().entrySet();
    }

    public static OsDetector getOsDetector() {
        return OS_DETECTOR;
    }
}
