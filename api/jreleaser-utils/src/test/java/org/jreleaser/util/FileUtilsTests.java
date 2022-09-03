/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class FileUtilsTests {
    @ParameterizedTest
    @EnumSource(value = FileType.class,
        names = {"TAR", "TAR_BZ2", "TAR_GZ", "TAR_XZ", "TBZ2", "TGZ", "TXZ", "ZIP"})
    @Platform(platform = "windows", match = false)
    public void unpackArchiveWithExecutable(FileType fileType) throws IOException {
        // given:
        Path resourcesDir = Paths.get(".")
            .resolve("src/test/resources")
            .normalize();
        Path archive = resourcesDir.resolve("app-1.0.0" + fileType.extension());
        Path tmp = Files.createTempDirectory(fileType.name());

        // when:
        FileUtils.unpackArchive(archive, tmp, false);

        // then:
        Path license = tmp.resolve("app-1.0.0").resolve("LICENSE");
        assertTrue(() -> Files.exists(license), "LICENSE exists");
        Path executable = tmp.resolve("app-1.0.0").resolve("bin/executable");
        assertTrue(() -> Files.exists(executable), "executable exists");
        assertTrue(() -> Files.isExecutable(executable), "executable has executable bit set");
    }
}
