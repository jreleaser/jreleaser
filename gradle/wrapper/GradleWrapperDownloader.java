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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.zip.ZipFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class GradleWrapperDownloader {
    private static final String GRADLE_VERSION = "8.10";

    private static final String DEFAULT_DOWNLOAD_URL = "https://services.gradle.org/distributions/gradle-" + GRADLE_VERSION + "-bin.zip";
    private static final String DISTRIBUTION_FILE_NAME = "gradle-" + GRADLE_VERSION + "-bin.zip";
    private static final String DISTRIBUTION_NAME = "gradle-" + GRADLE_VERSION;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java GradleWrapperDownloader <project-directory>");
            System.exit(1);
        }

        var dir = args[0].replace("..", ""); // Sanitize path
        var projectBasedir = Paths.get(dir).toAbsolutePath().normalize();
        if (!Files.isDirectory(projectBasedir, LinkOption.NOFOLLOW_LINKS)) {
            if (isWindows()) {
                System.out.printf("Directory '%s' does not exist%n", projectBasedir);
            } else {
                System.out.printf("❌ Directory '%s' does not exist%n", projectBasedir);
            }
            System.exit(1);
        }

        try {
            var tmp = Files.createTempDirectory("gradle-wrapper");
            var distributionZipPath = tmp.resolve(DISTRIBUTION_FILE_NAME);
            var distributionPath = tmp.resolve(DISTRIBUTION_NAME);

            downloadDistribution(DEFAULT_DOWNLOAD_URL, distributionZipPath);
            unpackDistribution(distributionZipPath, tmp.toFile());
            var wrapperProjectPath = generateWrapperProject(tmp);
            generateWrapper(distributionPath, wrapperProjectPath);
            copyWrapperJar(wrapperProjectPath, projectBasedir);

            System.exit(0);
        } catch (Exception e) {
            if (isWindows()) {
                System.err.println("Could not setup Gradle wrapper");
            } else {
                System.err.println("❌ Could not setup Gradle wrapper");
            }
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void downloadDistribution(String urlString, Path destination) throws URISyntaxException, IOException {
        if (isWindows()) {
            System.out.printf("Downloading %s%n", urlString);
        } else {
            System.out.printf("⬇️  Downloading %s%n", urlString);
        }

        if (null != System.getenv("GRADLEW_USERNAME") && null != System.getenv("GRADLEW_PASSWORD")) {
            var username = System.getenv("GRADLEW_USERNAME");
            var password = System.getenv("GRADLEW_PASSWORD").toCharArray();
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }

        var website = new URI(urlString).toURL();
        try (var inStream = website.openStream()) {
            Files.copy(inStream, destination, REPLACE_EXISTING);
        }
    }

    private static void unpackDistribution(Path distributionZipPath, File destinationDir) throws IOException {
        if (isWindows()) {
            System.out.printf("Unpacking %s%n", distributionZipPath.getFileName());
        } else {
            System.out.printf("📦 Unpacking %s%n", distributionZipPath.getFileName());
        }

        var distributionFile = distributionZipPath.toFile();
        try (var zipFile = new ZipFile(distributionFile)) {
            var buffer = new byte[1024];

            var entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                var entryName = entry.getName();
                var file = new File(destinationDir, entryName);

                if (entry.isDirectory()) {
                    if (!file.isDirectory() && !file.mkdirs()) {
                        throw new IOException("failed to create directory " + file);
                    }
                } else {
                    var parent = file.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("failed to create directory " + parent);
                    }

                    try (var in = zipFile.getInputStream(entry);
                         var fos = new FileOutputStream(file)) {
                        var len = 0;
                        while ((len = in.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    private static Path generateWrapperProject(Path parent) throws IOException {
        if (isWindows()) {
            System.out.println("Creating wrapper project");
        } else {
            System.out.println("📝 Creating wrapper project");
        }

        var wrapperProjectPath = parent.resolve("wrapper-project");
        Files.createDirectories(wrapperProjectPath);
        Files.writeString(wrapperProjectPath.resolve("settings.gradle"), "rootProject.name = 'wrapper-project'");
        return wrapperProjectPath;
    }

    private static void generateWrapper(Path distributionPath, Path wrapperProjectPath) throws Exception {
        if (isWindows()) {
            System.out.println("Generating wrapper");
        } else {
            System.out.println("👷‍ Generating wrapper");
        }

        var ext = isWindows() ? ".bat" : "";
        var launchScript = distributionPath.toAbsolutePath() + "/bin/gradle" + ext;
        Paths.get(launchScript).toFile().setExecutable(true);
        var pb = new ProcessBuilder(launchScript, "-S", "wrapper");
        pb.directory(wrapperProjectPath.toFile());
        var gradle = pb.start();

        if (0 != gradle.waitFor()) {
            throw new IllegalStateException("Gradle failed to generate a wrapper");
        }
    }

    private static void copyWrapperJar(Path wrapperProjectPath, Path projectBasedir) throws IOException {
        var destinationDir = projectBasedir.resolve("gradle/wrapper");
        if (isWindows()) {
            System.out.printf("Copying gradle-wrapper.jar to %s%n", destinationDir);
        } else {
            System.out.printf("🚚 Copying gradle-wrapper.jar to %s%n", destinationDir);
        }

        Files.copy(wrapperProjectPath.resolve("gradle/wrapper/gradle-wrapper.jar"),
            destinationDir.resolve("gradle-wrapper.jar"),
            REPLACE_EXISTING);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name")
            .toLowerCase(Locale.ENGLISH)
            .contains("windows");
    }
}
