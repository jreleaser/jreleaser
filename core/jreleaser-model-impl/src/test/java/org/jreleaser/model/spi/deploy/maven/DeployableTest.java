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
package org.jreleaser.model.spi.deploy.maven;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 1.21.0
 */
class DeployableTest {
    @ParameterizedTest
    @ValueSource(strings = {
        "git-283ef0c42f",
        "1.0.0-dev",
        "1.0.0-alpha",
        "feature-branch-123",
        "1.0.0-RC1"
    })
    void testCustomSnapshotVersionPattern(String version) {
        assertThrows(IllegalArgumentException.class,
            () -> new Deployable(
                true,
                "/staging/repo", "/com/example/artifact/" + version,
                "jar",
                "artifact-" + version + ".jar",
                false),
            "Snapshot version '" + version + "' must contain '-SNAPSHOT' suffix for Maven Deployment.");
    }

    @Test
    void testSnapshotVersionWithSnapshotSuffixSucceeds() {
        // Given: a proper snapshot version with "SNAPSHOT" suffix
        String snapshotVersion = "1.0.0-SNAPSHOT";
        
        // When: creating a Deployable with snapshot=true and proper "SNAPSHOT" suffix
        Deployable deployable = new Deployable(
            true,
            "/staging/repo",
            "/com/example/artifact/" + snapshotVersion,
            "jar",
            "artifact-1.0.0-20240101.120000-1.jar",
            false
        );
        
        // Then: it should succeed without throwing an exception
        assertThat(deployable.getVersion()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(deployable.getGroupId()).isEqualTo("com.example");
        assertThat(deployable.getArtifactId()).isEqualTo("artifact");
    }

    @Test
    void testNonSnapshotVersionSucceeds() {
        // Given: a release version
        String releaseVersion = "1.0.0";
        
        // When: creating a Deployable with snapshot=false
        Deployable deployable = new Deployable(
            false,
            "/staging/repo",
            "/com/example/artifact/" + releaseVersion,
            "jar",
            "artifact-"+ releaseVersion +".jar",
            false
        );
        
        // Then: it should succeed without throwing an exception
        assertThat(deployable.getVersion()).isEqualTo("1.0.0");
        assertThat(deployable.getGroupId()).isEqualTo("com.example");
        assertThat(deployable.getArtifactId()).isEqualTo("artifact");
    }
}
