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

import org.jreleaser.test.Platform;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
class FileUtilsTests {
    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

    @ParameterizedTest
    @EnumSource(value = FileType.class,
        names = {"TAR", "TAR_BZ2", "TAR_GZ", "TAR_XZ", "TAR_ZST", "TBZ2", "TGZ", "TXZ", "ZIP"})
    @Platform(platform = "windows", match = false)
    void packAndUnpackArchive(FileType fileType) throws IOException {
        // given:
        Path resourcesDir = Paths.get(".")
            .resolve("src/test/resources")
            .normalize();
        Path src = resourcesDir.resolve("archive");
        Path tmp1 = Files.createTempDirectory(fileType.name());
        Path tmp2 = Files.createTempDirectory(fileType.name());
        Path archive = tmp1.resolve("app-1.0.0" + fileType.extension());

        // when:
        FileUtils.ArchiveOptions options = new FileUtils.ArchiveOptions();
        options.withTimestamp(TIMESTAMP);
        FileUtils.packArchive(src, archive, options);
        FileUtils.unpackArchive(archive, tmp2, false);

        // then:
        Path license = tmp2.resolve("app-1.0.0").resolve("LICENSE");
        assertTrue(() -> Files.exists(license), "LICENSE exists");
        Path executable = tmp2.resolve("app-1.0.0").resolve("bin/executable");
        assertTrue(() -> Files.exists(executable), "executable exists");
        assertTrue(() -> Files.isExecutable(executable), "executable has executable bit set");
    }
}
