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

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.String.join;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.lineSeparator;
import static java.lang.System.out;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public class update_release_page {
    private static final Pattern PATTERN_SHA256 = Pattern.compile("sha256:([\\w\\.\\-]+)");

    public static void main(String[] args) {
        if (null == args || args.length != 2) {
            err.println("Usage: java update_release_page <checksumsFile> <releasePage>");
            exit(1);
        }

        var checksumsFile = Path.of(args[0]);
        var releasePage = Path.of(args[1]);

        if (!exists(checksumsFile)) {
            err.printf("File does not exist. %s%n", checksumsFile.toAbsolutePath());
            exit(1);
        }

        if (!exists(releasePage)) {
            err.printf("File does not exist. %s%n", releasePage.toAbsolutePath());
            exit(1);
        }

        out.printf("Adjusting %s with checksums from %s%n", releasePage, checksumsFile);
        exit(process(checksumsFile, releasePage));
    }

    private static int process(Path checksumsFile, Path releasePage) {
        var checksums = new LinkedHashMap<String, String>();

        try {
            readAllLines(checksumsFile).forEach(line -> {
                var checksum = line.substring(0, 65).trim();
                var filename = line.substring(66).trim();
                checksums.put(filename, checksum);
            });
        } catch (IOException e) {
            err.printf("Unexpected error reading checksums. %s%n", e.getMessage());
            return 1;
        }

        out.printf("%s checksums read%n", checksums.size());

        int[] replaceCount = new int[]{0};
        try {
            List<String> lines = readAllLines(releasePage).stream()
                .map(line -> replaceChecksums(line, checksums, replaceCount))
                .collect(toList());
            write(releasePage, join(lineSeparator(), lines).getBytes(), WRITE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            err.printf("Unexpected error replacing checksums. %s%n", e.getMessage());
            return 1;
        }

        out.printf("%s checksums replaced%n", replaceCount[0]);

        return 0;
    }

    private static String replaceChecksums(String line, Map<String, String> checksums, int[] replaceCount) {
        var matcher = PATTERN_SHA256.matcher(line);

        while (matcher.find()) {
            var filename = matcher.group(1);
            if (checksums.containsKey(filename)) {
                line = line.replace("sha256:" + filename, "sha256:`" + checksums.get(filename) + "`");
                replaceCount[0]++;
            } else {
                line = line.replace("sha256:" + filename, "");
            }
        }

        return line;
    }
}