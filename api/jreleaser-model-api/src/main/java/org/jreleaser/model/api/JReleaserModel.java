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
package org.jreleaser.model.api;

import org.jreleaser.model.api.announce.Announce;
import org.jreleaser.model.api.assemble.Assemble;
import org.jreleaser.model.api.catalog.Catalog;
import org.jreleaser.model.api.checksum.Checksum;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.Matrix;
import org.jreleaser.model.api.deploy.Deploy;
import org.jreleaser.model.api.distributions.Distribution;
import org.jreleaser.model.api.download.Download;
import org.jreleaser.model.api.environment.Environment;
import org.jreleaser.model.api.extensions.Extension;
import org.jreleaser.model.api.files.Files;
import org.jreleaser.model.api.hooks.Hooks;
import org.jreleaser.model.api.packagers.Packagers;
import org.jreleaser.model.api.platform.Platform;
import org.jreleaser.model.api.project.Project;
import org.jreleaser.model.api.release.Release;
import org.jreleaser.model.api.signing.Signing;
import org.jreleaser.model.api.upload.Upload;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface JReleaserModel extends Domain {
    ZonedDateTime getNow();

    String getTimestamp();

    Commit getCommit();

    Environment getEnvironment();

    Matrix getMatrix();

    Hooks getHooks();

    Platform getPlatform();

    Project getProject();

    Release getRelease();

    Packagers getPackagers();

    Announce getAnnounce();

    Assemble getAssemble();

    Download getDownload();

    Deploy getDeploy();

    Upload getUpload();

    Checksum getChecksum();

    Signing getSigning();

    Files getFiles();

    Catalog getCatalog();

    Map<String, ? extends Distribution> getDistributions();

    Map<String, ? extends Extension> getExtensions();

    final class Commit {
        private final String shortHash;
        private final String fullHash;
        private final String refName;
        private final int commitTime;
        private final ZonedDateTime timestamp;

        public Commit(String shortHash, String fullHash, String refName, int commitTime, ZonedDateTime timestamp) {
            this.shortHash = shortHash;
            this.fullHash = fullHash;
            this.refName = refName;
            this.commitTime = commitTime;
            this.timestamp = timestamp;
        }

        public String getShortHash() {
            return shortHash;
        }

        public String getFullHash() {
            return fullHash;
        }

        public String getRefName() {
            return refName;
        }

        public int getCommitTime() {
            return commitTime;
        }

        public ZonedDateTime getTimestamp() {
            return timestamp;
        }
    }
}
