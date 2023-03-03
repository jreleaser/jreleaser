/*
 * Copyright 2014 Trustin Heuiseung Lee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.dependencies.os;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Detector {

    public static final String DETECTED_NAME = "os.detected.name";
    public static final String DETECTED_ARCH = "os.detected.arch";
    public static final String DETECTED_BITNESS = "os.detected.bitness";
    public static final String DETECTED_VERSION = "os.detected.version";
    public static final String DETECTED_VERSION_MAJOR = DETECTED_VERSION + ".major";
    public static final String DETECTED_VERSION_MINOR = DETECTED_VERSION + ".minor";
    public static final String DETECTED_CLASSIFIER = "os.detected.classifier";
    public static final String DETECTED_RELEASE = "os.detected.release";
    public static final String DETECTED_RELEASE_VERSION = DETECTED_RELEASE + ".version";
    public static final String DETECTED_RELEASE_LIKE_PREFIX = DETECTED_RELEASE + ".like.";

    private static final String UNKNOWN = "unknown";
    private static final String LINUX_ID_PREFIX = "ID=";
    private static final String LINUX_ID_LIKE_PREFIX = "ID_LIKE=";
    private static final String LINUX_VERSION_ID_PREFIX = "VERSION_ID=";
    private static final String[] LINUX_OS_RELEASE_FILES = {"/etc/os-release", "/usr/lib/os-release"};
    private static final String REDHAT_RELEASE_FILE = "/etc/redhat-release";
    private static final String[] DEFAULT_REDHAT_VARIANTS = {"rhel", "fedora"};

    private static final Pattern VERSION_REGEX = Pattern.compile("((\\d+)\\.(\\d+)).*");
    private static final Pattern REDHAT_MAJOR_VERSION_REGEX = Pattern.compile("(\\d+)");

    private final SystemPropertyOperationProvider systemPropertyOperationProvider;
    private final FileOperationProvider fileOperationProvider;

    public Detector() {
        this(new SimpleSystemPropertyOperations(), new SimpleFileOperations());
    }

    public Detector(SystemPropertyOperationProvider systemPropertyOperationProvider,
        FileOperationProvider fileOperationProvider) {
        this.systemPropertyOperationProvider = systemPropertyOperationProvider;
        this.fileOperationProvider = fileOperationProvider;
    }

    protected void detect(Properties props, List<String> classifierWithLikes) {
        log("------------------------------------------------------------------------");
        log("Detecting the operating system and CPU architecture");
        log("------------------------------------------------------------------------");

        final String osName = systemPropertyOperationProvider.getSystemProperty("os.name");
        final String osArch = systemPropertyOperationProvider.getSystemProperty("os.arch");
        final String osVersion = systemPropertyOperationProvider.getSystemProperty("os.version");

        final String detectedName = normalizeOs(osName);
        final String detectedArch = normalizeArch(osArch);
        final int detectedBitness = determineBitness(detectedArch);

        setProperty(props, DETECTED_NAME, detectedName);
        setProperty(props, DETECTED_ARCH, detectedArch);
        setProperty(props, DETECTED_BITNESS, "" + detectedBitness);

        final Matcher versionMatcher = VERSION_REGEX.matcher(osVersion);
        if (versionMatcher.matches()) {
            setProperty(props, DETECTED_VERSION, versionMatcher.group(1));
            setProperty(props, DETECTED_VERSION_MAJOR, versionMatcher.group(2));
            setProperty(props, DETECTED_VERSION_MINOR, versionMatcher.group(3));
        }

        final String failOnUnknownOS =
            systemPropertyOperationProvider.getSystemProperty("failOnUnknownOS");
        if (!"false".equalsIgnoreCase(failOnUnknownOS)) {
            if (UNKNOWN.equals(detectedName)) {
                throw new DetectionException("unknown os.name: " + osName);
            }
            if (UNKNOWN.equals(detectedArch)) {
                throw new DetectionException("unknown os.arch: " + osArch);
            }
        }

        // Assume the default classifier, without any os "like" extension.
        final StringBuilder detectedClassifierBuilder = new StringBuilder();
        detectedClassifierBuilder.append(detectedName);
        detectedClassifierBuilder.append('-');
        detectedClassifierBuilder.append(detectedArch);

        // For Linux systems, add additional properties regarding details of the OS.
        final LinuxRelease linuxRelease = "linux".equals(detectedName) ? getLinuxRelease() : null;
        if (linuxRelease != null) {
            setProperty(props, DETECTED_RELEASE, linuxRelease.id);
            if (linuxRelease.version != null) {
                setProperty(props, DETECTED_RELEASE_VERSION, linuxRelease.version);
            }

            // Add properties for all systems that this OS is "like".
            for (String like : linuxRelease.like) {
                final String propKey = DETECTED_RELEASE_LIKE_PREFIX + like;
                setProperty(props, propKey, "true");
            }

            // If any of the requested classifier likes are found in the "likes" for this system,
            // append it to the classifier.
            for (String classifierLike : classifierWithLikes) {
                if (linuxRelease.like.contains(classifierLike)) {
                    detectedClassifierBuilder.append('-');
                    detectedClassifierBuilder.append(classifierLike);
                    // First one wins.
                    break;
                }
            }
        }
        setProperty(props, DETECTED_CLASSIFIER, detectedClassifierBuilder.toString());
    }

    private void setProperty(Properties props, String name, String value) {
        props.setProperty(name, value);
        systemPropertyOperationProvider.setSystemProperty(name, value);
        logProperty(name, value);
    }

    protected abstract void log(String message);
    protected abstract void logProperty(String name, String value);

    private static String normalizeOs(String value) {
        value = normalize(value);
        if (value.startsWith("aix")) {
            return "aix";
        }
        if (value.startsWith("hpux")) {
            return "hpux";
        }
        if (value.startsWith("os400")) {
            // Avoid the names such as os4000
            if (value.length() <= 5 || !Character.isDigit(value.charAt(5))) {
                return "os400";
            }
        }
        if (value.startsWith("linux")) {
            return "linux";
        }
        if (value.startsWith("mac") || value.startsWith("osx")) {
            return "osx";
        }
        if (value.startsWith("freebsd")) {
            return "freebsd";
        }
        if (value.startsWith("openbsd")) {
            return "openbsd";
        }
        if (value.startsWith("netbsd")) {
            return "netbsd";
        }
        if (value.startsWith("solaris") || value.startsWith("sunos")) {
            return "sunos";
        }
        if (value.startsWith("windows")) {
            return "windows";
        }
        if (value.startsWith("zos")) {
            return "zos";
        }

        return UNKNOWN;
    }

    private static String normalizeArch(String value) {
        value = normalize(value);
        if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return "x86_64";
        }
        if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return "x86_32";
        }
        if (value.matches("^(ia64w?|itanium64)$")) {
            return "itanium_64";
        }
        if ("ia64n".equals(value)) {
            return "itanium_32";
        }
        if (value.matches("^(sparc|sparc32)$")) {
            return "sparc_32";
        }
        if (value.matches("^(sparcv9|sparc64)$")) {
            return "sparc_64";
        }
        if (value.matches("^(arm|arm32)$")) {
            return "arm_32";
        }
        if ("aarch64".equals(value)) {
            return "aarch_64";
        }
        if (value.matches("^(mips|mips32)$")) {
            return "mips_32";
        }
        if (value.matches("^(mipsel|mips32el)$")) {
            return "mipsel_32";
        }
        if ("mips64".equals(value)) {
            return "mips_64";
        }
        if ("mips64el".equals(value)) {
            return "mipsel_64";
        }
        if (value.matches("^(ppc|ppc32)$")) {
            return "ppc_32";
        }
        if (value.matches("^(ppcle|ppc32le)$")) {
            return "ppcle_32";
        }
        if ("ppc64".equals(value)) {
            return "ppc_64";
        }
        if ("ppc64le".equals(value)) {
            return "ppcle_64";
        }
        if ("s390".equals(value)) {
            return "s390_32";
        }
        if ("s390x".equals(value)) {
            return "s390_64";
        }
        if (value.matches("^(riscv|riscv32)$")) {
            return "riscv";
        }
        if ("riscv64".equals(value)) {
            return "riscv64";
        }
        if ("e2k".equals(value)) {
            return "e2k";
        }
        if ("loongarch64".equals(value)) {
            return "loongarch_64";
        }
        return UNKNOWN;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }

    private LinuxRelease getLinuxRelease() {
        // First, look for the os-release file.
        for (String osReleaseFileName : LINUX_OS_RELEASE_FILES) {
            LinuxRelease res = parseLinuxOsReleaseFile(osReleaseFileName);
            if (res != null) {
                return res;
            }
        }

        // Older versions of redhat don't have /etc/os-release. In this case, try
        // parsing this file.
        return parseLinuxRedhatReleaseFile(REDHAT_RELEASE_FILE);
    }

    /**
     * Parses a file in the format of {@code /etc/os-release} and return a {@link LinuxRelease}
     * based on the {@code ID}, {@code ID_LIKE}, and {@code VERSION_ID} entries.
     */
    private LinuxRelease parseLinuxOsReleaseFile(String fileName) {
        BufferedReader reader = null;
        try {
            InputStream in = fileOperationProvider.readFile(fileName);
            reader = new BufferedReader(new InputStreamReader(in, "utf-8"));

            String id = null;
            String version = null;
            final Set<String> likeSet = new LinkedHashSet<String>();
            String line;
            while((line = reader.readLine()) != null) {
                // Parse the ID line.
                if (line.startsWith(LINUX_ID_PREFIX)) {
                    // Set the ID for this version.
                    id = normalizeOsReleaseValue(line.substring(LINUX_ID_PREFIX.length()));

                    // Also add the ID to the "like" set.
                    likeSet.add(id);
                    continue;
                }

                // Parse the VERSION_ID line.
                if (line.startsWith(LINUX_VERSION_ID_PREFIX)) {
                    // Set the ID for this version.
                    version = normalizeOsReleaseValue(line.substring(LINUX_VERSION_ID_PREFIX.length()));
                    continue;
                }

                // Parse the ID_LIKE line.
                if (line.startsWith(LINUX_ID_LIKE_PREFIX)) {
                    line = normalizeOsReleaseValue(line.substring(LINUX_ID_LIKE_PREFIX.length()));

                    // Split the line on any whitespace.
                    final String[] parts =  line.split("\\s+");
                    Collections.addAll(likeSet, parts);
                }
            }

            if (id != null) {
                return new LinuxRelease(id, version, likeSet);
            }
        } catch (IOException ignored) {
            // Just absorb. Don't treat failure to read /etc/os-release as an error.
        } finally {
            closeQuietly(reader);
        }
        return null;
    }

    /**
     * Parses the {@code /etc/redhat-release} and returns a {@link LinuxRelease} containing the
     * ID and like ["rhel", "fedora", ID]. Currently only supported for CentOS, Fedora, and RHEL.
     * Other variants will return {@code null}.
     */
    private LinuxRelease parseLinuxRedhatReleaseFile(String fileName) {
        BufferedReader reader = null;
        try {
            InputStream in = fileOperationProvider.readFile(fileName);
            reader = new BufferedReader(new InputStreamReader(in, "utf-8"));

            // There is only a single line in this file.
            String line = reader.readLine();
            if (line != null) {
                line = line.toLowerCase(Locale.US);

                final String id;
                String version = null;
                if (line.contains("centos")) {
                    id = "centos";
                } else if (line.contains("fedora")) {
                    id = "fedora";
                } else if (line.contains("red hat enterprise linux")) {
                    id = "rhel";
                } else {
                    // Other variants are not currently supported.
                    return null;
                }

                final Matcher versionMatcher = REDHAT_MAJOR_VERSION_REGEX.matcher(line);
                if (versionMatcher.find()) {
                    version = versionMatcher.group(1);
                }

                final Set<String> likeSet = new LinkedHashSet<String>(Arrays.asList(DEFAULT_REDHAT_VARIANTS));
                likeSet.add(id);

                return new LinuxRelease(id, version, likeSet);
            }
        } catch (IOException ignored) {
            // Just absorb. Don't treat failure to read /etc/os-release as an error.
        } finally {
            closeQuietly(reader);
        }
        return null;
    }

    private static String normalizeOsReleaseValue(String value) {
        // Remove any quotes from the string.
        return value.trim().replace("\"", "");
    }

    private int determineBitness(String architecture) {
        // try the widely adopted sun specification first.
        String bitness = systemPropertyOperationProvider.getSystemProperty("sun.arch.data.model", "");

        if (!bitness.isEmpty() && bitness.matches("[0-9]+")) {
            return Integer.parseInt(bitness, 10);
        }

        // bitness from sun.arch.data.model cannot be used. Try the IBM specification.
        bitness = systemPropertyOperationProvider.getSystemProperty("com.ibm.vm.bitmode", "");

        if (!bitness.isEmpty() && bitness.matches("[0-9]+")) {
            return Integer.parseInt(bitness, 10);
        }

        // as a last resort, try to determine the bitness from the architecture.
      return guessBitnessFromArchitecture(architecture);
    }

    public static int guessBitnessFromArchitecture(final String arch) {
        if (arch.contains("64")) {
            return 64;
        }

        return 32;
    }


    private static void closeQuietly(Closeable obj) {
        try {
            if (obj != null) {
                obj.close();
            }
        } catch (IOException ignored) {
            // Ignore.
        }
    }

    private static class LinuxRelease {
        final String id;
        final String version;
        final Collection<String> like;

        LinuxRelease(String id, String version, Set<String> like) {
            this.id = id;
            this.version = version;
            this.like = Collections.unmodifiableCollection(like);
        }
    }

    private static class SimpleSystemPropertyOperations implements SystemPropertyOperationProvider {
        @Override
        public String getSystemProperty(String name) {
            return System.getProperty(name);
        }

        @Override
        public String getSystemProperty(String name, String def) {
            return System.getProperty(name, def);
        }

        @Override
        public String setSystemProperty(String name, String value) {
            return System.setProperty(name, value);
        }
    }

    private static class SimpleFileOperations implements FileOperationProvider {
        @Override
        public InputStream readFile(String fileName) throws IOException {
            return new FileInputStream(fileName);
        }
    }
}
