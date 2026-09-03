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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class AbstractMavenDeployerTest {
    @TempDir
    private Path tempDir;

    @Test
    void missingPackagingDefaultsToJar() throws IOException {
        var path = pom("""
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>demo</artifactId>
              <version>1.0.0</version>
            </project>
            """);

        var result = AbstractMavenDeployer.DeployableCollector.parsePom(path);

        assertThat(result.packaging).isEqualTo("jar");
        assertThat(result.relocated).isFalse();
    }

    @Test
    void declaredPackagingIsRead() throws IOException {
        var path = pom("""
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <artifactId>demo</artifactId>
              <packaging>pom</packaging>
            </project>
            """);

        var result = AbstractMavenDeployer.DeployableCollector.parsePom(path);

        assertThat(result.packaging).isEqualTo("pom");
        assertThat(result.relocated).isFalse();
    }

    @Test
    void relocationIsDetected() throws IOException {
        var path = pom("""
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <artifactId>old</artifactId>
              <distributionManagement>
                <relocation>
                  <artifactId>new</artifactId>
                </relocation>
              </distributionManagement>
            </project>
            """);

        var result = AbstractMavenDeployer.DeployableCollector.parsePom(path);

        assertThat(result.packaging).isEqualTo("jar");
        assertThat(result.relocated).isTrue();
    }

    @Test
    void distributionManagementWithoutRelocationIsNotRelocated() throws IOException {
        var path = pom("""
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <artifactId>demo</artifactId>
              <distributionManagement>
                <repository>
                  <id>central</id>
                </repository>
              </distributionManagement>
            </project>
            """);

        var result = AbstractMavenDeployer.DeployableCollector.parsePom(path);

        assertThat(result.relocated).isFalse();
    }

    @Test
    void onlyDirectChildrenOfProjectAreConsidered() throws IOException {
        var path = pom("""
            <project xmlns="http://maven.apache.org/POM/4.0.0">
              <artifactId>demo</artifactId>
              <profiles>
                <profile>
                  <packaging>pom</packaging>
                  <distributionManagement>
                    <relocation>
                      <artifactId>new</artifactId>
                    </relocation>
                  </distributionManagement>
                </profile>
              </profiles>
            </project>
            """);

        var result = AbstractMavenDeployer.DeployableCollector.parsePom(path);

        assertThat(result.packaging).isEqualTo("jar");
        assertThat(result.relocated).isFalse();
    }

    private Path pom(String body) throws IOException {
        var path = tempDir.resolve("demo-1.0.0.pom");
        Files.writeString(path, """
            <?xml version="1.0" encoding="UTF-8"?>
            """ + body, UTF_8);
        return path;
    }
}
