/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.commons;

import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.TreeSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class SnapshotStamperTest {
    private static final String VERSION_PATH = "/com/example/lib/1.0.0-SNAPSHOT";
    // 14:00 in Kolkata is 08:30 UTC; Maven timestamps are UTC
    private static final ZonedDateTime NOW = ZonedDateTime.of(2026, 10, 17, 14, 0, 5, 0, ZoneId.of("Asia/Kolkata"));

    @TempDir
    private Path staging;

    @TempDir
    private Path target;

    @Test
    void plainSnapshotFilesAreCopiedUnderOneTimestampWithVersionMetadata() throws IOException {
        // Given: a snapshot staged the way 'mvn install' does (#2179)
        var deployables = stage(true,
            "lib-1.0.0-SNAPSHOT.jar",
            "lib-1.0.0-SNAPSHOT.pom",
            "lib-1.0.0-SNAPSHOT-sources.jar",
            "lib-1.0.0-SNAPSHOT.jar.asc");

        // When
        var stamped = SnapshotStamper.stamp(deployables, target, NOW);

        // Then: every file is deployed under the name 'mvn deploy' gives it, from a copy
        assertThat(stamped.stream().map(Deployable::getFilename).collect(toList())).containsExactly(
            "lib-1.0.0-20261017.083005-1-sources.jar",
            "lib-1.0.0-20261017.083005-1.jar",
            "lib-1.0.0-20261017.083005-1.jar.asc",
            "lib-1.0.0-20261017.083005-1.pom",
            "maven-metadata.xml");
        assertThat(stamped).allSatisfy(d -> {
            assertThat(d.getLocalPath()).exists().startsWith(target.toAbsolutePath());
            assertThat(d.getFullDeployPath()).startsWith("com/example/lib/1.0.0-SNAPSHOT/");
        });
        assertThat(Files.readString(target.resolve("com/example/lib/1.0.0-SNAPSHOT/lib-1.0.0-20261017.083005-1.jar")))
            .isEqualTo("lib-1.0.0-SNAPSHOT.jar");
        assertThat(staging.resolve("com/example/lib/1.0.0-SNAPSHOT/lib-1.0.0-SNAPSHOT.jar")).exists();

        // And: the version metadata lists the artifacts, but not their signatures
        assertThat(Files.readString(target.resolve("com/example/lib/1.0.0-SNAPSHOT/maven-metadata.xml"))).isEqualTo("""
            <?xml version="1.0" encoding="UTF-8"?>
            <metadata modelVersion="1.1.0">
              <groupId>com.example</groupId>
              <artifactId>lib</artifactId>
              <version>1.0.0-SNAPSHOT</version>
              <versioning>
                <snapshot>
                  <timestamp>20261017.083005</timestamp>
                  <buildNumber>1</buildNumber>
                </snapshot>
                <lastUpdated>20261017083005</lastUpdated>
                <snapshotVersions>
                  <snapshotVersion>
                    <classifier>sources</classifier>
                    <extension>jar</extension>
                    <value>1.0.0-20261017.083005-1</value>
                    <updated>20261017083005</updated>
                  </snapshotVersion>
                  <snapshotVersion>
                    <extension>jar</extension>
                    <value>1.0.0-20261017.083005-1</value>
                    <updated>20261017083005</updated>
                  </snapshotVersion>
                  <snapshotVersion>
                    <extension>pom</extension>
                    <value>1.0.0-20261017.083005-1</value>
                    <updated>20261017083005</updated>
                  </snapshotVersion>
                </snapshotVersions>
              </versioning>
            </metadata>
            """);
    }

    @Test
    void stagedMetadataOfAStampedVersionIsReplaced() throws IOException {
        var deployables = stage(true, "lib-1.0.0-SNAPSHOT.jar", "maven-metadata.xml", "maven-metadata.xml.sha1");

        var stamped = SnapshotStamper.stamp(deployables, target, NOW);

        assertThat(stamped.stream().map(Deployable::getFilename).collect(toList()))
            .containsExactly("lib-1.0.0-20261017.083005-1.jar", "maven-metadata.xml");
        assertThat(stamped).allSatisfy(d -> assertThat(d.getLocalPath()).startsWith(target.toAbsolutePath()));
    }

    @Test
    void timestampedSnapshotFilesAreLeftAlone() throws IOException {
        var deployables = stage(true, "lib-1.0.0-20261017.120000-1.jar", "maven-metadata.xml");

        assertThat(SnapshotStamper.stamp(deployables, target, NOW)).isSameAs(deployables);
        assertThat(target).isEmptyDirectory();
    }

    private Set<Deployable> stage(boolean snapshot, String... filenames) throws IOException {
        Path dir = staging.resolve(VERSION_PATH.substring(1));
        Files.createDirectories(dir);

        Set<Deployable> deployables = new TreeSet<>();
        for (String filename : filenames) {
            Files.write(dir.resolve(filename), filename.getBytes(UTF_8));
            deployables.add(new Deployable(snapshot, staging.toAbsolutePath().toString(), VERSION_PATH, "jar", filename, false));
        }
        return deployables;
    }
}
